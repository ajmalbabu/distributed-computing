package com.cep.bank.service.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Cassandra properties.
 */
@Component
@ConfigurationProperties(prefix = "cassandra", ignoreUnknownFields = false)
public class CassandraProperties {

    @Value("${keyspace:cep}")
    private String keySpace;

    @Value("${contactpoints:localhost}")
    private String contactPoints;

    @Value("${port:9042}")
    private String port;


    public CassandraProperties() {
    }

    public String getKeySpace() {
        return keySpace;
    }

    public void setKeySpace(String keySpace) {
        this.keySpace = keySpace;
    }

    public String getContactPoints() {
        return contactPoints;
    }

    public void setContactPoints(String contactPoints) {
        this.contactPoints = contactPoints;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }
}
