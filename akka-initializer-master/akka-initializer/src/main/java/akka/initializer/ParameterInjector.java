package akka.initializer;


import akka.initializer.model.Parameters;

/**
 * Implement this interface if parameter needs to be injected into an Actor/Object.
 * <p>
 * One such parameter  could be the sender actor ref. This is needed in case where "some" actor needs
 * to manage a Request - Response pattern. In such case the responsibility of merging response from
 * the multiple intermediate response should be given to a coordinator actor. Even-though the coordinator
 * actor is created by "some" actor, the result should not flow back to the "some" actor. It should
 * flow back to the "sender" of the "Some" actor.
 * </p>
 * Other parameters are the parameters needed for the actor to work which could be only set at runtime
 * not at the actor bean definition time, e.g. Id of the target configuration.
 * <ol>
 * <li>A video by AKKA committer on request response pattern Jsuereth -
 * http://www.infoq.com/presentations/akka-scala-actors-distributed-system</li>
 * <li>AKKA concurrency book  first edition - page 448</li>
 * </ol>
 */
public interface ParameterInjector {

    /**
     * Override and get handle to the parameters passed into the implementor of this interface.
     *
     * @param parameters - parameters being passed.
     */
    void setParameters(Parameters parameters);

}
