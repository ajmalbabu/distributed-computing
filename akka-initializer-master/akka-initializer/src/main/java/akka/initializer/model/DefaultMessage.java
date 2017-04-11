package akka.initializer.model;


import java.io.Serializable;
import java.time.Instant;
import java.util.Map;

/**
 * A convenient implementation of {@link Message} interface.
 */
public class DefaultMessage implements Message, Serializable {

    private static final long serialVersionUID = 1L;
    public static final String SELF_MESSAGE = "SelfMessage";

    protected static TransactionId transactionId = TransactionId.instance();

    /**
     * Shard this message/entity belongs to. This is optional and framework can derive this from entityId
     * using {@link DefaultMessageExtractor#shardId(Object)} and don't forget to set property
     * {@link DefaultMessageExtractor#maxNumberOfShards} by setting yaml property "akka.initializer.number.of.shards"
     */
    private Object shardId;

    /**
     * Entity id of this message. This is a mandatory field so either set it through constructor or via setter.
     */
    private String entityId;

    /**
     * Time at which the entity was created.
     */
    private Instant createTime;

    /**
     * MDC for traceability.
     */
    private Map<String, Object> mdc;

    /**
     * The sequence number of this message after it gets persisted, frameowrk sets this value transparently.
     * This is useful to delete this message when @Link {@link MessageExpiry} occurs.
     */
    private long sequenceNr;

    /**
     * To help with JSON parsers, which needs default constructor. Not advised to use this,
     * use the constructor that provides the entity-id.
     */
    public DefaultMessage() {
        this(null, null);
    }

    public DefaultMessage(String entityId) {
        this(null, entityId);
    }

    public DefaultMessage(Object shardId, String entityId) {
        this(shardId, entityId, transactionId.currentTransactionIdAsMap(), Instant.now());
    }

    public DefaultMessage(Object shardId, String entityId, Map<String, Object> mdc, Instant createTime) {
        this.shardId = shardId;
        this.entityId = entityId;
        this.mdc = mdc;
        this.createTime = createTime;
    }

    @Override
    public Map<String, Object> getMdc() {
        return mdc;
    }


    @Override
    public Instant getCreateTime() {
        return createTime;
    }


    @Override
    public String getEntityId() {
        return entityId;
    }

    @Override
    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }


    @Override
    public void setSequenceNr(long sequenceNr) {

        this.sequenceNr = sequenceNr;
    }

    @Override
    public long getSequenceNr() {
        return sequenceNr;
    }

    public Object getShardId() {
        return shardId;
    }

    @Override
    public void setShardId(Object shardId) {
        this.shardId = shardId;
    }


    @Override
    public String toString() {
        return "DefaultMessage{" +
                "mdc=" + mdc +
                ", shardId='" + shardId + '\'' +
                ", entityId='" + entityId + '\'' +
                ", createTime=" + createTime +
                '}';
    }


}

