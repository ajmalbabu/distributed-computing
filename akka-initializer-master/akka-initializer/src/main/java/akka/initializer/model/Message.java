package akka.initializer.model;

import java.time.Instant;
import java.util.Map;
import java.util.function.Consumer;

public interface Message {

    /**
     * The sequence number of this message after it gets persisted, framework sets this value transparently.
     * This is useful to delete this message when @Link {@link MessageExpiry} occurs.
     */
    long getSequenceNr();

    void setSequenceNr(long sequenceNr);

    /**
     * If you decide to provide your own shardId then consider this rule of thumb, the number of shards should be a
     * factor ten greater than the planned maximum number of cluster nodes. Refer to
     * http://doc.akka.io/docs/akka/2.4.16/scala/cluster-sharding.html section where it talks about
     * ShardRegion.HashCodeMessageExtractor.
     * <p>
     * If not, simply set the entity-id and then this base framework determines the shardId automatically for you
     * refer to {@link DefaultMessageExtractor#shardId(Object)} and don't forget to set property
     * {@link DefaultMessageExtractor#maxNumberOfShards} by setting yaml property "akka.initializer.number.of.shards"
     */
    Object getShardId();

    void setShardId(Object shardId);

    /**
     * This is a key field and should be set by application, it is used to uniquely identify the Aggregate actor where
     * this message will be send. This field is mandatory so set it using setter.
     */
    String getEntityId();

    void setEntityId(String entityId);

    Map<String, Object> getMdc();

    Instant getCreateTime();

    default boolean isAfter(Instant now) {
        return getCreateTime().isAfter(now);
    }

    default boolean isBefore(Instant now) {
        return getCreateTime().isBefore(now);
    }

    /**
     * If the Create-Time is before (now - provided timeWindow)
     */
    default boolean isBefore(Time timeWindow) {
        return isBefore(currentTime().minusMillis(timeWindow.toMillis()));
    }

    default Instant currentTime() {
        return Instant.now();
    }

    static Consumer<Message> noFunction() {
        return null;
    }
}

