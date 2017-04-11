package akka.initializer.support;

import akka.actor.*;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent.*;
import akka.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.FiniteDuration;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import static akka.pattern.Patterns.ask;
import static java.time.Instant.now;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Help the test cases to form a test cluster and detect the formation.
 */
public class ClusterFormationDetectorActor extends UntypedActor {

    protected static final Logger log = LoggerFactory.getLogger(ClusterFormationDetectorActor.class);


    private int numberOfNodes;
    private final Set<Address> nodes = new HashSet<Address>();

    Cluster cluster = Cluster.get(getContext().system());

    public ClusterFormationDetectorActor(int numberOfNodes) {

        this.numberOfNodes = numberOfNodes;
    }


    @Override
    public void preStart() {

        // Subscribe to cluster changes, MemberEvent
        cluster.subscribe(getSelf(), MemberEvent.class, ReachabilityEvent.class);
    }


    @Override
    public void postStop() {
        cluster.unsubscribe(getSelf());
    }

    @Override
    public void onReceive(Object message) {

        if (message instanceof ClusterReadyRequest) {

            if (nodes.size() >= numberOfNodes) {
                getSender().tell(new ClusterReadyResponse(true), null);
            } else {
                getSender().tell(new ClusterReadyResponse(false), null);
            }

        } else if (message instanceof MemberUp) {

            MemberUp mUp = (MemberUp) message;
            if (mUp.member().hasRole("")) {
                nodes.add(mUp.member().address());
            }
        } else if (message instanceof MemberEvent) {

            MemberEvent other = (MemberEvent) message;
            nodes.remove(other.member().address());

        } else if (message instanceof UnreachableMember) {

            UnreachableMember unreachable = (UnreachableMember) message;
            nodes.remove(unreachable.member().address());

        } else if (message instanceof ReachableMember) {

            ReachableMember reachable = (ReachableMember) message;
            if (reachable.member().hasRole("compute")) {
                nodes.add(reachable.member().address());
            }

        } else {
            unhandled(message);
        }
    }

    public static class ClusterReadyRequest {

    }

    public static class ClusterReadyResponse {

        private boolean clusterReady;

        public ClusterReadyResponse(boolean clusterReady) {
            this.clusterReady = clusterReady;
        }

        public boolean isClusterReady() {
            return clusterReady;
        }
    }


    public static void waitForAllNodesToJoinCluster(ActorSystem actorSystem, int numberOfNodes) throws Exception {

        ActorRef clusterFormationDetectorActor = actorSystem.actorOf(Props.create(ClusterFormationDetectorActor.class, numberOfNodes));

        Instant startTime = now();

        boolean waitForClusterReady = false;

        log.info("Waiting for cluster to start....");

        while (!waitForClusterReady) {

            FiniteDuration duration = FiniteDuration.create(10, SECONDS);

            Future<Object> futureResult = ask(clusterFormationDetectorActor, new ClusterFormationDetectorActor.ClusterReadyRequest(), Timeout.durationToTimeout(duration));

            ClusterFormationDetectorActor.ClusterReadyResponse clusterReadyResponse = (ClusterFormationDetectorActor.ClusterReadyResponse) Await.result(futureResult, duration);

            if (clusterReadyResponse.isClusterReady()) {
                log.info("Yeahhh....CLUSTER STARTED...");
                waitForClusterReady = true;
            } else {
                log.info("Cluster not ready yet.");
                Thread.sleep(100);
            }

            if (Duration.between(now(), startTime).getSeconds() > 20) {
                throw new IllegalStateException("Cluster is not starting in 15 seconds, giving up...");
            }

        }
    }

}
