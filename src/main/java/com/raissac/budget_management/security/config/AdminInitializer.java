package com.raissac.budget_management.security.config;

import com.raissac.budget_management.security.entity.Role;
import com.raissac.budget_management.security.entity.User;
import com.raissac.budget_management.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class AdminInitializer {

    private static final Logger logger = LoggerFactory.getLogger(AdminInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @EventListener(ApplicationReadyEvent.class)
    public void insertAdminIfNotExists() {

        String adminEmail = "admin@mail.com";
        boolean adminExist = userRepository.findByEmail(adminEmail).isPresent();

        if (!adminExist) {
            User admin = User.builder()
                    .firstName("Admin")
                    .lastName("Account")
                    .email(adminEmail)
                    .password(passwordEncoder.encode("admin123"))
                    .dateOfBirth(LocalDate.now())
                    .role(Role.ADMIN)
                    .build();

            userRepository.save(admin);
            logger.info("Admin created");
        }
        else {
            logger.info("Admin already exists");
        }
    }

}
