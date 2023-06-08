package com.doceree.foresightService.configurations;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Properties;

import static com.doceree.foresightService.utils.ApplicationConstants.*;

@Configuration
public class SnowflakeClientConfiguration {

    @Autowired
    ApplicationConfiguration applicationConfiguration;
    @Bean
    public JdbcTemplate snowflakenAnalylticsJdbcTemplate() {
        String url = applicationConfiguration.get(SPRING_SNOWFLAKE_DOCEREE_ANALYTICS_DATASOURCE_URL, String.class);
        HikariDataSource ds = getHikariDataSource(url);
        return new JdbcTemplate(ds);
    }

    @Bean
    public JdbcTemplate snowflakeCommonJdbcTemplate() {
        String url = applicationConfiguration.get(SPRING_SNOWFLAKE_COMMON_DATASOURCE_URL, String.class);
        HikariDataSource ds = getHikariDataSource(url);
        return new JdbcTemplate(ds);
    }

    private HikariDataSource getHikariDataSource(String url) {

        String user = applicationConfiguration.get(SNOWFLAKE_DATASOURCE_USERNAME, String.class);
        String password = applicationConfiguration.get(SNOWFLAKE_DATASOURCE_PASSWORD, String.class);
        String timeout = applicationConfiguration.get(SNOWFLAKE_DATASOURCE_HIKARI_CONNECTION_TIMEOUT, String.class);
        String idleTime = applicationConfiguration.get(SNOWFLAKE_DATASOURCE_HIKARI_IDLE_TIMEOUT, String.class);
        String maxlifeTime = applicationConfiguration.get(SNOWFLAKE_DATASOURCE_HIKARI_MAX_LIFETIME, String.class);
        String maxPoolSize = applicationConfiguration.get(SNOWFLAKE_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE, String.class);
        String driverClass = applicationConfiguration.get(SNOWFLAKE_DRIVERCLASS, String.class);
        Properties properties = new Properties();
        properties.put(MAX_LIFE_TIME, maxlifeTime);
        properties.put(USER, user);
        properties.put(PASSWORD, password);
        HikariConfig config = new HikariConfig();
        config.setDriverClassName(driverClass);
        config.setDataSourceProperties(properties);
        config.setJdbcUrl(url);
        config.setConnectionTimeout(Long.parseLong(timeout));
        config.setIdleTimeout(Long.parseLong(idleTime));
        config.setMaximumPoolSize(Integer.parseInt(maxPoolSize));
        HikariDataSource ds = new HikariDataSource(config);
        return ds;
    }
}
