package akka.sdk;

import akka.actor.ActorRef;
import akka.cluster.sharding.ClusterSharding;
import akka.routing.FromConfig;
import akka.sdk.AnomalyPublisher.Anomaly;
import akka.sdk.AnomalyPublisher.ObservableAnomalyPublisher;
import akka.sdk.model.ResponseMessage;
import akka.sdk.model.TransactionId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Read javadoc from {@link SampleAnomalyDetector}
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = SpringConfig.class)
public class SampleAnomalyCancelTest {


    private TransactionId transactionId = TransactionId.instance();

    @Autowired
    private ObservableAnomalyPublisher observableAnomalyPublisher;

    @Autowired
    private AkkaManager akkaManager;

    @Autowired
    private SpringExtension springExtension;

    @Profile("test")
    @Configuration
    public static class FlightDelayTestMocks {

    }

    @Test
    public void anomalyGenerationShouldBeCancelledOut() throws Exception {

        // Given
        SampleDelayEvent sampleDelayEvent1 = new SampleDelayEvent(1, "1", "Delay1");
        SampleDelayEvent sampleDelayEvent2 = new SampleDelayEvent(1, "1", "Delay2");
        SampleCancelEvent sampleCancelEvent = new SampleCancelEvent(1, "1");
        ActorRef actorRef = ClusterSharding.get(akkaManager.getActorSystem()).shardRegion("SampleShardRegion");

        // When
        ActorRef responseActor1 = springExtension.actorOf(akkaManager.getActorSystem(), ResponseHandlerActor.RESPONSE_HANDLER_ACTOR,
                new FromConfig(), ResponseHandlerActor.RESPONSE_HANDLER_ACTOR_DISPATCHER);
        ActorRef responseActor2 = springExtension.actorOf(akkaManager.getActorSystem(), ResponseHandlerActor.RESPONSE_HANDLER_ACTOR,
                new FromConfig(), ResponseHandlerActor.RESPONSE_HANDLER_ACTOR_DISPATCHER);
        ActorRef responseActor3 = springExtension.actorOf(akkaManager.getActorSystem(), ResponseHandlerActor.RESPONSE_HANDLER_ACTOR,
                new FromConfig(), ResponseHandlerActor.RESPONSE_HANDLER_ACTOR_DISPATCHER);

        actorRef.tell(sampleDelayEvent1, responseActor1);
        ResponseMessage responseMessage1 = ResponseHandlerActor.blockForResponse(responseActor1, 250, 5000);
        actorRef.tell(sampleDelayEvent2, responseActor2);
        ResponseMessage responseMessage2 = ResponseHandlerActor.blockForResponse(responseActor2, 250, 5000);
        actorRef.tell(sampleCancelEvent, responseActor3);
        ResponseMessage responseMessage3 = ResponseHandlerActor.blockForResponse(responseActor3, 250, 5000);
        List<Anomaly> anomalies = observableAnomalyPublisher.blockForAnomalies(250, 4000);

        // Then
        assertThat(responseMessage1.getResponseType()).isEqualTo(ResponseMessage.ResponseType.MessageProcessed);
        assertThat(responseMessage2.getResponseType()).isEqualTo(ResponseMessage.ResponseType.MessageProcessed);
        assertThat(responseMessage3.getResponseType()).isEqualTo(ResponseMessage.ResponseType.MessageProcessed);

        assertThat(anomalies.size()).isEqualTo(0);

    }


}
