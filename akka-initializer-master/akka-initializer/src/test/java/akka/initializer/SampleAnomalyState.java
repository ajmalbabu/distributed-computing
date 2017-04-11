package akka.initializer;


import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class SampleAnomalyState {

    private List<SampleDelayEvent> sampleDelayEvents = new ArrayList<>();
    private SampleCancelEvent sampleCancelEvent;
    private boolean anomalyCheckScheduled;

    public void addDelayEvent(SampleDelayEvent sampleDelayEvent) {
        sampleDelayEvents.add(sampleDelayEvent);
    }

    public int delayEvents() {
        return sampleDelayEvents.size();
    }

    public void setSampleCancelEvent(SampleCancelEvent sampleCancelEvent) {
        this.sampleCancelEvent = sampleCancelEvent;
    }

    public boolean hasCancelEventWithinLastMinute() {
        return sampleCancelEvent != null && sampleCancelEvent.getCreateTime().isAfter(Instant.now().minus(Duration.ofMinutes(1)));
    }

    public boolean hasAnomaly() {
        return !hasCancelEventWithinLastMinute() &&
                hasTwoAnomalyWithinLastMinute();
    }

    public boolean hasTwoAnomalyWithinLastMinute() {
        return hasNumberOfAnomalyWithin(2, Instant.now().minus(Duration.ofMinutes(1)));
    }

    public boolean hasNumberOfAnomalyWithin(int numberOfAnomalies, Instant now) {
        return sampleDelayEvents.stream().filter(e -> e.isAfter(now)).count() >= numberOfAnomalies;
    }

    public boolean isAnomalyCheckScheduled() {
        return anomalyCheckScheduled;
    }

    public void setAnomalyCheckScheduled(boolean anomalyCheckScheduled) {
        this.anomalyCheckScheduled = anomalyCheckScheduled;
    }

    @Override
    public String toString() {
        return "SampleAnomalyState{" +
                "sampleDelayEvents=" + sampleDelayEvents +
                ", sampleCancelEvent=" + sampleCancelEvent +
                ", anomalyCheckScheduled=" + anomalyCheckScheduled +
                '}';
    }
}
