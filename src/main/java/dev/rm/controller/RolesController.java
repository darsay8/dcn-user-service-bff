package dev.rm.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.rm.model.Role;
import dev.rm.service.RoleService;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/roles")
public class RolesController {

    private final RoleService roleService;

    public RolesController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    public Flux<Role> getRoles() {
        return roleService.getAllRoles();
    }

    @GetMapping("/{roleId}")
    public Mono<ResponseEntity<Role>> getRoleById(@PathVariable Long roleId) {
        return roleService.getRoleById(roleId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Mono<ResponseEntity<Role>> createRole(@RequestBody Role role) {
        log.info("Received create role request for role name: {}", role.getName());
        log.info("Received create role: {}", role);
        return roleService.createRole(role)
                .map(createdRole -> ResponseEntity.ok(createdRole))
                .onErrorResume(e -> {
                    log.error("Error creating role: {}", e.getMessage(), e);
                    return Mono.just(ResponseEntity.internalServerError().build());
                });
    }

    @PutMapping("/{roleId}")
    public Mono<ResponseEntity<Role>> updateRole(@PathVariable Long roleId, @RequestBody Role role) {
        log.info("Received update role request for roleId: {}", roleId);
        return roleService.updateRole(roleId, role)
                .map(updatedRole -> ResponseEntity.ok(updatedRole))
                .onErrorResume(e -> {
                    log.error("Error updating role: {}", e.getMessage(), e);
                    return Mono.just(ResponseEntity.internalServerError().build());
                });
    }

    @DeleteMapping("/{roleId}")
    public Mono<ResponseEntity<Void>> deleteRole(@PathVariable Long roleId) {
        return roleService.deleteRole(roleId)
                .then(Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT)))
                .onErrorResume(e -> {
                    log.error("Error deleting role: {}", e.getMessage(), e);
                    return Mono.just(ResponseEntity.internalServerError().build());
                });
    }

}
