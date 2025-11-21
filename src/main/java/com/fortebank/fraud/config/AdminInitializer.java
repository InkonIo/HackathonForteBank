package com.fortebank.fraud.config;

import com.fortebank.fraud.auth.entity.User;
import com.fortebank.fraud.auth.repository.UserRepository;
import com.fortebank.fraud.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Класс для инициализации начальных данных, в частности, пользователя-администратора.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class AdminInitializer {

    private final AuthService authService;
    private final UserRepository userRepository;

    @Bean
    public CommandLineRunner initAdminUser() {
        return args -> {
            final String adminEmail = "forte@bank.kz";
            final String adminPassword = "admin123";
            final String adminFullName = "ForteBank Admin";
            final User.UserRole adminRole = User.UserRole.ADMIN;

            if (!userRepository.existsByEmail(adminEmail)) {
                log.info("Creating initial ADMIN user: {}", adminEmail);
                try {
                    authService.registerUser(adminEmail, adminPassword, adminFullName, adminRole);
                    log.info("Successfully created ADMIN user: {}", adminEmail);
                } catch (Exception e) {
                    log.error("Failed to create initial ADMIN user: {}", adminEmail, e);
                }
            } else {
                log.info("ADMIN user already exists: {}", adminEmail);
            }
        };
    }
}
