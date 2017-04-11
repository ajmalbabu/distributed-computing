package akka.initializer;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.event.DiagnosticLoggingAdapter;
import akka.event.Logging;
import akka.initializer.model.ResponseMessage;
import akka.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.FiniteDuration;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

import static akka.pattern.Patterns.ask;

/**
 * During unit testing there are needs to assert the messages in a predictable way.
 * The base core library provides this class to make the testing easier.
 */
@Service(ResponseHandlerActor.RESPONSE_HANDLER_ACTOR)
@Scope("prototype")
@Lazy
public class ResponseHandlerActor extends UntypedActor {

    public static final String RESPONSE_HANDLER_ACTOR = "responseHandlerActor";
    public static final String RESPONSE_HANDLER_ACTOR_DISPATCHER = "task-runner-actor-dispatcher";
    public static final String GET_RESPONSE = "GetResponse";
    public static final String NOT_COMPLETED = "NotCompleted";
    private static final Logger LOGGER = LoggerFactory.getLogger(ResponseHandlerActor.class);

    private DiagnosticLoggingAdapter log = Logging.getLogger(this);
    public ResponseMessage responseMessage = null;

    @Override
    public void onReceive(Object message) throws Throwable {

        try {
            if (message instanceof ResponseMessage) {

                responseMessage = (ResponseMessage) message;
                if (responseMessage.getMessage() != null)
                    log.setMDC(responseMessage.getMdc());
                log.info("Response received: {}", responseMessage);

            } else if (message.equals(GET_RESPONSE)) {
                if (responseMessage == null) {
                    sender().tell(NOT_COMPLETED, ActorRef.noSender());
                } else {
                    sender().tell(responseMessage, ActorRef.noSender());
                }
            } else {
                log.error("unknown message to process: {}", message);
                unhandled(message);
            }
        } finally {
            log.clearMDC();
        }
    }


    public static ResponseMessage blockForResponse(ActorRef actorRef, long sleepIntervalMillis, long maxSleepMillis) {

        boolean completed = false;
        ResponseMessage responseMessage = null;
        Instant startTime = Instant.now();

        while (!completed && Instant.now().minusMillis(maxSleepMillis).isBefore(startTime)) {
            try {

                Object response = blockedResponse(actorRef, sleepIntervalMillis);

                if (response.equals(NOT_COMPLETED)) {
                    Thread.sleep(sleepIntervalMillis);
                } else {
                    responseMessage = (ResponseMessage) response;
                    if (!(completed = responseMessage.isCompleted())) {
                        Thread.sleep(sleepIntervalMillis);
                    }
                }
            } catch (Exception e) {
                LOGGER.info("Try again, error processing request: {}", e.toString());
            }
        }

        return responseMessage;

    }

    private static Object blockedResponse(ActorRef actorRef, long delayIntervalMillis) throws Exception {

        Timeout callTimeout = Timeout.durationToTimeout(FiniteDuration.create(delayIntervalMillis, TimeUnit.MILLISECONDS));
        Future<Object> futureResult = ask(actorRef, GET_RESPONSE, callTimeout);
        FiniteDuration duration = FiniteDuration.create(delayIntervalMillis, TimeUnit.MILLISECONDS);
        return Await.result(futureResult, duration);
    }
}
