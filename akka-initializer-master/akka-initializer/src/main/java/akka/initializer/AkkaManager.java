package akka.initializer;

import akka.actor.ActorSystem;
import akka.cluster.sharding.ClusterShardingSettings;
import akka.initializer.model.AkkaConfig;
import akka.initializer.model.ClusterShardsConfig;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigRenderOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Abstracts AKKA configuration, setup and initialization.
 */

@Component("akkaManager")
public class AkkaManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(AkkaManager.class);

    @Autowired
    private ClusterShardsConfig clusterShardsConfig;

    @Autowired
    private AkkaConfig akkaConfig;

    @Autowired
    private ShardRegionInitializer shardRegionInitializer;

    private ActorSystem actorSystem;

    private ClusterShardingSettings clusterShardingSettings;


    @PostConstruct
    public void postConstruct() {
        LOGGER.debug("actorSystemName: {}", akkaConfig.getAkkaConfigFileName());
        actorSystem = ActorSystem.create(akkaConfig.getActorSystemName(), createConfig());

        clusterShardingSettings = ClusterShardingSettings.create(actorSystem);
        shardRegionInitializer.initialize(clusterShardsConfig, actorSystem, clusterShardingSettings);
        LOGGER.debug("Created actor system: {}", actorSystem);
    }


    private Config createConfig() {

        Config config = ConfigFactory.empty();

        if (akkaConfig.getAkkaConfigFileName().length() > 0) {
            LOGGER.info("Use the provided akka.initializer.config.file.name = {}", akkaConfig.getAkkaConfigFileName());
            config = config.withFallback(ConfigFactory.load(akkaConfig.getAkkaConfigFileName()));
        }

        LOGGER.info("akka.initializer config {}", config.root().render(ConfigRenderOptions.defaults().setJson(true)));

        return config;
    }

    public String getActorSystemName() {
        return akkaConfig.getActorSystemName();
    }

    public ActorSystem getActorSystem() {
        return actorSystem;
    }

    public ClusterShardingSettings getClusterShardingSettings() {
        return clusterShardingSettings;
    }


}
