package com.cep.bank.service.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cassandra.config.CassandraCqlSessionFactoryBean;
import org.springframework.cassandra.config.java.AbstractSessionConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.cassandra.config.CassandraClusterFactoryBean;

@Configuration
public class CassandraConfiguration extends AbstractSessionConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(CassandraConfiguration.class);

    @Autowired
    private Environment environment;


    @Autowired
    private CassandraProperties cassandraProperties;

    @Override
    public String getKeyspaceName() {
        return cassandraProperties.getKeySpace();
    }

    @Bean
    public CassandraClusterFactoryBean cluster() {

        CassandraClusterFactoryBean cluster = new CassandraClusterFactoryBean();
        cluster.setContactPoints(cassandraProperties.getContactPoints());
        cluster.setPort(Integer.valueOf(cassandraProperties.getPort()));
        return cluster;
    }


    @Bean
    @Override
    public CassandraCqlSessionFactoryBean session() {

        if (environment.acceptsProfiles("isolate")) {
            return new IsolateModeArtifacts.CassandraCqlSessionFactoryBeanFake();
        } else {
            return super.session();
        }
    }


}
