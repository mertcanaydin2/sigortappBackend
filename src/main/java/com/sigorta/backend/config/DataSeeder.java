package com.sigorta.backend.config;

import com.sigorta.backend.entity.Role;
import com.sigorta.backend.entity.User;
import com.sigorta.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private static final String DEFAULT_ADMIN_USERNAME = "admin";
    private static final String DEFAULT_ADMIN_PASSWORD = "123456";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }

        User adminUser = User.builder()
                .username(DEFAULT_ADMIN_USERNAME)
                .password(passwordEncoder.encode(DEFAULT_ADMIN_PASSWORD))
                .role(Role.ADMIN)
                .build();

        userRepository.save(adminUser);
    }
}
