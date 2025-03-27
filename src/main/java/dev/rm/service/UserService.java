package dev.rm.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Service;
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
        return restTemplate.getForObject(azFunctionUserUrl, List.class);
    }

}
