package dev.rm.service;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import dev.rm.model.Role;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class RoleService {

    private final WebClient baseWebClient;

    public RoleService(WebClient baseWebClient) {
        this.baseWebClient = baseWebClient;
    }

    public Flux<Role> getAllRoles() {
        return baseWebClient.get()
                .uri("/getAllRolesFunction")
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> response.bodyToMono(String.class)
                                .flatMap(errorMessage -> {
                                    log.error("Error calling Azure Function: {}", errorMessage);
                                    return Mono.error(new RuntimeException("Failed to fetch roles: " + errorMessage));
                                }))
                .bodyToFlux(Role.class);
    }

    public Mono<Role> getRole(Long roleId) {
        return baseWebClient.get()
                .uri(uriBuilder -> uriBuilder.path("/getRoleFunction").queryParam("roleId", roleId)
                        .build())
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> response.bodyToMono(String.class)
                                .flatMap(errorMessage -> {
                                    log.error("Error calling Azure Function: {}", errorMessage);
                                    return Mono.error(new RuntimeException("Failed to fetch role: " +
                                            errorMessage));
                                }))
                .bodyToMono(Role.class);
    }

    public Mono<Role> createRole(Role role) {
        return baseWebClient.post()
                .uri("/createRoleFunction")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(role)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> response.bodyToMono(String.class)
                                .flatMap(errorMessage -> {
                                    log.error("Error calling Azure Function: {}", errorMessage);
                                    return Mono.error(new RuntimeException("Failed to create role: " +
                                            errorMessage));
                                }))
                .bodyToMono(Role.class);
    }

    public Mono<Role> updateRole(Long roleId, Role role) {
        return baseWebClient.put()
                .uri(uriBuilder -> uriBuilder.path("/updateRoleFunction").queryParam("roleId", roleId)
                        .build())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(role)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> response.bodyToMono(String.class)
                                .flatMap(errorMessage -> {
                                    log.error("Error calling Azure Function: {}", errorMessage);
                                    return Mono.error(new RuntimeException("Failed to update role: " +
                                            errorMessage));
                                }))
                .bodyToMono(Role.class);
    }

    public Mono<Void> deleteRole(Long roleId) {
        return baseWebClient.delete()
                .uri(uriBuilder -> uriBuilder.path("/deleteRoleFunction").queryParam("roleId", roleId)
                        .build())
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> response.bodyToMono(String.class)
                                .flatMap(errorMessage -> {
                                    log.error("Error calling Azure Function: {}", errorMessage);
                                    return Mono.error(new RuntimeException("Failed to delete role: " +
                                            errorMessage));
                                }))
                .bodyToMono(Void.class);
    }

}
