package dev.rm.service;

import java.util.List;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import dev.rm.model.User;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UserService {

    public UserService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Value("${az.function.user.dev.url}")
    private String azFunctionUserUrl;

    private final RestTemplate restTemplate;

    public List<User> getAllUsers() {
        String url = azFunctionUserUrl + "/getAllUsersFunction";
        return restTemplate.getForObject(url, List.class);
    }

    public User getUserById(UUID userId) {
        String url = azFunctionUserUrl + "/getUserFunction?userId=" + userId;
        return restTemplate.getForObject(url, User.class);
    }

    public User createUser(User user) {
        String url = azFunctionUserUrl + "/createUserFunction";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<User> request = new HttpEntity<>(user, headers);

        log.debug("Sending request to Azure Function: {}", user);

        try {
            ResponseEntity<User> response = restTemplate.postForEntity(
                    url,
                    request,
                    User.class);
            log.info("User created successfully with status: {}", response.getStatusCode());
            return response.getBody();
        } catch (RestClientException e) {
            log.error("Error calling Azure Function: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create user", e);
        }
    }

    public User updateUser(UUID userId, User user) {
        String url = azFunctionUserUrl + "/updateUserFunction?userId=" + userId;
        restTemplate.put(url, user);
        return user;
    }

    public void deleteUser(UUID userId) {
        String url = azFunctionUserUrl + "/deleteUserFunction?userId=" + userId;
        restTemplate.delete(url);
    }

}
