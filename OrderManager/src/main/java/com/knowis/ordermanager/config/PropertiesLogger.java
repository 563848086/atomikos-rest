package com.knowis.ordermanager.config;

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
       printAllApplicationProperties((ConfigurableEnvironment) event.getApplicationContext().getEnvironment());
    }

    private void printAllApplicationProperties(ConfigurableEnvironment env) {

        log.info("************************* APP PROPERTIES ***********************************");

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