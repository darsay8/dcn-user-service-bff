package dev.rm.service;

import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import dev.rm.model.User;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@AllArgsConstructor
public class UserService {

    private final WebClient createUserWebClient;
    private final WebClient updateUserWebClient;
    private final WebClient deleteUserWebClient;
    private final WebClient getUserWebClient;

    public Flux<User> getAllUsers() {
        return getUserWebClient.get()
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
        return getUserWebClient.get()
                .uri(uriBuilder -> uriBuilder.path("/getUserFunction").queryParam("userId", userId)
                        .build())
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> response.bodyToMono(String.class)
                                .flatMap(errorMessage -> {
                                    log.error("Error calling Azure Function: {}", errorMessage);
                                    return Mono.error(new RuntimeException("Failed to fetch user: " +
                                            errorMessage));
                                }))
                .bodyToMono(User.class);
    }

    public Mono<User> createUser(User user) {
        log.debug("Sending request to Azure Function: {}", user);
        return createUserWebClient.post()
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
        return updateUserWebClient.put()
                .uri(uriBuilder -> uriBuilder.path("").queryParam("userId", userId)
                        .build())
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
        return deleteUserWebClient.delete()
                .uri(uriBuilder -> uriBuilder.path("")
                        .queryParam("userId", userId)
                        .build())
                .retrieve()
                .toBodilessEntity()
                .then();
    }

}
