package akka.initializer.model;

import com.google.common.base.Strings;
import org.slf4j.MDC;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Helps to create unique transaction identifier and set into MDC if one does not exists in MDC.
 * Not making it spring bean, else every actor who needs a reference of this should be passed around.
 */
public class TransactionId implements Serializable {

    private static final long serialVersionUID = 1L;

    private static TransactionId instance = new TransactionId();

    // UUID is a good transaction identifier but it uses lots of file space hence converting to a hash with the limitation hash collision.
    public static final int TRANSACTION_ID_LENGTH = 10;
    public static final char PAD_CHAR = '0';
    public static final String TRANSACTION_ID = "transactionId";

    private AtomicInteger transactionId = new AtomicInteger(1);

    private TransactionId() {

    }

    public static TransactionId instance() {
        return instance;
    }

    public Map<String, Object> currentTransactionIdAsMap() {
        if (MDC.get(TRANSACTION_ID) == null) {
            return newTransactionIdAsMap();
        } else {
            return new HashMap<String, Object>() {
                {
                    put(TRANSACTION_ID, MDC.get(TRANSACTION_ID));
                }
            };
        }
    }

    public Map<String, Object> newTransactionIdAsMap() {
        return new HashMap<String, Object>() {
            {
                put(TRANSACTION_ID, generateTransactionId());
            }
        };
    }

    public void setTransactionId(Map<String, Object> transactionIdMap) {
        Map<String, Object> transactionIdMapLocal = transactionIdMap;
        if (transactionIdMapLocal == null) {
            transactionIdMapLocal = newTransactionIdAsMap();
        }
        MDC.put(TRANSACTION_ID, Strings.padStart((String) transactionIdMapLocal.get(TRANSACTION_ID), TRANSACTION_ID_LENGTH, PAD_CHAR));
    }

    public void setTransactionId(String transactionId) {

        // Decided to store the hash-code of the UUID to reduce the size of transaction identifier. also limit to 10 chars. Realizes this could
        // cause few duplicate transaction.
        if (transactionId != null) {
            MDC.put(TRANSACTION_ID, Strings.padStart(transactionId.hashCode() + "", TRANSACTION_ID_LENGTH, PAD_CHAR));
        } else {
            MDC.put(TRANSACTION_ID, Strings.padStart(generateTransactionId(), TRANSACTION_ID_LENGTH, PAD_CHAR));
        }

    }

    private String generateTransactionId() {

        // Race condition edge case of reinitializing multiple times is fine. Performance benefits out weigh synchronization.
        int val = transactionId.incrementAndGet();
        // Integer value got rolled over.
        if (val < 0) {
            transactionId = new AtomicInteger(1);
            val = transactionId.incrementAndGet();
        }
        return val + "";
    }


    public void clear() {
        MDC.clear();
    }

}
