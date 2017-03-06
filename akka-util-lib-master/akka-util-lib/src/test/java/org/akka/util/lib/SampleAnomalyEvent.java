package org.akka.util.lib;


import org.akka.util.lib.model.DefaultMessage;

import java.io.Serializable;

public class SampleAnomalyEvent extends DefaultMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    public SampleAnomalyEvent(Object shardId, String entityId) {
        super(shardId, entityId);
    }

    @Override
    public String toString() {
        return "SampleAnomalyEvent{id: " +
                super.toString() +
                "}";
    }
}
