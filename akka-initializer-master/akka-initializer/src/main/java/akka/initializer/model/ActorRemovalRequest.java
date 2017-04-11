package akka.initializer.model;

import java.io.Serializable;

/**
 * Used internally by framework. An internal event used by the framework to send this
 * message to remove the actor when the {@link TimeToLive} value is reached.
 */
public class ActorRemovalRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    public ActorRemovalRequest() {
    }

    @Override
    public String toString() {
        return "ActorRemovalRequest{" +
                "}";
    }
}
