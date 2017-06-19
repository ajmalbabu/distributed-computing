package akka.initializer;

import akka.actor.ActorRef;
import akka.cluster.sharding.ClusterSharding;
import akka.routing.FromConfig;
import akka.initializer.model.HeartBeatMessage;
import akka.initializer.model.ResponseMessage;
import akka.initializer.model.StopMessage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * These test cases are time sensitive and may (extremely rarely) fail if run with test suite because of GC pause etc.
 * Run them as part of separate suite or increase the expiry delay to higher number and adjust test cases delays accordingly.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = SpringConfig.class)
public class ActorRecoveryTest {

    @Autowired
    private AkkaManager akkaManager;

    @Autowired
    private SpringExtension springExtension;

    @Test
    public void actorShouldGetStoppedAndRecoveredBack() throws Exception {

        // Given - Setup the actor and send a message to the actor.
        ActorRef actorWithTimeToLive = ClusterSharding.get(akkaManager.getActorSystem()).shardRegion("ActorRemovalShardRegion");

        ActorRef responseActor1 = springExtension.actorOf(akkaManager.getActorSystem(), ResponseHandlerActor.RESPONSE_HANDLER_ACTOR,
                new FromConfig(), ResponseHandlerActor.RESPONSE_HANDLER_ACTOR_DISPATCHER);

        MessageExpiryEvent messageExpiryEvent = new MessageExpiryEvent(1, "4", 101);
        actorWithTimeToLive.tell(messageExpiryEvent, responseActor1);
        ResponseMessage responseMessage1 = ResponseHandlerActor.blockForResponse(responseActor1, 10, 5000);

        assertThat(responseMessage1.getResponseType()).isEqualTo(ResponseMessage.ResponseType.MessageProcessed);

        ActorRef responseActor2 = springExtension.actorOf(akkaManager.getActorSystem(), ResponseHandlerActor.RESPONSE_HANDLER_ACTOR,
                new FromConfig(), ResponseHandlerActor.RESPONSE_HANDLER_ACTOR_DISPATCHER);
        actorWithTimeToLive.tell(new MessageExpiryDetector.StateEventCountReq(1, "4"), responseActor2);
        ResponseMessage responseMessage2 = ResponseHandlerActor.blockForResponse(responseActor2, 10, 5000);
        assertThat(((MessageExpiryDetector.StateEventCountResp) responseMessage2.getMessage()).messageExpiryEvents.size()).isEqualTo(1);
        assertThat(((MessageExpiryDetector.StateEventCountResp) responseMessage2.getMessage()).messageExpiryEvents.get(0).getEventId()).isEqualTo(101);

        // When
        // Stop the actor. Don't wait to reach time-to-live time and automatically remove it, instead forcefully stop.
        actorWithTimeToLive.tell(new StopMessage(1, "4"), ActorRef.noSender());

        // Then
        // Actor should come back with state
        Thread.sleep(10);
        actorWithTimeToLive = ClusterSharding.get(akkaManager.getActorSystem()).shardRegion("ActorRemovalShardRegion");
        // Send the heart beat to revive the actor.
        ActorRef responseActor3 = springExtension.actorOf(akkaManager.getActorSystem(), ResponseHandlerActor.RESPONSE_HANDLER_ACTOR,
                new FromConfig(), ResponseHandlerActor.RESPONSE_HANDLER_ACTOR_DISPATCHER);
        actorWithTimeToLive.tell(new HeartBeatMessage(1, "4"), responseActor3);
        ResponseMessage responseMessage3 = ResponseHandlerActor.blockForResponse(responseActor3, 10, 2000);
        assertThat(responseMessage3.getResponseType()).isEqualTo(ResponseMessage.ResponseType.MessageProcessed);

        // Give time so that actor gets Cleaned-up and Stopped. actor.TTL.seconds = 2 in application.yaml
        Thread.sleep(2100);

        // Query the actor again, now it gets restarted, but state will be empty.
        ActorRef responseActor4 = springExtension.actorOf(akkaManager.getActorSystem(), ResponseHandlerActor.RESPONSE_HANDLER_ACTOR,
                new FromConfig(), ResponseHandlerActor.RESPONSE_HANDLER_ACTOR_DISPATCHER);
        actorWithTimeToLive.tell(new MessageExpiryDetector.StateEventCountReq(1, "4"), responseActor4);
        ResponseMessage responseMessage4 = ResponseHandlerActor.blockForResponse(responseActor4, 10, 5000);
        assertThat(((MessageExpiryDetector.StateEventCountResp) responseMessage4.getMessage()).messageExpiryEvents.size()).isEqualTo(0);

    }

}
