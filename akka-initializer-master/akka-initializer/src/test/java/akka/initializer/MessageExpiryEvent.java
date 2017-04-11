package akka.initializer;


import akka.initializer.model.DefaultMessage;

import java.io.Serializable;
import java.time.Instant;
import java.util.Map;

public class MessageExpiryEvent extends DefaultMessage implements Serializable {

    private static final long serialVersionUID = 1L;
    private int eventId;

    public MessageExpiryEvent(Object shardId, String entityId, int eventId) {
        super(shardId, entityId);
        this.eventId = eventId;
    }

    public MessageExpiryEvent(Object shardId, String entityId, int eventId, Map<String, Object> mdc, String eventTime) {
        super(shardId, entityId, mdc, Instant.parse(eventTime));
        this.eventId = eventId;
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    @Override
    public String toString() {
        return "MessageExpiryEvent{" +
                super.toString() +
                ", eventId=" + eventId +
                '}';
    }
}
