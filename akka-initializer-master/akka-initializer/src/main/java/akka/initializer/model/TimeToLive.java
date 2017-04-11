package akka.initializer.model;

/**
 * An actor can implement this interface to get it removed after the time has passed.
 * Framework makes sure the actor (Typically Aggregate) gets removed and all the event
 * sources data for that actor from the persistence store gets removed as well.
 */
public interface TimeToLive {

    /**
     * @return the time duration after which actor should be removed.
     */
    Time actorTtl();
}
