package akka.initializer.model;

import java.io.Serializable;

/**
 * Used internally by framework. To delay the initialization of the actor using this message.
 * Wait for all the properties of the class to be set and initialized before actor can initialize
 * itself.
 */
public class IncarnationMessage extends DefaultMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    public IncarnationMessage() {
        super(SELF_MESSAGE, SELF_MESSAGE);
    }


    @Override
    public String toString() {
        return "IncarnationMessage{" +
                super.toString() +
                '}';
    }
}
