package akka.initializer;

import akka.initializer.AnomalyPublisher.Anomaly;
import akka.initializer.model.Message;
import akka.initializer.model.Parameters;
import akka.initializer.model.ScheduleInfo;
import org.springframework.context.ApplicationContext;

import java.time.Instant;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * A sample anomaly detector to test out AKKA cluster & AKKA persistence features of AKKA initializer library. This is an anomaly
 * detector that holds state and receives SampleDelayEvent(s) and generate anomaly when two SampleDelayEvent happen within
 * 4 secs. But, if a SampleCancelEvent occurs within 4 secs after a SampleDelayEvent, anomaly gets cancelled. Following
 * scenario can be tested with this implementation.
 * <p>
 * This actor reads cluster shard region configurations from the yaml files.
 * <p>
 * 1. Anomaly is generated correctly?
 * 2. Anomaly gets canceled if CancelEvents is received on time?
 */
public class SampleAnomalyDetector extends PersistenceActor {

    public static final String ANOMALY_PUBLISHER_BEAN = "anomaly.publisher.bean";
    private static final String ANOMALY_TRIGGER_TIME_SECS = "anomaly.trigger.time.secs";
    private SampleAnomalyState sampleAnomalyState = new SampleAnomalyState();
    private AnomalyPublisher anomalyPublisher;
    private long anomalyTriggerTimeSecs;

    public SampleAnomalyDetector(ApplicationContext applicationContext, Parameters parameters) {
        this.anomalyPublisher = applicationContext.getBean(parameters.getString(ANOMALY_PUBLISHER_BEAN), AnomalyPublisher.class);
        this.anomalyTriggerTimeSecs = parameters.parseLong(ANOMALY_TRIGGER_TIME_SECS);

    }

    @Override
    protected void handleReceiveRecover(Message message) {
        if (message instanceof SampleDelayEvent) {

            handleDelay((SampleDelayEvent) message);

        } else if (message instanceof SampleCancelEvent) {

            handleCancel((SampleCancelEvent) message);

        } else if (message instanceof SampleAnomalyEvent) {

            handleAnomaly(message);
        }
    }

    @Override
    protected void handleReceiveCommand(Message message) {

        if (message instanceof SampleDelayEvent) {

            store(message, this::handleDelay);

        } else if (message instanceof SampleCancelEvent) {

            store(message, this::handleCancel);

        } else if (message instanceof SampleAnomalyEvent) {

            store(message, this::handleAnomaly);
        }
        acknowledge();
    }


    private void handleDelay(SampleDelayEvent sampleDelayEvent) {

        sampleAnomalyState.addDelayEvent(sampleDelayEvent);

        LOGGER.info(sampleAnomalyState.toString());

        if (!sampleAnomalyState.isAnomalyCheckScheduled()) {

            SampleAnomalyEvent sampleAnomalyEvent = new SampleAnomalyEvent(sampleDelayEvent.getShardId(), sampleDelayEvent.getEntityId());

            ScheduleInfo scheduleInfo = new ScheduleInfo(Instant.now(), sampleAnomalyEvent, anomalyTriggerTimeSecs, SECONDS);

            scheduleInfo.schedule(getContext());

            sampleAnomalyState.setAnomalyCheckScheduled(true);
        }
    }


    private void handleCancel(SampleCancelEvent sampleCancelEvent) {
        sampleAnomalyState.setSampleCancelEvent(sampleCancelEvent);
    }

    private void handleAnomaly(Message message) {

        LOGGER.info(sampleAnomalyState.toString());

        // sampleAnomalyState.hasAnomaly() method checks if there was a cancel event between delays events.
        if (sampleAnomalyState.hasAnomaly()) {
            Anomaly<String> anomaly = new Anomaly<>("Anomaly detected");
            anomalyPublisher.publish(anomaly);
        }


    }

    @Override
    public String persistenceId() {
        return getSelf().path().parent() + "-" + getSelf().path().name();
    }


}
