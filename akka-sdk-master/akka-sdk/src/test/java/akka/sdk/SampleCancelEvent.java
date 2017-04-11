package akka.sdk;


import akka.sdk.model.DefaultMessage;

import java.io.Serializable;

public class SampleCancelEvent extends DefaultMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    public SampleCancelEvent(Object shardId, String entityId) {
        super(shardId, entityId);
    }

    @Override
    public String toString() {
        return "SampleCancelEvent{" +
                super.toString() +
                "}";
    }
}
