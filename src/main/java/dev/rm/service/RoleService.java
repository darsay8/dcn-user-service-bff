package dev.rm.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
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

    @Value("${azure.function.role.rest.prod.create-code}")
    private String createRoleCodeKey;

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
        log.debug("Sending request to Azure Function to create role: {}", role);
        return baseWebClient.post()
                .uri("/createRoleFunction")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(role)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> response.bodyToMono(String.class)
                                .flatMap(errorMessage -> {
                                    log.error("Error calling Azure Function: {}", errorMessage);
                                    return Mono.error(new RuntimeException("Failed to create role: " + errorMessage));
                                }))
                .bodyToMono(Role.class) // ✅ deserialize directly to Role
                .doOnSuccess(createdRole -> log.info("Role created successfully: {}", createdRole));
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

    public Mono<Map<String, String>> deleteRole(Long roleId) {
        return baseWebClient.delete()
                .uri(uriBuilder -> uriBuilder
                        .path("/deleteRoleFunction")
                        .queryParam("roleId", roleId)
                        .build())
                .exchangeToMono(response -> {
                    int statusCode = response.statusCode().value();
                    log.info(":::::::::::>>>>>>>>>>> Delete role response status code: {}", statusCode);

                    if (statusCode == 204) {
                        Map<String, String> successMap = new HashMap<>();
                        successMap.put("message", "Role deleted successfully. No users required reassignment.");
                        successMap.put("status", "SUCCESS");
                        return Mono.error(new SilentSuccessException(successMap));
                    }

                    if (statusCode == 200) {
                        Map<String, String> successMap = new HashMap<>();
                        successMap.put("message", "Role deleted successfully after reassigning users.");
                        successMap.put("status", "SUCCESS");
                        return Mono.error(new SilentSuccessException(successMap));
                    }

                    if (statusCode >= 400) {
                        return response.bodyToMono(String.class)
                                .flatMap(body -> {
                                    log.error("Azure Function returned error: {}", body);
                                    return Mono.error(new RuntimeException("Failed to delete role: " + body));
                                });
                    }

                    return response.bodyToMono(new ParameterizedTypeReference<Map<String, String>>() {
                    })
                            .doOnNext(resp -> log.info("Delete role response: {}", resp));
                })
                .onErrorResume(SilentSuccessException.class, ex -> Mono.just(ex.getResponseMap()));
    }

    // Custom signal class
    public static class SilentSuccessException extends RuntimeException {
        private final Map<String, String> responseMap;

        public SilentSuccessException(Map<String, String> responseMap) {
            super((String) responseMap.getOrDefault("message", "Success"));
            this.responseMap = responseMap;
        }

        public Map<String, String> getResponseMap() {
            return responseMap;
        }
    }

}
