package akka.initializer.model;

import java.io.Serializable;

public class StopMessage extends DefaultMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    public StopMessage() {
        this(SELF_MESSAGE, SELF_MESSAGE);
    }

    public StopMessage(Object shardId, String entityId) {
        super(shardId, entityId);
    }


    @Override
    public String toString() {
        return "StopMessage{" +
                super.toString() +
                '}';
    }
}
