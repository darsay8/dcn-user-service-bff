package dev.rm.init;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class DataInitializer implements CommandLineRunner {

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Initializing data...");
    }

}
