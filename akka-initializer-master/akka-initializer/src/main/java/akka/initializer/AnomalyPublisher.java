package akka.initializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * This is a utility interface: The akka initializer library can be used for various use-cases. One use-case
 * is to use this framework for complex event processing detection and publish anomalies on such
 * detection. The classes defined here will help to ease out CEP use-case. These classes are not used
 * by the framework directly for any other purpose. JUNIT test cases uses this interface and related class
 * and tries to simulate a CEP use-case.
 */
public interface AnomalyPublisher {

    void publish(Anomaly anomaly);

    /**
     * A parametrized anomaly object that would be created by CEP engine.
     *
     * @param <T>
     */
    class Anomaly<T> {

        private T message;

        public Anomaly(T message) {
            this.message = message;
        }

        public T getMessage() {
            return message;
        }

        @Override
        public String toString() {
            return "Anomaly{" +
                    "message=" + message +
                    '}';
        }

    }

    @Service("consoleAnomalyPublisher")
    class ConsoleAnomalyPublisher implements AnomalyPublisher {

        private static final Logger LOGGER = LoggerFactory.getLogger(ConsoleAnomalyPublisher.class);

        @Override
        public void publish(Anomaly anomaly) {
            LOGGER.info("\n\r ************** \n\r Publishing ANOMALY \n\r ************** \n\r\n\r: " +
                    "{} \n\r\n\r **************", anomaly);
        }
    }

    /**
     * Can listen for anomalies, helpful during unit testing to perform asserts.
     */
    @Service("observableAnomalyPublisher")
    class ObservableAnomalyPublisher extends Observable implements AnomalyPublisher {

        private static final Logger LOGGER = LoggerFactory.getLogger(ObservableAnomalyPublisher.class);

        private ObservableAnomalyPublisher.AnomalyObserver anomalyObserver = new ObservableAnomalyPublisher.AnomalyObserver();

        @Override
        public void publish(Anomaly anomaly) {
            setChanged();
            notifyObservers(anomaly);
        }

        @PostConstruct
        public void postConstruct() {
            this.addObserver(anomalyObserver);
        }

        public ObservableAnomalyPublisher.AnomalyObserver getAnomalyObserver() {
            return anomalyObserver;
        }

        public List<Anomaly> blockForAnomalies(long sleepIntervalMillis, long maxSleepMillis) {
            return getAnomalyObserver().blockForAnomalies(sleepIntervalMillis, maxSleepMillis);
        }

        public void clear() {
            getAnomalyObserver().clear();
        }

        public static class AnomalyObserver implements Observer {

            private List<Anomaly> anomalies = new ArrayList<>();

            @Override
            public void update(Observable o, Object arg) {

                anomalies.add((Anomaly) arg);
            }

            public void clear() {
                anomalies.clear();
            }

            public List<Anomaly> blockForAnomalies(long sleepIntervalMillis, long maxSleepMillis) {
                Instant startTime = Instant.now();
                while (anomalies.size() == 0 && Instant.now().minusMillis(maxSleepMillis).isBefore(startTime)) {
                    try {
                        Thread.sleep(sleepIntervalMillis);
                    } catch (InterruptedException e) {
                        LOGGER.warn("Try again, error processing request: {}", e.toString());
                    }
                }

                return anomalies;
            }
        }

    }
}
