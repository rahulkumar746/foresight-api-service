package com.doceree.foresightService.configurations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

@Configuration
@PropertySource("classpath:application.properties")
public class ApplicationConfiguration {

    static final Logger logger = LoggerFactory.getLogger(ApplicationConfiguration.class);
    private final Environment environment;

    @Autowired
    public ApplicationConfiguration(Environment environment) {
        this.environment = environment;
    }

    public <Y> Y get(String key, Class<Y> cls) {
        return environment.getRequiredProperty(key, cls);
    }
}
