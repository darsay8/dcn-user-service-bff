package dev.rm.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;

import dev.rm.model.User;

import dev.rm.service.UserService;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public Flux<User> getUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{userId}")
    public Mono<ResponseEntity<User>> getUserById(@PathVariable UUID userId) {
        return userService.getUserById(userId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Mono<ResponseEntity<Object>> createUser(@RequestBody User user) {
        log.info("Received create user request for username: {}", user.getUsername());
        log.info("Received create user: {}", user);
        return userService.createUser(user)
                .map(savedUser -> ResponseEntity
                        .status(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body((Object) savedUser)) // Cast to Object to match return type
                .onErrorResume(e -> {
                    log.error("Error creating user: {}", e.getMessage(), e);

                    String errorMessage = "Failed to create user.";

                    if (e.getMessage().contains("ORA-00001")) {
                        if (e.getMessage().contains("SYS_C0050205")) {
                            errorMessage = "Error creating user: Username or email already exists.";
                        } else {
                            errorMessage = "Error creating user: Unique constraint violation.";
                        }
                    } else if (e.getMessage().contains("ORA-")) {
                        errorMessage = "Error creating user: Database error occurred. Please contact support.";
                    }

                    Map<String, String> errorBody = new HashMap<>();
                    errorBody.put("message", errorMessage);

                    return Mono.just(ResponseEntity
                            .status(HttpStatus.CONFLICT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(errorBody));
                });
    }

    @PutMapping("/{userId}")
    public Mono<ResponseEntity<User>> updateUser(@PathVariable UUID userId, @RequestBody User user) {
        log.info("Received update user request for userId: {}", userId);
        return userService.updateUser(userId, user)
                .map(updatedUser -> ResponseEntity.ok(updatedUser))
                .onErrorResume(e -> {
                    log.error("Error updating user: {}", e.getMessage());
                    return Mono.just(ResponseEntity.internalServerError().build());
                })
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{userId}")
    public Mono<ResponseEntity<Void>> deleteUser(@PathVariable UUID userId) {
        return userService.deleteUser(userId)
                .then(Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT)))
                .onErrorResume(e -> {
                    log.error("Error deleting user: {}", e.getMessage());
                    return Mono.just(new ResponseEntity<Void>(HttpStatus.INTERNAL_SERVER_ERROR));
                });
    }

}
