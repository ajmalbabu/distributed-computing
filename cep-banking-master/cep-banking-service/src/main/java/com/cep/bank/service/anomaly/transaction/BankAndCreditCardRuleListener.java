package com.cep.bank.service.anomaly.transaction;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;
import akka.sdk.AnomalyPublisher;
import akka.sdk.AnomalyPublisher.Anomaly;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This listener gets invoked when a rule condition is met. This class can notify external system etc.
 */
public class BankAndCreditCardRuleListener implements UpdateListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(BankAndCreditCardRuleListener.class);
    private AnomalyPublisher anomalyPublisher;

    public BankAndCreditCardRuleListener(AnomalyPublisher anomalyPublisher) {

        this.anomalyPublisher = anomalyPublisher;
    }

    public void update(EventBean[] newEvents, EventBean[] oldEvents) {

        LOGGER.info("Found bank transaction anomaly with event: {}", newEvents.length);

        if (newEvents.length > 0) {

            Anomaly<String> anomaly = new Anomaly<>("Found bank transaction anomaly with customer name #: " + newEvents[0].get("customerName").toString());
            anomalyPublisher.publish(anomaly);
        }

    }
}
