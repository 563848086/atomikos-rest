package com.knowis.payment.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
@Slf4j
public class PropertiesLogger {

    @EventListener
    public void handleContextRefreshed(ContextRefreshedEvent event) {
       // printAllActiveProperties((ConfigurableEnvironment) event.getApplicationContext().getEnvironment());

        printAllApplicationProperties((ConfigurableEnvironment) event.getApplicationContext().getEnvironment());
    }

    private void printAllActiveProperties(ConfigurableEnvironment env) {

        log.info("************************* ALL PROPERTIES(EVENT) ******************************");

        env.getPropertySources()
                .stream()
                .filter(ps -> ps instanceof MapPropertySource)
                .map(ps -> ((MapPropertySource) ps).getSource().keySet())
                .flatMap(Collection::stream)
                .distinct()
                .sorted()
                .forEach(key -> log.info("{}={}", key, env.getProperty(key)));

        log.info("******************************************************************************");
    }

    private void printAllApplicationProperties(ConfigurableEnvironment env) {

        log.info("************************* APP PROPERTIES(EVENT) ******************************");

        env.getPropertySources()
                .stream()
                .filter(ps -> ps instanceof MapPropertySource && ps.getName().contains("application.properties"))
                .map(ps -> ((MapPropertySource) ps).getSource().keySet())
                .flatMap(Collection::stream)
                .distinct()
                .sorted()
                .forEach(key -> log.info("{}={}", key, key.toLowerCase().contains("password")?"******":env.getProperty(key)));

        log.info("******************************************************************************");
    }
}