package dev.rm.service;

import java.util.List;
import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import org.springframework.web.reactive.function.client.WebClient;

import dev.rm.model.Role;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class RoleGraphQLService {

    private final WebClient graphqlWebClient;

    public Flux<Role> getAllRoles() {
        log.info("Fetching all roles");

        String query = """
                    query {
                        getAllRoles {
                            roleId
                            name
                        }
                    }
                """;

        return executeListQuery("/getAllRolesFunctionGraphQL", query, "getAllRoles", Role.class);
    }

    public Mono<Role> getRoleById(Long roleId) {
        log.info("Fetching role with ID: {}", roleId);

        String query = String.format("""
                    query {
                        getRole(id: %d) {
                            roleId
                            name
                        }
                    }
                """, roleId);

        return executeSingleQuery("/getRoleFunctionGraphQL", query, "getRole", Role.class);
    }

    public Mono<Role> createRole(Role role) {
        log.info("Creating new role: {}", role);

        String mutation = String.format("""
                    mutation {
                        saveRole(input: {
                            name: "%s"
                        }) {
                            roleId
                            name
                        }
                    }
                """, role.getName());

        return executeSingleQuery("/createRoleFunctionGraphQL", mutation, "saveRole", Role.class);
    }

    public Mono<Role> updateRole(Long roleId, Map<String, Object> updates) {
        log.info("Updating role with ID: {}, updates: {}", roleId, updates);

        StringBuilder inputBuilder = new StringBuilder();

        // Only add name if it's present in the updates map
        if (updates.containsKey("name")) {
            inputBuilder.append(String.format("name: \"%s\"", updates.get("name")));
        }

        String mutation = String.format("""
                    mutation {
                        updateRole(id: %d, input: {
                            %s
                        }) {
                            roleId
                            name
                        }
                    }
                """, roleId, inputBuilder.toString());

        return executeSingleQuery("/updateRoleFunctionGraphQL", mutation, "updateRole", Role.class);
    }

    public Mono<Boolean> deleteRole(Long roleId) {
        log.info("Deleting role with ID: {}", roleId);

        String mutation = String.format("""
                    mutation {
                        deleteRole(id: %d)
                    }
                """, roleId);

        return executeSingleQuery("/deleteRoleFunctionGraphQL", mutation, "deleteRole", Boolean.class);
    }

    private <T> Flux<T> executeListQuery(String uri, String query, String rootField, Class<T> type) {
        return executeRawQuery(uri, query)
                .map(data -> {
                    if (data == null || data.get(rootField) == null) {
                        log.error("No data returned for field: {}", rootField);
                        throw new RuntimeException("No data returned from GraphQL service");
                    }

                    List<Map<String, Object>> resultList = (List<Map<String, Object>>) data.get(rootField);
                    return resultList.stream()
                            .map(item -> convertToType(item, type))
                            .toList();
                })
                .flatMapMany(Flux::fromIterable)
                .doOnError(e -> log.error("Error executing list query: {}", e.getMessage()));
    }

    private <T> Mono<T> executeSingleQuery(String uri, String query, String rootField, Class<T> type) {
        return executeRawQuery(uri, query)
                .doOnNext(data -> log.debug("Raw response data: {}", data))
                .map(data -> {
                    if (data == null || data.get(rootField) == null) {
                        log.error("No data returned for field: {}", rootField);
                        throw new RuntimeException("No data returned from GraphQL service");
                    }
                    return convertToType(data.get(rootField), type);
                })
                .doOnError(e -> log.error("Error executing query: {}", e.getMessage()))
                .doOnNext(result -> log.debug("Converted result: {}", result));
    }

    private Mono<Map<String, Object>> executeRawQuery(String uri, String query) {
        log.debug("Sending GraphQL to {}:\n{}", uri, query);

        return graphqlWebClient.post()
                .uri(uri)
                .header("X-REQUEST-TYPE", "GraphQL")
                .bodyValue(Map.of("query", query))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .map(response -> {
                    if (response.containsKey("errors")) {
                        List<Map<String, Object>> errors = (List<Map<String, Object>>) response.get("errors");
                        String errorMessage = errors.stream()
                                .map(error -> error.get("message").toString())
                                .reduce((a, b) -> a + "; " + b)
                                .orElse("Unknown GraphQL error");
                        log.error("GraphQL error: {}", errorMessage);
                        throw new RuntimeException("GraphQL error: " + errorMessage);
                    }
                    return (Map<String, Object>) response.get("data");
                })
                .doOnError(e -> log.error("Error executing raw query: {}", e.getMessage()));
    }

    @SuppressWarnings("unchecked")
    private <T> T convertToType(Object data, Class<T> type) {
        if (data == null) {
            return null;
        }
        if (type == Boolean.class) {
            return (T) Boolean.valueOf(data.toString());
        }
        if (!(data instanceof Map)) {
            throw new IllegalArgumentException("Data is not a Map");
        }
        Map<String, Object> dataMap = (Map<String, Object>) data;
        if (type == Role.class) {
            return (T) Role.builder()
                    .roleId(dataMap.get("roleId") != null ? Long.valueOf(dataMap.get("roleId").toString()) : null)
                    .name((String) dataMap.get("name"))
                    .build();
        }
        throw new IllegalArgumentException("Unsupported conversion type: " + type.getName());
    }
}