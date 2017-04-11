package akka.initializer;

import akka.actor.*;
import akka.pattern.AskableActorSelection;
import akka.util.Timeout;
import scala.concurrent.Await;
import scala.concurrent.Future;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * To keep track of actor identity and would help to crate the actor at a later stage.
 * </p>
 */
public class ActorInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String DEFAULT_ACTOR_PATH = "user";
    public static final int RETRIEVE_ACTOR_WAIT_TIMEOUT = 2;

    private String actorSystemName;

    private String actorPath;

    private String actorName;

    public ActorInfo(String actorName, String actorSystemName) {
        this(DEFAULT_ACTOR_PATH, actorName, actorSystemName);
    }


    public ActorInfo(String actorPath, String actorName, String actorSystemName) {

        this.actorPath = actorPath;
        this.actorName = actorName;
        this.actorSystemName = actorSystemName;
    }

    public String getActorSystemName() {
        return actorSystemName;
    }

    public void setActorSystemName(String actorSystemName) {
        this.actorSystemName = actorSystemName;
    }


    public void setActorPath(String actorPath) {
        this.actorPath = actorPath;
    }

    public void setActorName(String actorName) {
        this.actorName = actorName;
    }

    public String getActorName() {
        return actorName;
    }

    /**
     * Retrieves the actor using the actor system and actor info in this class.
     *
     * @param actorSystem The system to retrieve the actor from.
     * @return Returns the Actor for the actor id.
     */
    public ActorRef actor(ActorSystem actorSystem) {
        ActorSelection actorSelection = actorSelection(actorSystem);

        return retrieveActor(actorSelection);
    }

    private ActorRef retrieveActor(ActorSelection actorSelection) {

        Timeout timeout = new Timeout(RETRIEVE_ACTOR_WAIT_TIMEOUT, TimeUnit.SECONDS);
        AskableActorSelection askableActorSelection = new AskableActorSelection(actorSelection);
        Future<Object> future = askableActorSelection.ask(new Identify(1), timeout);
        ActorIdentity actorIdentity;
        try {
            actorIdentity = (ActorIdentity) Await.result(future, timeout.duration());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to retrieve the requested actor.", e);
        }

        return actorIdentity.getRef();
    }

    private ActorSelection actorSelection(ActorSystem actorSystem) {

        return actorSystem.actorSelection("/" + actorPath + "/" + actorName);

    }


    @Override
    public String toString() {
        return "ActorInfo{" +
                ", actorSystemName='" + actorSystemName + '\'' +
                ", actorPath='" + actorPath + '\'' +
                ", actorName='" + actorName + '\'' +
                '}';
    }
}
