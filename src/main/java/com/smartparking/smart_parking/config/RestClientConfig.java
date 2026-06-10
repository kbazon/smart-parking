package com.smartparking.smart_parking.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import java.net.http.HttpClient;
import org.springframework.http.client.JdkClientHttpRequestFactory;


@Configuration
public class RestClientConfig {

    @Value("${ml.service.url}")
    private String mlServiceUrl;

    @Bean
    public RestClient mlRestClient() {
        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)  // ← ovo je fix
                .build();

        return RestClient.builder()
                .baseUrl(mlServiceUrl)
                .requestFactory(new JdkClientHttpRequestFactory(httpClient))
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Accept", "application/json")
                .build();
    }
}
