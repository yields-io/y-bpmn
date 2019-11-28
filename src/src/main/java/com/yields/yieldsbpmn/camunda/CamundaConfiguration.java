package com.yields.yieldsbpmn.camunda;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;


//@Configuration
class CamundaConfiguration {

    @Bean
    @Primary
    @ConfigurationProperties(prefix="datasource.primary")
    DataSource primaryDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name="camundaBpmDataSource")
    @ConfigurationProperties(prefix="datasource.secondary")
    DataSource secondaryDataSource() {
        return DataSourceBuilder.create().build();
    }
}
