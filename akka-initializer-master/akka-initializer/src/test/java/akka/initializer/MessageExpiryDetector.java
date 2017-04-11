package akka.initializer;

import akka.actor.ActorRef;
import akka.initializer.model.*;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Helps to test message expiry implementation.
 */
public class MessageExpiryDetector extends PersistenceActor implements MessageExpiry {

    private MessageExpiryState messageExpiryState = new MessageExpiryState();
    private Time messageExpiryTime;

    public MessageExpiryDetector(ApplicationContext applicationContext, Parameters parameters) {
        messageExpiryTime = new Time(parameters.parseLong(ACTOR_STATE_MESSAGE_EXPIRY_TIME_MILLIS), TimeUnit.MILLISECONDS);
    }

    @Override
    protected void handleReceiveRecover(Message message) {

        if (message instanceof MessageExpiryEvent) {

            handleMessageExpiryEvent((MessageExpiryEvent) message);

        }
    }

    @Override
    protected void handleReceiveCommand(Message message) {

        if (message instanceof MessageExpiryEvent) {

            store(message, this::handleMessageExpiryEvent);

            acknowledge();

        } else if (message instanceof StateEventCountReq) {

            StateEventCountResp stateEventCountResp = new StateEventCountResp(message.getShardId(), message.getEntityId(), new ArrayList<>(messageExpiryState.getMessageExpiryEvents()));

            sender().tell(new ResponseMessage(ResponseMessage.ResponseType.MessageProcessed, stateEventCountResp), ActorRef.noSender());
        }
    }


    private void handleMessageExpiryEvent(MessageExpiryEvent messageExpiryEvent) {

        messageExpiryState.addMessageExpiryEvent(messageExpiryEvent);

    }


    @Override
    public String persistenceId() {
        return getSelf().path().parent() + "-" + getSelf().path().name();
    }

    @Override
    public MessageExpiryListener messageExpiryListener() {
        return messageExpiryState;
    }

    @Override
    public Time expiryTime() {
        return messageExpiryTime;
    }

    static public class StateEventCountReq extends DefaultMessage {

        public StateEventCountReq(Object shardId, String entityId) {
            super(shardId, entityId);
        }

        @Override
        public String toString() {
            return "StateEventCountReq{}" +
                    super.toString() +
                    "";
        }
    }

    static public class StateEventCountResp extends DefaultMessage {

        public final List<MessageExpiryEvent> messageExpiryEvents;


        public StateEventCountResp(Object shardId, String entityId, List<MessageExpiryEvent> messageExpiryEvents) {
            super(shardId, entityId);
            this.messageExpiryEvents = new ArrayList<>(messageExpiryEvents);
        }

        @Override
        public String toString() {
            return "StateEventCountResp{" +
                    super.toString() +
                    ", messageExpiryEvents=" + messageExpiryEvents +
                    '}';
        }
    }

}
