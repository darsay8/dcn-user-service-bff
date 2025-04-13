package dev.rm.resolver;

import java.util.Map;
import java.util.UUID;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import dev.rm.model.Role;
import dev.rm.model.User;

import dev.rm.service.UserGraphQLService;
import graphql.kickstart.tools.GraphQLMutationResolver;
import graphql.kickstart.tools.GraphQLQueryResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Controller
@Slf4j
@RequiredArgsConstructor
public class UserGraphQLResolver implements GraphQLQueryResolver, GraphQLMutationResolver {

    private final UserGraphQLService userGraphQLService;

    @QueryMapping
    public Flux<User> getAllUsers() {
        log.info("GraphQL request: getAllUsers");
        return userGraphQLService.getAllUsers();
    }

    @QueryMapping
    public Mono<User> getUser(@Argument String id) {
        log.info("GraphQL request: getUser with id: {}", id);
        return userGraphQLService.getUserById(UUID.fromString(id));
    }

    @MutationMapping
    public Mono<User> saveUser(@Argument("input") Map<String, Object> input) {
        log.info("GraphQL request: saveUser with input: {}", input);
        return userGraphQLService.createUser(convertToUser(input));
    }

    @MutationMapping
    public Mono<User> updateUser(@Argument String id, @Argument("input") Map<String, Object> input) {
        log.info("GraphQL request: updateUser with id: {} and input: {}", id, input);
        return userGraphQLService.updateUser(UUID.fromString(id), input);
    }

    @MutationMapping
    public Mono<Boolean> deleteUser(@Argument String id) {
        log.info("GraphQL request: deleteUser with id: {}", id);
        return userGraphQLService.deleteUser(UUID.fromString(id));
    }

    private User convertToUser(Map<String, Object> input) {
        Role role = Role.builder()
                .roleId(input.get("roleId") != null ? Long.valueOf(input.get("roleId").toString()) : null)
                .build();

        return User.builder()
                .username((String) input.get("username"))
                .email((String) input.get("email"))
                .password((String) input.get("password"))
                .role(role)
                .build();
    }
}