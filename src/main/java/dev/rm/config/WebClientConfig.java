package dev.rm.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class WebClientConfig {

    @Value("${azure.function.user.prod.url}")
    private String baseUrl;

    @Value("${azure.function.user.prod.create}")
    private String createUrl;

    @Value("${azure.function.user.prod.update}")
    private String updateUrl;

    @Value("${azure.function.user.prod.delete}")
    private String deleteUrl;

    @Value("${azure.function.user.prod.get}")
    private String getUrl;

    @Bean
    public WebClient createUserWebClient() {
        return WebClient.builder()
                .baseUrl(createUrl)
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Accept", "application/json")
                .build();
    }

    @Bean
    public WebClient updateUserWebClient() {
        return WebClient.builder()
                .baseUrl(updateUrl)
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Accept", "application/json")
                .build();
    }

    @Bean
    public WebClient deleteUserWebClient() {
        return WebClient.builder()
                .baseUrl(deleteUrl)
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Accept", "application/json")
                .build();
    }

    @Bean
    public WebClient getUserWebClient() {
        return WebClient.builder()
                .baseUrl(getUrl)
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Accept", "application/json")
                .build();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}