package com.sigorta.backend.config;

import com.sigorta.backend.entity.Role;
import com.sigorta.backend.entity.User;
import com.sigorta.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DataSeederTest {

    @Test
    void createsDefaultAdminWhenUserTableIsEmpty() throws Exception {
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        when(userRepository.count()).thenReturn(0L);
        when(passwordEncoder.encode("123456")).thenReturn("encoded-password");
        DataSeeder dataSeeder = new DataSeeder(userRepository, passwordEncoder);

        dataSeeder.run();

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertEquals("admin", savedUser.getUsername());
        assertEquals("encoded-password", savedUser.getPassword());
        assertEquals(Role.ADMIN, savedUser.getRole());
    }

    @Test
    void doesNotCreateAdminWhenAUserAlreadyExists() throws Exception {
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        when(userRepository.count()).thenReturn(1L);
        DataSeeder dataSeeder = new DataSeeder(userRepository, passwordEncoder);

        dataSeeder.run();

        verify(passwordEncoder, never()).encode("123456");
        verify(userRepository, never()).save(org.mockito.ArgumentMatchers.any(User.class));
    }
}
