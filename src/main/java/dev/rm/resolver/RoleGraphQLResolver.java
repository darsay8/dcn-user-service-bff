package dev.rm.resolver;

import java.util.Map;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import dev.rm.model.Role;
import dev.rm.service.RoleGraphQLService;
import graphql.kickstart.tools.GraphQLMutationResolver;
import graphql.kickstart.tools.GraphQLQueryResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Controller
@Slf4j
@RequiredArgsConstructor
public class RoleGraphQLResolver implements GraphQLQueryResolver, GraphQLMutationResolver {

    private final RoleGraphQLService roleGraphQLService;

    @QueryMapping
    public Flux<Role> getAllRoles() {
        log.info("GraphQL request: getAllRoles");
        return roleGraphQLService.getAllRoles();
    }

    @QueryMapping
    public Mono<Role> getRole(@Argument String id) {
        log.info("GraphQL request: getRole with id: {}", id);
        return roleGraphQLService.getRoleById(Long.valueOf(id));
    }

    @MutationMapping
    public Mono<Role> saveRole(@Argument("input") Map<String, Object> input) {
        log.info("GraphQL request: saveRole with input: {}", input);
        return roleGraphQLService.createRole(convertToRole(input));
    }

    @MutationMapping
    public Mono<Role> updateRole(@Argument String id, @Argument("input") Map<String, Object> input) {
        log.info("GraphQL request: updateRole with id: {} and input: {}", id, input);
        return roleGraphQLService.updateRole(Long.valueOf(id), input);
    }

    @MutationMapping
    public Mono<Boolean> deleteRole(@Argument String id) {
        log.info("GraphQL request: deleteRole with id: {}", id);
        return roleGraphQLService.deleteRole(Long.valueOf(id));
    }

    private Role convertToRole(Map<String, Object> input) {
        return Role.builder()
                .name((String) input.get("name"))
                .build();
    }
}