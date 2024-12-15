package org.mql.ws.sentimentanalyze.api.configuration;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RestTemplateConfiguration {

    @Bean
    public org.springframework.web.client.RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }
}
