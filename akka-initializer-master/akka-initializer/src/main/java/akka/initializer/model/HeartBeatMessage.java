package akka.initializer.model;

import java.io.Serializable;
import java.time.Instant;
import java.util.Map;

/**
 * Helps to send heart-beat message to the Aggregate. Used purely for testing during development to
 * see if the Aggregate actor got created in one of the cluster nodes.
 */
public class HeartBeatMessage extends DefaultMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    public HeartBeatMessage(Object shardId, String entityId) {
        this(shardId, entityId, transactionId.currentTransactionIdAsMap(), Instant.now());
    }


    public HeartBeatMessage(Object shardId, String entityId, Map<String, Object> mdc, Instant instant) {
        super(shardId, entityId, mdc, instant);
    }

    @Override
    public String toString() {
        return "HeartBeatMessage{" +
                super.toString() +
                "}";
    }
}
