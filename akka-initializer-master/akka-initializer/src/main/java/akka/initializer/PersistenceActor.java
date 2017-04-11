package akka.initializer;

import akka.actor.ActorRef;
import akka.cluster.sharding.ShardRegion;
import akka.event.DiagnosticLoggingAdapter;
import akka.event.Logging;
import akka.persistence.DeleteMessagesSuccess;
import akka.persistence.UntypedPersistentActor;
import akka.initializer.model.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * A base class actor that makes event sourcing easier. Provide extension points to
 * implement Time-to-live {@link TimeToLive} and message expiry {@link MessageExpiry}
 */
public abstract class PersistenceActor extends UntypedPersistentActor {

    public static final String ACTOR_TIME_TO_LIVE_SECONDS = "actor.TTL.seconds";
    public static final String ACTOR_TIME_TO_LIVE_MINUTES = "actor.TTL.minutes";
    public static final String ACTOR_STATE_MESSAGE_EXPIRY_TIME_MINUTES = "actor.state.message.expiry.minutes";
    public static final String ACTOR_STATE_MESSAGE_EXPIRY_TIME_SECONDS = "actor.state.message.expiry.seconds";
    public static final String ACTOR_STATE_MESSAGE_EXPIRY_TIME_MILLIS = "actor.state.message.expiry.millis";

    protected DiagnosticLoggingAdapter LOGGER = Logging.getLogger(this);

    protected TransactionId transactionId = TransactionId.instance();

    /**
     * Each time the actor gets created or re-created a new incarnation request with the time element is added into this list.
     */
    private List<IncarnationMessage> incarnationMessages = new ArrayList<>();

    /**
     * Gets called if/when time-to-live is configured & reached.
     */
    private boolean passivateActor;

    /**
     * If message expiry frequency is set, then this listener is used to clean-up the message.
     */
    private MessageExpiryListener messageExpiryListener;

    /**
     * Captures the last received command message.
     */
    protected Message lastCommandMessage;


    public PersistenceActor() {
        initialize();
    }

    private void initialize() {

        IncarnationMessage incarnationMessage = new IncarnationMessage();

        ScheduleInfo scheduleInfo = new ScheduleInfo(Instant.now(), incarnationMessage, 1, TimeUnit.MILLISECONDS);

        scheduleInfo.schedule(getContext());
    }

    /**
     * Handle message is if it is not already processed. Set and unset LOGGER MDC context.
     *
     * @param msg - Message to be recovered.
     */
    @Override
    public void onReceiveRecover(Object msg) {

        LOGGER.info("ReceiveRecover on actor {} - {} with message: {}", self(), persistenceId(), msg);

        try {

            if (msg instanceof IncarnationMessage) {

                incarnationMessages.add((IncarnationMessage) msg);

            } else if (msg instanceof Message) {

                receiveRecoverMessage((Message) msg);

            } else {
                unhandled(msg);
            }

        } finally {
            unsetMdc();
        }

    }

    private void receiveRecoverMessage(Message message) {

        setMdc(message);

        handleReceiveRecover(message);
    }

    /**
     * Handle message is if it is not already processed. Set and unset LOGGER MDC context.
     *
     * @param msg - Message to be processed.
     */
    @Override
    public void onReceiveCommand(Object msg) {

        LOGGER.info("ReceiveCommand on actor {} with message: {}", persistenceId(), msg);

        try {

            setMdc(msg);

            if (msg instanceof IncarnationMessage) {

                handleIncarnation((IncarnationMessage) msg);

            } else if (msg instanceof StopMessage) {

                handleActorStop();

            } else if (msg instanceof MessageExpiryRequest) {

                handleMessageExpiry((MessageExpiryRequest) msg);

            } else if (msg instanceof ActorRemovalRequest) {

                handleActorRemovalRequest();

            } else if (msg instanceof Message) {

                handleMessage(msg);

            } else if (msg instanceof DeleteMessagesSuccess) {

                handleDeleteMessageSuccess(msg);
            }

        } finally {
            unsetMdc();
        }

    }

    @Override
    public String persistenceId() {
        return getSelf().path().name();
    }

    private void handleIncarnation(IncarnationMessage incarnationMessage) {
        store(incarnationMessage, this::scheduleTtlAndExpiry);
    }

    private void scheduleTtlAndExpiry(IncarnationMessage incarnationMessage) {

        incarnationMessages.add(incarnationMessage);
        scheduleTtl();
        scheduleMessageExpiry();

    }

    /**
     * Registers the actor to be automatically garbage collected/removed at a later timeToLive if actor implements
     * TimeToLive interface. If a node failure happens and actor gets started on another node. Then actor removal
     * time gets adjusted by considering the original actor incarnation time.
     */
    private void scheduleTtl() {

        if (TimeToLive.class.isAssignableFrom(getClass())) {

            Time ttl = ((TimeToLive) this).actorTtl();

            ScheduleInfo scheduleInfo = new ScheduleInfo(incarnationMessages.get(0).getCreateTime(),
                    new ActorRemovalRequest(), ttl.time, ttl.timeUnit);

            scheduleInfo.schedule(getContext());
        }
    }

    /**
     * Registers for message expiry on a sliding window basis if actor implements MessageExpiry interface.
     * If a node failure happens and actor gets started on another node. Then message expiry time gets
     * adjusted by considering the original actor incarnation time.
     */
    private void scheduleMessageExpiry() {

        if (MessageExpiry.class.isAssignableFrom(getClass())) {

            MessageExpiry messageExpiry = (MessageExpiry) this;
            messageExpiryListener = messageExpiry.messageExpiryListener();

            MessageExpiryRequest messageExpiryRequest = new MessageExpiryRequest(messageExpiry.expiryTime());

            ScheduleInfo scheduleInfo = new ScheduleInfo(incarnationMessages.get(incarnationMessages.size() - 1).getCreateTime(),
                    messageExpiryRequest, messageExpiry.expiryTime().time, messageExpiry.expiryTime().time,
                    messageExpiry.expiryTime().timeUnit);

            scheduleInfo.schedule(getContext());
        }

    }

    private void handleMessage(Object msg) {

        lastCommandMessage = (Message) msg;

        if (msg instanceof HeartBeatMessage) {

            LOGGER.info("Received heart beat {}", msg);
            getSender().tell(new ResponseMessage(ResponseMessage.ResponseType.MessageProcessed, null), ActorRef.noSender());

        } else {
            LOGGER.debug("Continue the process {}", lastCommandMessage);
            handleReceiveCommand(lastCommandMessage);
        }
    }

    private void handleMessageExpiry(MessageExpiryRequest messageExpiryRequest) {

        LOGGER.info("Received message expiry request @ {}. Find highest seq number ", persistenceId());
        long toSequenceNr = messageExpiryListener.expirySequenceNr(messageExpiryRequest.getExpiryTime());

        if (toSequenceNr == Long.MIN_VALUE) {
            LOGGER.info("Do not cleanupState messages @ {}. None found to cleanupState", persistenceId());
        } else {
            LOGGER.info("Expire messages @ {}. for anything <= seq number: {}", persistenceId(), toSequenceNr);
            deleteMessages(toSequenceNr);
        }
    }

    private void handleActorRemovalRequest() {

        LOGGER.info("Received actor clean-up request, remove event store elements and passivate actor {}", persistenceId());

        if (lastSequenceNr() > 0) {
            LOGGER.info("Remove all event sourced message for this actor {}", persistenceId());
            passivateActor = true;
            deleteMessages(lastSequenceNr());
        } else {
            checkAndPassivate(true);
        }
    }

    private void handleDeleteMessageSuccess(Object msg) {

        DeleteMessagesSuccess deleteMessageSuccess = (DeleteMessagesSuccess) msg;
        LOGGER.info("Clean up state @ {}. up to seq number {}", persistenceId(), deleteMessageSuccess.toSequenceNr());
        if (messageExpiryListener != null)
            messageExpiryListener.cleanupState(deleteMessageSuccess.toSequenceNr());

        checkAndPassivate(passivateActor);
    }

    private void checkAndPassivate(boolean passivateActorArg) {

        if (passivateActorArg) {
            LOGGER.info("Send stop message to STOP the actor {}", persistenceId());
            ShardRegion.Passivate passivate = new ShardRegion.Passivate(new StopMessage());
            getContext().parent().tell(passivate, self());
        }
    }

    private void handleActorStop() {
        LOGGER.info("Stop received, stopping actor {}", persistenceId());
        getContext().stop(self());
    }


    /**
     * Persists, updates sequence number of last persisted message and call next function.
     */
    protected <T> void store(Message message, Consumer<T> nextFunction) {

        persistInternal(message, nextFunction);
    }

    /**
     * Persists, updates sequence number of last persisted message.
     */
    protected <T> void store(Message message) {

        persistInternal(message, Message.noFunction());
    }

    private <T> void persistInternal(Message message, Consumer<T> nextFunction) {

        persist(message, msg -> {

            message.setSequenceNr(lastSequenceNr());

            if (nextFunction != Message.noFunction())
                nextFunction.accept((T) msg);

        });
    }


    protected void acknowledge() {
        getSender().tell(new ResponseMessage(ResponseMessage.ResponseType.MessageProcessed, lastCommandMessage), ActorRef.noSender());
    }

    private void setMdc(Object message) {
        if (message instanceof Message) {
            LOGGER.setMDC(((Message) message).getMdc());
            transactionId.setTransactionId(((Message) message).getMdc());
        }
    }

    private void unsetMdc() {
        transactionId.clear();
        LOGGER.clearMDC();
    }

    protected abstract void handleReceiveRecover(Message stateElement);

    protected abstract void handleReceiveCommand(Message message);


}
