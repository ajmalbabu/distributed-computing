package akka.initializer.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


public class TestCluster {

    protected static final Logger log = LoggerFactory.getLogger(TestCluster.class);

    private List<SpringContextSupport> springContextSupportForClusterNodes = new ArrayList<>();

    private String[] configFile;
    private String[] ruleConfig;
    private int numberOfWorkerNodes = 0;


    public TestCluster(String[] configFileArg, String[] ruleConfigArg, int numberOfWorkerNodes) {

        this.configFile = configFileArg;
        this.ruleConfig = ruleConfigArg;
        this.numberOfWorkerNodes = numberOfWorkerNodes;
    }

    public void startCluster() throws Exception {

        // Start seed nodes

        int seed1Port = UnitTestPortManager.instance().getNextPort();
        int seed2Port = UnitTestPortManager.instance().getNextPort();

        startNode(seed1Port, seed1Port, seed2Port);
        startNode(seed2Port, seed1Port, seed2Port);

        // Start worker nodes.

        for (int i = 0; i < numberOfWorkerNodes; i++) {
            startNode(UnitTestPortManager.instance().getNextPort(), seed1Port, seed2Port);
        }

//        ClusterFormationDetectorActor.waitForAllNodesToJoinCluster(getStellarFromAnyClusterNode().getClusteredActorSystem(), 2 + numberOfWorkerNodes);


    }

    private void startNode(int clusterInstancePort, int seed1Port, int seed2Port) {

        String[] packages = {
                "akka.initializer"
        };

        SpringContextSupport springContextSupport = SpringContextSupport.instance().build(packages);

        springContextSupportForClusterNodes.add(springContextSupport);

    }

    public void shutDownCluster() {
        springContextSupportForClusterNodes.stream().forEach(e -> e.close());
    }


//    public Stellar getStellarFromAnyClusterNode() {
//
//        return springContextSupportForClusterNodes.get(ThreadLocalRandom.current().nextInt(springContextSupportForClusterNodes.size())).getStellar();
//    }

    public static TestCluster createTwoSeedCluster(String configFileArg, String ruleConfigArg, int numberOfWorkerNodes) {

        String[] configFile = {"stellar.configuration", configFileArg};
        String[] ruleConfig = {"stellar.rule.configuration", ""};
        if (ruleConfigArg != null) {
            ruleConfig[1] = ruleConfigArg;
        }

        return new TestCluster(configFile, ruleConfig, numberOfWorkerNodes);

    }


}
