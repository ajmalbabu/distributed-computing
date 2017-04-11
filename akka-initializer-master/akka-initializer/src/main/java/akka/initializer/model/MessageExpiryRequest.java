package akka.initializer.model;

import java.io.Serializable;

public class MessageExpiryRequest implements Serializable {

    private static final long serialVersionUID = 1L;
    private Time expiryTime;

    public MessageExpiryRequest(Time expiryTime) {
        this.expiryTime = expiryTime;
    }

    public Time getExpiryTime() {
        return expiryTime;
    }

    @Override
    public String toString() {
        return "MessageExpiryRequest{" +
                ", expiryTime=" + expiryTime +
                '}';
    }
}
