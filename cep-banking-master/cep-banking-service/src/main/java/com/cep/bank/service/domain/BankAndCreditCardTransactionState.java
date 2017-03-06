package com.cep.bank.service.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores all the transaction events for a customer.
 */
public class BankAndCreditCardTransactionState {

    private List<CustomerTransactionEvent> customerTransactionEvents = new ArrayList<>();
    private List<GenericEvent> genericEvents = new ArrayList<>();

    public void addCustomerTransactionEvent(CustomerTransactionEvent customerTransactionEvent) {
        customerTransactionEvents.add(customerTransactionEvent);
    }

    public void addGenericEvent(GenericEvent genericEvent) {
        genericEvents.add(genericEvent);
    }


    @Override
    public String toString() {
        return "BankAndCreditCardTransactionState{" +
                "customerTransactionEvents=" + customerTransactionEvents +
                ", genericEvents=" + genericEvents +
                '}';
    }
}
