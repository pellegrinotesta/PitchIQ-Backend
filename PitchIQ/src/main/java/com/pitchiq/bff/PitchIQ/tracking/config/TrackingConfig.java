package com.pitchiq.bff.PitchIQ.tracking.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(TrackingProperties.class)
public class TrackingConfig {

    @Bean
    RestClient analyticsRestClient(TrackingProperties props) {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3_000);
        factory.setReadTimeout(10_000); // il servizio risponde 202 subito, il calcolo va in background
        return RestClient.builder()
                .baseUrl(props.analyticsUrl())
                .requestFactory(factory)
                .build();
    }
}
