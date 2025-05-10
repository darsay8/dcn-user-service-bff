package dev.rm.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import org.springframework.web.reactive.function.client.WebClient;

import dev.rm.model.Role;
import dev.rm.model.User;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserGraphQLService {

    private final WebClient graphqlWebClient;

    public Flux<User> getAllUsers() {
        log.info("Fetching all users");

        String query = """
                    query {
                        getAllUsers {
                            userId
                            username
                            email
                            role {
                                roleId
                                name
                            }
                        }
                    }
                """;

        return executeListQuery("/getAllUsersFunctionGraphQL", query, "getAllUsers", User.class);
    }

    public Mono<User> getUserById(UUID userId) {
        log.info("Fetching user with ID: {}", userId);

        String query = String.format("""
                    query {
                        getUser(id: "%s") {
                            userId
                            username
                            email
                            role {
                                roleId
                                name
                            }
                        }
                    }
                """, userId);

        return executeSingleQuery("/getUserFunctionGraphQL", query, "getUser", User.class);
    }

    public Mono<User> createUser(User user) {
        log.info("Creating new user: {}", user);

        String mutation = String.format("""
                    mutation {
                        saveUser(input: {
                            username: "%s"
                            email: "%s"
                            password: "%s"
                            roleId: %d
                        }) {
                            userId
                            username
                            email
                            password
                        }
                    }
                """, user.getUsername(), user.getEmail(), user.getPassword(),
                user.getRole().getRoleId());

        return executeSingleQuery("/createUserFunctionGraphQL", mutation, "saveUser", User.class);
    }

    public Mono<User> updateUser(UUID userId, Map<String, Object> updates) {
        log.info("Updating user with ID: {}, updates: {}", userId, updates);

        StringBuilder inputBuilder = new StringBuilder();

        // Only add fields that are present in the updates map
        if (updates.containsKey("username")) {
            inputBuilder.append(String.format("username: \"%s\"", updates.get("username")));
        }

        if (updates.containsKey("email")) {
            if (inputBuilder.length() > 0)
                inputBuilder.append("\n        ");
            inputBuilder.append(String.format("email: \"%s\"", updates.get("email")));
        }

        if (updates.containsKey("password")) {
            if (inputBuilder.length() > 0)
                inputBuilder.append("\n        ");
            inputBuilder.append(String.format("password: \"%s\"", updates.get("password")));
        }

        if (updates.containsKey("roleId")) {
            if (inputBuilder.length() > 0)
                inputBuilder.append("\n        ");
            inputBuilder.append(String.format("roleId: %s", updates.get("roleId")));
        }

        String mutation = String.format("""
                    mutation {
                        updateUser(id: "%s", input: {
                            %s
                        }) {
                            userId
                            username
                            email
                            role {
                                roleId
                                name
                            }
                        }
                    }
                """, userId, inputBuilder.toString());

        return executeSingleQuery("/updateUserFunctionGraphQL", mutation, "updateUser", User.class);
    }

    public Mono<Boolean> deleteUser(UUID userId) {
        log.info("Deleting user with ID: {}", userId);

        String mutation = String.format("""
                    mutation {
                        deleteUser(id: "%s")
                    }
                """, userId);

        return executeSingleQuery("/deleteUserFunctionGraphQL", mutation, "deleteUser", Boolean.class);
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

        if (type == User.class) {
            Map<String, Object> roleMap = (Map<String, Object>) dataMap.get("role");

            Role role = Role.builder()
                    .roleId(roleMap != null && roleMap.get("roleId") != null
                            ? Long.valueOf(roleMap.get("roleId").toString())
                            : null)
                    .name(roleMap != null ? (String) roleMap.get("name") : null)
                    .build();

            return (T) User.builder()
                    .userId(dataMap.get("userId") != null ? UUID.fromString(dataMap.get("userId").toString()) : null)
                    .username((String) dataMap.get("username"))
                    .email((String) dataMap.get("email"))
                    .password((String) dataMap.get("password"))
                    .role(role)
                    .build();
        }

        throw new IllegalArgumentException("Unsupported conversion type: " + type.getName());
    }
}