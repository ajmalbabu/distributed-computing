package org.akka.util.lib;

import org.akka.util.lib.AnomalyPublisher.Anomaly;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AnomalyTest {


    @Test
    public void anomalyTest() throws Exception {
        Anomaly<String> anomaly = new Anomaly<>("Test Anomaly");
        assertThat(anomaly.getMessage()).isEqualTo("Test Anomaly");
        assertThat(anomaly.toString()).contains("Test Anomaly");

    }
}
