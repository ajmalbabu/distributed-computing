package com.cep.bank.service.anomaly.transaction;

import com.cep.bank.service.domain.BankAndCreditCardTransactionState;
import com.cep.bank.service.domain.CustomerTransactionEvent;
import com.cep.bank.service.domain.GenericEvent;
import akka.initializer.AnomalyPublisher;
import akka.initializer.PersistenceActor;
import akka.initializer.model.Message;
import akka.initializer.model.Parameters;
import org.springframework.context.ApplicationContext;

/**
 * Detects anomaly if 3 credit-card transaction events of credit sub type followed by 2 banking
 * withdrawal transaction event sub type along with a global security event in the last 30 seconds.
 */
public class BankAndCreditCardAnomalyDetector extends PersistenceActor {

    private static final String ANOMALY_PUBLISHER_BEAN = "anomaly.publisher.bean";

    private AnomalyPublisher anomalyPublisher;
    private BankAndCreditCardExpectedEventDefinition bankAndCreditCardExpectedEventDefinition;

    private BankAndCreditCardTransactionState state = new BankAndCreditCardTransactionState();

    public BankAndCreditCardAnomalyDetector(ApplicationContext applicationContext, Parameters parameters) {

        anomalyPublisher = applicationContext.getBean(parameters.getString(ANOMALY_PUBLISHER_BEAN), AnomalyPublisher.class);
        bankAndCreditCardExpectedEventDefinition = new BankAndCreditCardExpectedEventDefinition(anomalyPublisher);

    }

    @Override
    public String persistenceId() {
        return getSelf().path().parent() + "-" + getSelf().path().name();
    }

    @Override
    public void handleReceiveRecover(Message cepMessage) {

        LOGGER.info("Receive recover {} state: {}", this, state);

        if (cepMessage instanceof CustomerTransactionEvent) {

            sendCustomerTransactionEvent((CustomerTransactionEvent) cepMessage);

        } else if (cepMessage instanceof GenericEvent) {

            sendGenericEvent((GenericEvent) cepMessage);

        }
        LOGGER.info("Complete recover of a message. state {}", state);

    }


    @Override
    public void handleReceiveCommand(Message cepMessage) {

        LOGGER.info("Receive command {} state: {}", this, state);

        if (cepMessage instanceof CustomerTransactionEvent) {

            store(cepMessage, this::sendCustomerTransactionEvent);

        } else if (cepMessage instanceof GenericEvent) {

            store(cepMessage, this::sendGenericEvent);

        }

        acknowledge();
    }

    private void sendCustomerTransactionEvent(CustomerTransactionEvent customerTransactionEvent) {
        state.addCustomerTransactionEvent(customerTransactionEvent);
        bankAndCreditCardExpectedEventDefinition.sendEvent(customerTransactionEvent);
    }

    private void sendGenericEvent(GenericEvent genericEvent) {
        state.addGenericEvent(genericEvent);
        bankAndCreditCardExpectedEventDefinition.sendEvent(genericEvent);
    }

}
