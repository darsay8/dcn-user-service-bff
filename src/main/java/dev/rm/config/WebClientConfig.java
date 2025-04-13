package dev.rm.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient baseWebClient(@Value("${azure.function.user.dev.url}") String baseUrl) {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Accept", "application/json")
                .build();
    }

    @Bean
    public WebClient graphqlWebClient(@Value("${azure.function.graphql.url}") String graphqlUrl) {
        return WebClient.builder()
                .baseUrl(graphqlUrl)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Bean
    public WebClient createUserWebClient(@Value("${azure.function.rest.user.url.create}") String createUserUrl) {
        return WebClient.builder()
                .baseUrl(createUserUrl)
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Accept", "application/json")
                .build();
    }

    @Bean
    public WebClient getUserWebClient(@Value("${azure.function.rest.user.url.get}") String getUserUrl) {
        return WebClient.builder()
                .baseUrl(getUserUrl)
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Accept", "application/json")
                .build();
    }

    @Bean
    public WebClient deleteUserWebClient(@Value("${azure.function.graphql.user.delete}") String deleteUserUrl) {
        return WebClient.builder()
                .baseUrl(deleteUserUrl)
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Accept", "application/json")
                .build();
    }

    @Bean
    public WebClient createRolWebClient(@Value("${azure.function.graphql.role.create}") String createRoleUrl) {
        return WebClient.builder()
                .baseUrl(createRoleUrl)
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Accept", "application/json")
                .build();
    }

    @Bean
    public WebClient getRoleWebClient(@Value("${azure.function.rest.role.url.get}") String getRoleUrl) {
        return WebClient.builder()
                .baseUrl(getRoleUrl)
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Accept", "application/json")
                .build();
    }

    @Bean
    public WebClient updateRolWebClient(@Value("${azure.function.graphql.role.update}") String updateRoleUrl) {
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