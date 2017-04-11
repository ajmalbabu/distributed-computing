package akka.initializer.model;


import java.util.concurrent.TimeUnit;

public class Time {

    public final long time;
    public final TimeUnit timeUnit;

    public Time(long time, TimeUnit timeUnit) {
        this.time = time;
        this.timeUnit = timeUnit;
    }

    public long toMillis() {
        return timeUnit.toMillis(time);
    }

    @Override
    public String toString() {
        return "Time{" +
                "time=" + time +
                ", timeUnit=" + timeUnit +
                '}';
    }
}
