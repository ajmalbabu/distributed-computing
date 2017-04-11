package akka.initializer;

import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.cluster.sharding.ClusterSharding;
import akka.cluster.sharding.ClusterShardingSettings;
import akka.initializer.model.ClusterShardsConfig;
import akka.initializer.model.DefaultMessageExtractor;
import akka.initializer.model.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import static akka.cluster.sharding.ShardRegion.MessageExtractor;

/**
 * Different shard region for different persistence actors must be initialized using this initializer.
 * Initializes based on the configuration provided in the akka.initializer.clusterShards section of yaml file.
 */
@Component
public class ShardRegionInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShardRegionInitializer.class);

    @Autowired
    private ApplicationContext applicationContext;

    /**
     * Message extractor that is used to determine the shardId and entityId from the message and route the message
     * to the appropriate cluster sharded actor instance.
     * <p>
     * Plugin a user provided message extractor by providing a spring bean that implements MessageExtractor
     * and annotate with @Component or @Service annotation and made available in the runtime class-path.
     * If one does not exists then framework chooses the {@link DefaultMessageExtractor} and uses that.
     */
    @Autowired(required = false)
    private MessageExtractor messageExtractor;

    /**
     * Refer to javadoc present in {@link DefaultMessageExtractor#maxNumberOfShards}
     */
    @Value("${akka.initializer.number.of.shards:10}")
    private int maxNumberOfShards;

    @PostConstruct
    public void postConstruct() {

        // If there are no message extractor spring bean in the class-path use the default one.
        if (messageExtractor == null) {
            messageExtractor = new DefaultMessageExtractor(maxNumberOfShards);
        }
    }


    public void initialize(ClusterShardsConfig clusterShardsConfig, ActorSystem actorSystem, ClusterShardingSettings clusterShardingSettings) {

        LOGGER.info("ClusterShardsConfig config {}", clusterShardsConfig);

        for (ClusterShardsConfig.ClusterShard clusterShard : clusterShardsConfig.getClusterShardList()) {

            Parameters parameters = Parameters.instance();

            for (String paramKey : clusterShard.getParameters().keySet()) {
                parameters = parameters.add(paramKey, clusterShard.getParameters().get(paramKey));
            }

            LOGGER.info("Configure cluster shard for {} ", clusterShard);

            ClusterSharding.get(actorSystem).start(clusterShard.getShardRegionName(),
                    Props.create(clusterShard.shardClazz(), applicationContext, parameters),
                    clusterShardingSettings, messageExtractor);
        }
    }
}
