package akka.initializer;


import akka.initializer.model.DefaultMessage;

import java.io.Serializable;

public class SampleDelayEvent extends DefaultMessage implements Serializable {

    private static final long serialVersionUID = 1L;
    private String delayMessage;

    public SampleDelayEvent(Object shardId, String entityId, String delayContent) {
        super(shardId, entityId);
        this.delayMessage = delayContent;
    }

    public String getDelayMessage() {
        return delayMessage;
    }

    public void setDelayMessage(String delayMessage) {
        this.delayMessage = delayMessage;
    }

    @Override
    public String toString() {
        return "SampleDelayEvent{" +
                super.toString() +
                ", delayMessage='" + delayMessage + '\'' +
                '}';
    }
}
