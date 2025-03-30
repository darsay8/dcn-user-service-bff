package dev.rm.service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import dev.rm.model.User;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class UserService {

    public UserService(WebClient webClient) {
        this.webClient = webClient;
    }

    @Value("${azure.function.user.prod.url}")
    private String azFunctionUserUrl;

    private final WebClient webClient;

    public Flux<User> getAllUsers() {
        return webClient.get()
                .uri("/getAllUsersFunction")
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> response.bodyToMono(String.class)
                                .flatMap(errorMessage -> {
                                    log.error("Error calling Azure Function: {}", errorMessage);
                                    return Mono.error(new RuntimeException("Failed to fetch users: " + errorMessage));
                                }))
                .bodyToFlux(User.class);
    }

    public Mono<User> getUserById(UUID userId) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/getUserFunction").queryParam("userId", userId).build())
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> response.bodyToMono(String.class)
                                .flatMap(errorMessage -> {
                                    log.error("Error calling Azure Function: {}", errorMessage);
                                    return Mono.error(new RuntimeException("Failed to fetch user: " + errorMessage));
                                }))
                .bodyToMono(User.class);
    }

    public Mono<User> createUser(User user) {
        log.debug("Sending request to Azure Function: {}", user);
        return webClient.post()
                .uri("/createUserFunction")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(user)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> response.bodyToMono(String.class)
                                .flatMap(errorMessage -> {
                                    log.error("Error calling Azure Function: {}", errorMessage);
                                    return Mono.error(new RuntimeException("Failed to create user: " + errorMessage));
                                }))
                .bodyToMono(User.class)
                .doOnSuccess(createdUser -> log.info("User created successfully: {}", createdUser))
                .doOnError(error -> log.error("Error creating user: {}", error.getMessage()));
    }

    public Mono<User> updateUser(UUID userId, User user) {
        return webClient.put()
                .uri(uriBuilder -> uriBuilder.path("/updateUserFunction").queryParam("userId", userId).build())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(user)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> response.bodyToMono(String.class)
                                .flatMap(errorMessage -> {
                                    log.error("Error calling Azure Function: {}", errorMessage);
                                    return Mono.error(new RuntimeException("Failed to update user: " + errorMessage));
                                }))
                .bodyToMono(User.class)
                .doOnSuccess(updatedUser -> log.info("User updated successfully: {}", updatedUser))
                .doOnError(error -> log.error("Error updating user: {}", error.getMessage()));
    }

    public Mono<Void> deleteUser(UUID userId) {
        return webClient.delete()
                .uri(uriBuilder -> uriBuilder.path("/deleteUserFunction")
                        .queryParam("userId", userId)
                        .build())
                .retrieve()
                .toBodilessEntity()
                .then();
    }

}
