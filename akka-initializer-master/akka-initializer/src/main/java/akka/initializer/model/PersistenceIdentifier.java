package akka.initializer.model;

import java.util.List;

import static java.lang.Long.compare;

public class PersistenceIdentifier {

    public final Object shardId;
    public final String entityId;
    public final long sequenceNumber;

    public PersistenceIdentifier(Object shardId, String entityId, long sequenceNumber) {
        this.shardId = shardId;
        this.entityId = entityId;
        this.sequenceNumber = sequenceNumber;
    }

    public static long highestSeqNr(List<PersistenceIdentifier> persistenceIdentifiers) {

        if (persistenceIdentifiers.size() == 0) return Long.MIN_VALUE;

        return persistenceIdentifiers.stream().max((o1, o2) -> compare(o1.sequenceNumber, o2.sequenceNumber)).get().sequenceNumber;

    }

}
