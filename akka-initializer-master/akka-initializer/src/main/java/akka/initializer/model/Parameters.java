package akka.initializer.model;

import akka.actor.ActorRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Dynamic parameters that can be set into Actor. Read java-docs in ParameterInjector.
 * This class is completely immutable and thread safe.
 */
public class Parameters implements Serializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(Parameters.class);
    private static final long serialVersionUID = 1L;

    private static final String SENDER_ACTOR_REF = "SENDER_ACTOR_REF";
    public static final String SHARD_REGION_NAME = "shardRegionName";
    public static final String CLASS_NAME = "className";

    private final Map<String, Object> parameters;


    public Parameters() {
        this(new HashMap<>());
    }

    public Parameters(Map<String, Object> parameters) {
        this.parameters = new HashMap<>(parameters);
    }


    public Parameters add(String key, Object value) {
        Map<String, Object> params = new HashMap<>(parameters);
        params.put(key, value);
        return new Parameters(params);
    }


    public <T> T get(String key) {
        return (T) parameters.get(key);
    }

    public Parameters addSender(ActorRef sender) {
        return add(SENDER_ACTOR_REF, sender);
    }

    public ActorRef getSender() {
        return (ActorRef) get(SENDER_ACTOR_REF);
    }

    public Parameters addClassName(Class<?> className) {
        return add(CLASS_NAME, className);
    }

    public Class<?> getClassName() {
        return get(CLASS_NAME);
    }

    public Parameters addShardRegionName(String persistenceId) {
        return add(SHARD_REGION_NAME, persistenceId);
    }

    public String getShardRegionName() {
        return (String) get(SHARD_REGION_NAME);
    }

    public Map<String, Object> getParameters() {
        return new HashMap<>(parameters);
    }

    public String getString(String param) {
        return get(param).toString();
    }

    public Integer getInteger(String param) {
        return (Integer) get(param);
    }

    public Long getLong(String param) {
        return (Long) get(param);
    }

    public UUID getUuid(String param) {
        return (UUID) get(param);
    }

    public Long parseLong(String param) {
        return Long.parseLong(get(param).toString());
    }

    public Integer parseInt(String param) {
        return Integer.parseInt(get(param).toString());
    }

    @Override
    public String toString() {
        return "Parameters{" +
                "parameters=" + parameters +
                '}';
    }


    // Factory methods.

    public static Parameters instance() {
        return new Parameters();
    }
}
