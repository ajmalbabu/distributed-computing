package akka.initializer.model;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Abstracts the yaml configuration.
 */
@Component
public class AkkaConfig {

    @Value("${akka.initializer.config.file.name:}")
    private String akkaConfigFileName;

    @Value("${akka.initializer.actor.system.name:AkkaInitializerActorSystem}")
    private String actorSystemName;

    public String getAkkaConfigFileName() {
        return akkaConfigFileName;
    }

    public void setAkkaConfigFileName(String akkaConfigFileName) {
        this.akkaConfigFileName = akkaConfigFileName;
    }

    public String getActorSystemName() {
        return actorSystemName;
    }

    public void setActorSystemName(String actorSystemName) {
        this.actorSystemName = actorSystemName;
    }
}
