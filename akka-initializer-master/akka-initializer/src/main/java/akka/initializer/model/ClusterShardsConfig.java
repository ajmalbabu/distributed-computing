package akka.initializer.model;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "akka.initializer.cluster.shards.config")
public class ClusterShardsConfig {

    private List<ClusterShard> clusterShardList = new ArrayList<>();

    public List<ClusterShard> getClusterShardList() {
        return clusterShardList;
    }

    public void setClusterShardList(List<ClusterShard> clusterShardList) {
        this.clusterShardList = clusterShardList;
    }

    @Override
    public String toString() {
        return "ClusterShardsConfig{" +
                "clusterShardList=" + clusterShardList +
                '}';
    }

    public static class ClusterShard {

        private String shardRegionName;
        private String shardActorClass;
        private Map<String, String> parameters = new HashMap<>();


        public String getShardRegionName() {
            return shardRegionName;
        }

        public Class<?> shardClazz() {
            try {
                return Class.forName(shardActorClass);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        public void setShardRegionName(String shardRegionName) {
            this.shardRegionName = shardRegionName;
        }

        public String getShardActorClass() {
            return shardActorClass;
        }

        public Class getShardActorClazz() throws ClassNotFoundException {
            return Class.forName(shardActorClass);
        }

        public void setShardActorClass(String shardActorClass) {
            this.shardActorClass = shardActorClass;
        }

        public Map<String, String> getParameters() {
            return parameters;
        }

        public void setParameters(Map<String, String> parameters) {
            this.parameters = parameters;
        }

        @Override
        public String toString() {
            return "ClusterShard{" +
                    "shardRegionName='" + shardRegionName + '\'' +
                    ", shardActorClass='" + shardActorClass + '\'' +
                    ", parameters=" + parameters +
                    '}';
        }
    }
}
