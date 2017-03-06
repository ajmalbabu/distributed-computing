package com.cep.bank.service.anomaly.transaction;

import com.espertech.esper.client.EPAdministrator;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.UpdateListener;

/**
 * Authors specific Esper rules to be applied across the available expected events.
 */
public class BankAndCreditCardRuleAuthor {

    private EPStatement statement;

    public BankAndCreditCardRuleAuthor(EPAdministrator admin) {

        String stmt = "select cc[0].customerName as customerName from pattern " +
                "[every (GenericEvent -> [2] cc=CustomerTransactionEvent(transactionSubType='Credit') -> " +
                "[3] bk=CustomerTransactionEvent(transactionSubType='Withdraw'))  " +
                "where timer:within(10000 milliseconds)]"; // Tumbling window

        statement = admin.createEPL(stmt);
    }

    public void addListener(UpdateListener listener) {
        statement.addListener(listener);
    }
}
