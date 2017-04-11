package akka.initializer.model;


import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class DefaultMessageExtractorTest {

    @Test
    public void normalTest() throws Exception {
        DefaultMessageExtractor messageExtractor = new DefaultMessageExtractor(10);
        messageExtractor.setMaxNumberOfShards(10);

        // Use the shardId provided in the message itself.
        assertThat(messageExtractor.shardId(new HeartBeatMessage("1", "2"))).isEqualTo("1");
        assertThat(messageExtractor.shardId(new HeartBeatMessage("8", "2"))).isEqualTo("8");

        // Determine shard from entityId
        assertThat(messageExtractor.shardId(new HeartBeatMessage(null, "2"))).isEqualTo("0");
        assertThat(messageExtractor.shardId(new HeartBeatMessage(null, "4"))).isEqualTo("2");
        assertThat(messageExtractor.shardId(new HeartBeatMessage(null, "4"))).isEqualTo("2");

        // Assert entityIds.
        assertThat(messageExtractor.entityId(new HeartBeatMessage("1", "2"))).isEqualTo("2");
        assertThat(messageExtractor.entityMessage(new HeartBeatMessage("1", "2")).toString()).contains("shardId='1', entityId='2'");
    }

    @Test
    public void exceptionTest() throws Exception {
        DefaultMessageExtractor messageExtractor = new DefaultMessageExtractor(10);
        assertThatThrownBy(() -> messageExtractor.shardId("test")).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> messageExtractor.entityId("test")).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> messageExtractor.entityMessage("test")).isInstanceOf(IllegalStateException.class);
    }
}
