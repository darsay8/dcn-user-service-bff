package dev.rm.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient baseWebClient(@Value("${azure.function.user.rest.prod.url}") String baseUrl) {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Accept", "application/json")
                .build();
    }

    @Bean
    public WebClient graphqlWebClient(@Value("${azure.function.user.graphql.prod.url}") String graphqlUrl) {
        return WebClient.builder()
                .baseUrl(graphqlUrl)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Bean
    public WebClient createUserWebClient(@Value("${azure.function.user.rest.prod.create}") String createUserUrl) {
        return WebClient.builder()
                .baseUrl(createUserUrl)
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Accept", "application/json")
                .build();
    }

    @Bean
    public WebClient getUserWebClient(@Value("${azure.function.user.rest.prod.get}") String getUserUrl) {
        return WebClient.builder()
                .baseUrl(getUserUrl)
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Accept", "application/json")
                .build();
    }

    @Bean
    public WebClient updateUserWebClient(@Value("${azure.function.user.rest.prod.update}") String updateUserUrl) {
        return WebClient.builder()
                .baseUrl(updateUserUrl)
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Accept", "application/json")
                .build();
    }

    @Bean
    public WebClient deleteUserWebClient(@Value("${azure.function.user.rest.prod.delete}") String deleteUserUrl) {
        return WebClient.builder()
                .baseUrl(deleteUserUrl)
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Accept", "application/json")
                .build();
    }

    @Bean
    public WebClient createRoleWebClient(@Value("${azure.function.role.rest.prod.create}") String createRoleUrl) {
        return WebClient.builder()
                .baseUrl(createRoleUrl)
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Accept", "application/json")
                .build();
    }

    @Bean
    public WebClient getRoleWebClient(@Value("${azure.function.role.rest.prod.get}") String getRoleUrl) {
        return WebClient.builder()
                .baseUrl(getRoleUrl)
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Accept", "application/json")
                .build();
    }

    @Bean
    public WebClient updateRoleWebClient(@Value("${azure.function.role.rest.prod.update}") String updateRoleUrl) {
        return WebClient.builder()
                .baseUrl(updateRoleUrl)
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Accept", "application/json")
                .build();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}