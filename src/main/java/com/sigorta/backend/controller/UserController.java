package com.sigorta.backend.controller;

import com.sigorta.backend.dto.auth.CreateUserRequest;
import com.sigorta.backend.dto.auth.UserResponse;
import com.sigorta.backend.entity.User;
import com.sigorta.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/api/users")
    public ResponseEntity<List<UserResponse>> getUsers() {
        List<UserResponse> users = userRepository.findAll()
                .stream()
                .map(this::toUserResponse)
                .toList();

        return ResponseEntity.ok(users);
    }

    @PostMapping("/api/users/create")
    public ResponseEntity<UserResponse> createUser(@RequestBody CreateUserRequest createUserRequest) {
        validateCreateUserRequest(createUserRequest);

        if (userRepository.findByUsername(createUserRequest.username().trim()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }

        User user = User.builder()
                .username(createUserRequest.username().trim())
                .password(passwordEncoder.encode(createUserRequest.password()))
                .role(createUserRequest.role())
                .build();

        User savedUser = userRepository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED).body(toUserResponse(savedUser));
    }

    private void validateCreateUserRequest(CreateUserRequest createUserRequest) {
        if (createUserRequest == null
                || createUserRequest.username() == null
                || createUserRequest.username().isBlank()
                || createUserRequest.password() == null
                || createUserRequest.password().isBlank()
                || createUserRequest.role() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username, password and role are required");
        }
    }

    private UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getRole().name()
        );
    }
}
