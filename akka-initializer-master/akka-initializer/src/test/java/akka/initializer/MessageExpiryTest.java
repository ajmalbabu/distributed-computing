package akka.initializer;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import akka.actor.ActorRef;
import akka.cluster.sharding.ClusterSharding;
import akka.initializer.model.ResponseMessage;
import akka.routing.FromConfig;

/**
 * These test cases are time sensitive and may (extremely rarely) if run with test suite because of GC pause etc.
 * Run them as part of separate suite or increase the expiry delay actor.state.message.expiry.millis to higher
 * number and adjust test cases delays accordingly.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = SpringConfig.class)
public class MessageExpiryTest {

    @Autowired
    private AkkaManager akkaManager;

    @Autowired
    private SpringExtension springExtension;

    @Test
    public void shouldExpireMessagesAtProvidedInterval() throws Exception {

        // Given
        ActorRef messageExpiryDetector = ClusterSharding.get(akkaManager.getActorSystem()).shardRegion("MessageExpiryShardRegion");

        // When
        ActorRef responseActor1 = springExtension.actorOf(akkaManager.getActorSystem(), ResponseHandlerActor.RESPONSE_HANDLER_ACTOR,
                new FromConfig(), ResponseHandlerActor.RESPONSE_HANDLER_ACTOR_DISPATCHER);

        MessageExpiryEvent messageExpiryEvent = new MessageExpiryEvent(1, "1", 1);
        messageExpiryDetector.tell(messageExpiryEvent, responseActor1);
        ResponseMessage responseMessage1 = ResponseHandlerActor.blockForResponse(responseActor1, 10, 5000);

        // Then
        assertThat(responseMessage1.getResponseType()).isEqualTo(ResponseMessage.ResponseType.MessageProcessed);

        // Message should not have expired.
        ActorRef responseActor2 = springExtension.actorOf(akkaManager.getActorSystem(), ResponseHandlerActor.RESPONSE_HANDLER_ACTOR,
                new FromConfig(), ResponseHandlerActor.RESPONSE_HANDLER_ACTOR_DISPATCHER);
        messageExpiryDetector.tell(new MessageExpiryDetector.StateEventCountReq(1, "1"), responseActor2);
        ResponseMessage responseMessage2 = ResponseHandlerActor.blockForResponse(responseActor2, 10, 5000);
        assertThat(((MessageExpiryDetector.StateEventCountResp) responseMessage2.getMessage()).messageExpiryEvents.size()).isEqualTo(1);
        assertThat(((MessageExpiryDetector.StateEventCountResp) responseMessage2.getMessage()).messageExpiryEvents).extracting("eventId").contains(1);

        // Give sometime (more than application.yaml => actor.state.message.expiry.millis) so that message expires
        Thread.sleep(301);
        ActorRef responseActor3 = springExtension.actorOf(akkaManager.getActorSystem(), ResponseHandlerActor.RESPONSE_HANDLER_ACTOR,
                new FromConfig(), ResponseHandlerActor.RESPONSE_HANDLER_ACTOR_DISPATCHER);
        messageExpiryDetector.tell(new MessageExpiryDetector.StateEventCountReq(1, "1"), responseActor3);
        ResponseMessage responseMessage3 = ResponseHandlerActor.blockForResponse(responseActor3, 10, 200);
        assertThat(((MessageExpiryDetector.StateEventCountResp) responseMessage3.getMessage()).messageExpiryEvents.size()).isEqualTo(0);

    }


    @Test
    public void shouldExpireEachMessagesAtProvidedInterval() throws Exception {

        // Given
        ActorRef messageExpiryDetector = ClusterSharding.get(akkaManager.getActorSystem()).shardRegion("MessageExpiryShardRegion");

        // When
        ActorRef responseActor1 = springExtension.actorOf(akkaManager.getActorSystem(), ResponseHandlerActor.RESPONSE_HANDLER_ACTOR,
                new FromConfig(), ResponseHandlerActor.RESPONSE_HANDLER_ACTOR_DISPATCHER);
        messageExpiryDetector.tell(new MessageExpiryEvent(1, "2", 1), responseActor1);
        ResponseMessage responseMessage1 = ResponseHandlerActor.blockForResponse(responseActor1, 10, 5000);
        assertThat(responseMessage1.getResponseType()).isEqualTo(ResponseMessage.ResponseType.MessageProcessed);

        // Keep a delay to send first message.
        Thread.sleep(100);
        ActorRef responseActor2 = springExtension.actorOf(akkaManager.getActorSystem(), ResponseHandlerActor.RESPONSE_HANDLER_ACTOR,
                new FromConfig(), ResponseHandlerActor.RESPONSE_HANDLER_ACTOR_DISPATCHER);
        messageExpiryDetector.tell(new MessageExpiryEvent(1, "2", 2), responseActor2);
        ResponseMessage responseMessage2 = ResponseHandlerActor.blockForResponse(responseActor2, 10, 5000);
        assertThat(responseMessage2.getResponseType()).isEqualTo(ResponseMessage.ResponseType.MessageProcessed);

        // Then => Message should not have expired.
        ActorRef responseActor3 = springExtension.actorOf(akkaManager.getActorSystem(), ResponseHandlerActor.RESPONSE_HANDLER_ACTOR,
                new FromConfig(), ResponseHandlerActor.RESPONSE_HANDLER_ACTOR_DISPATCHER);
        messageExpiryDetector.tell(new MessageExpiryDetector.StateEventCountReq(1, "2"), responseActor3);
        ResponseMessage responseMessage3 = ResponseHandlerActor.blockForResponse(responseActor3, 10, 5000);
        assertThat(((MessageExpiryDetector.StateEventCountResp) responseMessage3.getMessage()).messageExpiryEvents.size()).isEqualTo(2);
        assertThat(((MessageExpiryDetector.StateEventCountResp) responseMessage3.getMessage()).messageExpiryEvents).extracting("eventId").contains(1, 2);

        // Give sometime (more than application.yaml => actor.state.message.expiry.millis) so that first message expires
        Thread.sleep(101);
        ActorRef responseActor4 = springExtension.actorOf(akkaManager.getActorSystem(), ResponseHandlerActor.RESPONSE_HANDLER_ACTOR,
                new FromConfig(), ResponseHandlerActor.RESPONSE_HANDLER_ACTOR_DISPATCHER);
        messageExpiryDetector.tell(new MessageExpiryDetector.StateEventCountReq(1, "2"), responseActor4);
        ResponseMessage responseMessage4 = ResponseHandlerActor.blockForResponse(responseActor4, 10, 250);
        assertThat(((MessageExpiryDetector.StateEventCountResp) responseMessage4.getMessage()).messageExpiryEvents.size()).isEqualTo(1);
        assertThat(((MessageExpiryDetector.StateEventCountResp) responseMessage3.getMessage()).messageExpiryEvents).extracting("eventId").contains(2);

        // Give some more time (more than application.yaml => actor.state.message.expiry.millis) so that second message expires
        Thread.sleep(201);
        ActorRef responseActor5 = springExtension.actorOf(akkaManager.getActorSystem(), ResponseHandlerActor.RESPONSE_HANDLER_ACTOR,
                new FromConfig(), ResponseHandlerActor.RESPONSE_HANDLER_ACTOR_DISPATCHER);
        messageExpiryDetector.tell(new MessageExpiryDetector.StateEventCountReq(1, "2"), responseActor5);
        ResponseMessage responseMessage5 = ResponseHandlerActor.blockForResponse(responseActor5, 10, 250);
        assertThat(((MessageExpiryDetector.StateEventCountResp) responseMessage5.getMessage()).messageExpiryEvents.size()).isEqualTo(0);

    }

}
