package akka.initializer.model;

import akka.cluster.sharding.ShardRegion;

/**
 * A class used internally by the framework to route the message to correct shard.
 * All messages flowing through the system are expected to implement Message interface
 * and the extractor uses shardId (optional) and entityId form the Message interface.
 * If shardId is not present this class derives a shardId using entityId
 * {@link DefaultMessageExtractor#shardId(Object)}
 */
public class DefaultMessageExtractor implements ShardRegion.MessageExtractor {


    /**
     * As a rule of thumb, the number of shards should be a factor ten greater than the planned maximum number
     * of cluster nodes. Refer to http://doc.akka.io/docs/akka/2.4.16/scala/cluster-sharding.html section
     * where it talks about ShardRegion.HashCodeMessageExtractor.
     * So say if you have a 15 node cluster then the akka.initializer.number.of.shards should be 15 * 10 = 150.
     * The entities (Aggregate/Persistence) actors in the system will be shared by this 150 shards.
     */
    private int maxNumberOfShards;

    public DefaultMessageExtractor(int maxNumberOfShards) {
        this.maxNumberOfShards = maxNumberOfShards;
    }

    /**
     * If shardId is present in the Message, then it returns that shardId, if not it derives a scalable
     * shardId using the entityId of the Message using and algorithm similar to that of
     * {@link akka.cluster.sharding.ShardRegion.HashCodeMessageExtractor}
     *
     * @param message Message.
     * @return shardId
     */
    @Override
    public String shardId(Object message) {

        if (message instanceof Message) {
            Message message1 = (Message) message;
            // If the shard is already calculated and set in the message then use it.
            if (message1.getShardId() != null) {
                return message1.getShardId().toString();
            } else {
                // This is similar to the implementation provided in AKKA ShardRegion.HashCodeMessageExtractor
                return String.valueOf(Math.abs(((Message) message).getEntityId().hashCode()) % maxNumberOfShards);
            }
        } else {
            return handleError(message);
        }
    }

    @Override
    public String entityId(Object message) {
        if (message instanceof Message)
            return ((Message) message).getEntityId();
        else
            return handleError(message);
    }

    @Override
    public Object entityMessage(Object message) {
        if (message instanceof Message)
            return message;
        else
            return handleError(message);
    }

    private String handleError(Object message) {
        throw new IllegalStateException("Wrong message type: " + message);
    }

    public void setMaxNumberOfShards(int maxNumberOfShards) {
        this.maxNumberOfShards = maxNumberOfShards;
    }
}
