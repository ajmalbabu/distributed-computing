package org.akka.util.lib;

import akka.actor.ActorRef;
import akka.cluster.sharding.ClusterSharding;
import akka.routing.FromConfig;
import org.akka.util.lib.AnomalyPublisher.Anomaly;
import org.akka.util.lib.AnomalyPublisher.ObservableAnomalyPublisher;
import org.akka.util.lib.model.ResponseMessage;
import org.akka.util.lib.model.TransactionId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static org.akka.util.lib.ResponseHandlerActor.RESPONSE_HANDLER_ACTOR;
import static org.akka.util.lib.ResponseHandlerActor.RESPONSE_HANDLER_ACTOR_DISPATCHER;
import static org.akka.util.lib.ResponseHandlerActor.blockForResponse;
import static org.akka.util.lib.model.ResponseMessage.ResponseType.*;
import static org.assertj.core.api.Assertions.assertThat;


/**
 * Read javadoc from {@link SampleAnomalyDetector}
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = SpringConfig.class)
public class SampleAnomalyTest {

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
    public void shouldGenerateFlightDelayAnomalyAfterSomePause() throws Exception {

        // Given
        SampleDelayEvent sampleDelayEvent1 = new SampleDelayEvent(2, "2", "Delay1");
        SampleDelayEvent sampleDelayEvent2 = new SampleDelayEvent(2, "2", "Delay2");
        ActorRef actorRef = ClusterSharding.get(akkaManager.getActorSystem()).shardRegion("SampleShardRegion");

        // When
        ActorRef responseActor1 = springExtension.actorOf(akkaManager.getActorSystem(), RESPONSE_HANDLER_ACTOR,
                new FromConfig(), RESPONSE_HANDLER_ACTOR_DISPATCHER);
        ActorRef responseActor2 = springExtension.actorOf(akkaManager.getActorSystem(), RESPONSE_HANDLER_ACTOR,
                new FromConfig(), RESPONSE_HANDLER_ACTOR_DISPATCHER);

        actorRef.tell(sampleDelayEvent1, responseActor1);
        ResponseMessage responseMessage1 = blockForResponse(responseActor1, 250, 5000);
        actorRef.tell(sampleDelayEvent2, responseActor2);
        ResponseMessage responseMessage2 = blockForResponse(responseActor2, 250, 5000);
        List<Anomaly> anomalies = observableAnomalyPublisher.blockForAnomalies(250, 5000);

        // Then
        assertThat(responseMessage1.getResponseType()).isEqualTo(MessageProcessed);
        assertThat(responseMessage2.getResponseType()).isEqualTo(MessageProcessed);
        assertThat(anomalies.size()).isEqualTo(1);

    }


}
