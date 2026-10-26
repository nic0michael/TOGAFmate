package com.togafmate.backend.config;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.togafmate.backend.entity.User;
import com.togafmate.backend.entity.UserPermission;
import com.togafmate.backend.repository.UserRepository;

/** Optionally provisions the first administrator from environment configuration. */
@Component
public class BootstrapAdministrator implements ApplicationRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(BootstrapAdministrator.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String userID;
    private final String password;

    /** Creates the optional bootstrap administrator runner. */
    public BootstrapAdministrator(UserRepository userRepository, PasswordEncoder passwordEncoder,
            @Value("${APP_BOOTSTRAP_ADMIN_USER_ID:}") String userID,
            @Value("${APP_BOOTSTRAP_ADMIN_PASSWORD:}") String password) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userID = userID;
        this.password = password;
    }

    /** Creates an administrator when both bootstrap environment values are configured. */
    @Override
    public void run(ApplicationArguments args) {
        if (userID.isBlank() && password.isBlank()) {
            return;
        }
        if (userID.isBlank() || password.isBlank()) {
            throw new IllegalStateException("Both bootstrap administrator settings must be configured");
        }
        if (!userRepository.existsById(userID)) {
            userRepository.save(new User(userID, passwordEncoder.encode(password),
                    List.of(UserPermission.ADMINISTRATOR_ACCESS)));
            LOGGER.info("Bootstrapped administrator account");
        }
    }
}
