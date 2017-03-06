package com.cep.bank.service.anomaly.transaction;

import com.cep.bank.service.domain.CustomerTransactionEvent;
import com.cep.bank.service.domain.GenericEvent;
import com.espertech.esper.client.Configuration;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import org.akka.util.lib.AnomalyPublisher;

/**
 * Configures all the types of expected events so that these event types can be refereed in the rule author module.
 */
public class BankAndCreditCardExpectedEventDefinition {

    public static final String CUSTOMER_TRANSACTION_EVENT = "CustomerTransactionEvent";
    public static final String GENERIC_EVENT = "GenericEvent";
    // A unique identifier needed for Esper for each type of rule configuration
    public static final String BANK_AND_CREDIT_CARD_ANOMALY_URI = "BankAndCreditCardAnomalyUri";

    private AnomalyPublisher anomalyPublisher;
    private EPServiceProvider epServiceProvider;

    public BankAndCreditCardExpectedEventDefinition(AnomalyPublisher anomalyPublisher) {

        this.anomalyPublisher = anomalyPublisher;
        configure();
    }

    public void configure() {

        configureExpectedEvents();
        configureEsperRules();

    }

    private void configureExpectedEvents() {

        Configuration configuration = new Configuration();
        configuration.addEventType(CUSTOMER_TRANSACTION_EVENT, CustomerTransactionEvent.class.getName());
        configuration.addEventType(GENERIC_EVENT, GenericEvent.class.getName());

        epServiceProvider = EPServiceProviderManager.getProvider(BANK_AND_CREDIT_CARD_ANOMALY_URI, configuration);
    }

    private void configureEsperRules() {

        BankAndCreditCardRuleAuthor bankAndCreditCardRuleAuthor = new BankAndCreditCardRuleAuthor(epServiceProvider.getEPAdministrator());

        bankAndCreditCardRuleAuthor.addListener(new BankAndCreditCardRuleListener(anomalyPublisher));
    }

    public void sendEvent(CustomerTransactionEvent customerTransactionEvent) {
        epServiceProvider.getEPRuntime().sendEvent(customerTransactionEvent);
    }

    public void sendEvent(GenericEvent genericEvent) {
        epServiceProvider.getEPRuntime().sendEvent(genericEvent);
    }

}
