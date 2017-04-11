package com.cep.bank.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

@SpringBootApplication(scanBasePackages = "com.cep, akka.sdk")
@EnableCaching
public class Application {

    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        LOGGER.debug("BootClasspath: ", runtimeMXBean.getBootClassPath());
        LOGGER.debug("Classpath: ", runtimeMXBean.getClassPath());
        LOGGER.debug("Library Path: ", runtimeMXBean.getLibraryPath());
        for (String argument : runtimeMXBean.getInputArguments()) {
            LOGGER.debug("Input Argument: ", argument);
        }


    }


}

