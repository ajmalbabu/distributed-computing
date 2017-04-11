package com.cep.bank.service;

import akka.actor.ActorRef;
import akka.routing.FromConfig;
import com.cep.bank.service.domain.CustomerTransactionEvent;
import com.cep.bank.service.domain.GenericEvent;
import akka.initializer.AkkaManager;
import akka.initializer.AnomalyPublisher.Anomaly;
import akka.initializer.AnomalyPublisher.ObservableAnomalyPublisher;
import akka.initializer.SpringExtension;
import akka.initializer.model.ResponseMessage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static akka.initializer.ResponseHandlerActor.*;
import static akka.initializer.model.ResponseMessage.ResponseType.MessageProcessed;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = SpringConfig.class)
public class BankAnomalyServiceTest {

    @Autowired
    private BankAnomalyService bankAnomalyService;

    @Autowired
    private ObservableAnomalyPublisher observableAnomalyPublisher;

    @Autowired
    private AkkaManager akkaManager;

    @Autowired
    private SpringExtension springExtension;

    @Test
    public void shouldGenerateAnomalyOnSecurityEventFolllowedByTwoCreditCardAndThreeBankEvent() throws Exception {

        // Given
        GenericEvent genericEvent = new GenericEvent();
        genericEvent.setEntityId("Tester");

        CustomerTransactionEvent creditTransactionEvent1 = CustomerTransactionEvent.createEvent("Tester", "CreditCard", "Credit", 10, "USD");
        CustomerTransactionEvent creditTransactionEvent2 = CustomerTransactionEvent.createEvent("Tester", "CreditCard", "Credit", 15, "USD");

        CustomerTransactionEvent bankTransactionEvent1 = CustomerTransactionEvent.createEvent("Tester", "Bank", "Withdraw", 25, "USD");
        CustomerTransactionEvent bankTransactionEvent2 = CustomerTransactionEvent.createEvent("Tester", "Bank", "Withdraw", 35, "USD");
        CustomerTransactionEvent bankTransactionEvent3 = CustomerTransactionEvent.createEvent("Tester", "Bank", "Withdraw", 45, "USD");

        // When
        ActorRef responseActor = springExtension.actorOf(akkaManager.getActorSystem(), RESPONSE_HANDLER_ACTOR,
                new FromConfig(), RESPONSE_HANDLER_ACTOR_DISPATCHER);

        // Send security event.
        bankAnomalyService.handleGenericEvent(genericEvent, responseActor);

        // Send credit card transaction events.
        bankAnomalyService.handleCustomerTransactionEvent(creditTransactionEvent1, responseActor);
        bankAnomalyService.handleCustomerTransactionEvent(creditTransactionEvent2, responseActor);

        // Send bank withdraw transaction events.
        bankAnomalyService.handleCustomerTransactionEvent(bankTransactionEvent1, responseActor);
        bankAnomalyService.handleCustomerTransactionEvent(bankTransactionEvent2, responseActor);
        bankAnomalyService.handleCustomerTransactionEvent(bankTransactionEvent3, responseActor);


        ResponseMessage responseMessage = blockForResponse(responseActor, 250, 5000);
        List<Anomaly> anomalies = observableAnomalyPublisher.blockForAnomalies(250, 5000);

        // Then
        assertThat(responseMessage.getResponseType()).isEqualTo(MessageProcessed);
        assertThat(anomalies.size()).isEqualTo(1);
        assertThat(anomalies.get(0).getMessage().toString().contains("Found bank transaction anomaly with customer name #: Tester"));

    }
}
