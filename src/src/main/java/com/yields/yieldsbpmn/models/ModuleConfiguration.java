package com.yields.yieldsbpmn.models;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
class ModuleConfiguration {

    @Value("${chiron.api.url}")
    private String chironApiUrl;

    @Bean
    ModelsFetcher modelsFetcher() {
        return new ModelsFetcher(chironApiUrl);
    }
}
