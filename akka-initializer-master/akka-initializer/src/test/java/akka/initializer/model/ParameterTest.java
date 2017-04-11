package akka.initializer.model;


import akka.actor.ActorRef;
import org.junit.Test;
import org.mockito.Mock;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class ParameterTest {

    @Mock
    ActorRef actorRef;

    @Test
    public void normalTest() throws Exception {

        Parameters parameters = Parameters.instance();
        parameters = parameters.addSender(actorRef);
        assertThat(parameters.getSender()).isEqualTo(actorRef);

        parameters = parameters.addClassName(String.class);
        assertThat(parameters.getClassName()).isEqualTo(String.class);

        parameters = parameters.addShardRegionName("shard1");
        assertThat(parameters.getShardRegionName()).isEqualTo("shard1");

        parameters = parameters.add("key1", "v1");
        assertThat(parameters.getString("key1")).isEqualTo("v1");

        parameters = parameters.add("1", 1);
        assertThat(parameters.getInteger("1")).isEqualTo(1);

        assertThat(parameters.parseInt("1")).isEqualTo(1);
        assertThat(parameters.parseLong("1")).isEqualTo(1);

        parameters = parameters.add("2", 2L);
        assertThat(parameters.getLong("2")).isEqualTo(2L);

        UUID uuid = UUID.randomUUID();
        parameters = parameters.add("uuid", uuid);
        assertThat(parameters.getUuid("uuid")).isEqualTo(uuid);

        assertThat(parameters.getParameters()).isNotNull();
        assertThat(parameters.toString()).isNotNull();
    }

}
