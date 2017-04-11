package akka.initializer.model;

import akka.initializer.PersistenceActor;

/**
 * The actors that extends {@link PersistenceActor} should implement
 * this interface to expire and clean-up the older messages in their internal state.
 * The internal state objects should implement {@link MessageExpiryListener}
 */
public interface MessageExpiry {

    MessageExpiryListener messageExpiryListener();

    /**
     * The message expiry time frequency. At this frequency the framework calls
     * methods in the {@link MessageExpiryListener} to expire the qualified messages.
     */
    Time expiryTime();

}
