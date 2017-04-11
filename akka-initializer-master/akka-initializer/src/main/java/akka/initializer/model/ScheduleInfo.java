package akka.initializer.model;

import akka.actor.ActorRef;
import akka.actor.UntypedActorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.io.Serializable;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

/**
 * Helps to schedule scheduledMessage for a future time. Can be scheduled for once or for repeated.
 */
public class ScheduleInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduleInfo.class);

    private Instant initialScheduledTime;
    private Object scheduledMessage;
    private long triggerTime;
    private long repeatedTriggerTime;
    private TimeUnit triggerTimeUnit;

    /**
     * To schedule once at 'triggerTime'
     */
    public ScheduleInfo(Instant initialScheduledTime, Object scheduledMessage, long triggerTime, TimeUnit triggerTimeUnit) {
        this(initialScheduledTime, scheduledMessage, triggerTime, 0, triggerTimeUnit);
    }

    /**
     * To schedule repeated starting at 'triggerTime' and repeating at 'repeatedTriggerTime'.
     */
    public ScheduleInfo(Instant initialScheduledTime, Object scheduledMessage, long triggerTime, long repeatedTriggerTime, TimeUnit triggerTimeUnit) {
        this.initialScheduledTime = initialScheduledTime;
        this.scheduledMessage = scheduledMessage;
        this.triggerTime = triggerTime;
        this.repeatedTriggerTime = repeatedTriggerTime;
        this.triggerTimeUnit = triggerTimeUnit;
    }

    public Object getScheduledMessage() {
        return scheduledMessage;
    }

    public long getTriggerTime() {
        return triggerTime;
    }

    public TimeUnit getTriggerTimeUnit() {
        return triggerTimeUnit;
    }

    public long getRepeatedTriggerTime() {
        return repeatedTriggerTime;
    }

    public void schedule(UntypedActorContext context) {

        if (repeatedTriggerTime <= 0) {
            LOGGER.info("Scheduling once {}", toString());
            context.system().scheduler().scheduleOnce(
                    startTime(),
                    context.self(), getScheduledMessage(), context.dispatcher(), ActorRef.noSender());
        } else {
            LOGGER.info("Scheduling repeated {}", toString());
            context.system().scheduler().schedule(
                    startTime(),
                    Duration.create(getRepeatedTriggerTime(), getTriggerTimeUnit()),
                    context.self(), getScheduledMessage(), context.dispatcher(), ActorRef.noSender());

        }
    }

    private FiniteDuration startTime() {

        long startTime = 0;
        if (initialScheduledTime.isAfter(Instant.now())) {
            startTime = initialScheduledTime.toEpochMilli() + getTriggerTimeUnit().toMillis(getTriggerTime());
        } else {
            startTime = Instant.now().minusMillis(initialScheduledTime.toEpochMilli()).toEpochMilli();
            startTime = getTriggerTimeUnit().toMillis(getTriggerTime()) - startTime;
        }
        if (startTime < 0) {
            startTime = 100;
        }
        return Duration.create(startTime, TimeUnit.MILLISECONDS);
    }


    @Override
    public String toString() {
        return "ScheduleInfo{" +
                "initialScheduledTime=" + initialScheduledTime +
                ", scheduledMessage=" + scheduledMessage +
                ", triggerTime=" + triggerTime +
                ", repeatedTriggerTime=" + repeatedTriggerTime +
                ", triggerTimeUnit=" + triggerTimeUnit +
                '}';
    }
}

