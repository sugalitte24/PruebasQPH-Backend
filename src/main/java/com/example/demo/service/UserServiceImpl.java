package com.example.demo.service;

import com.example.demo.dto.CreateUserRequest;
import com.example.demo.dto.UserResponse;
import com.example.demo.entity.User;
import com.example.demo.exception.ConflictException;
import com.example.demo.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        return repository.findAll(Sort.by("id"))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public UserResponse create(CreateUserRequest request) {
        String username = request.username().trim().toLowerCase();
        String email = request.email().trim().toLowerCase();

        if (repository.existsByUsername(username)) {
            throw new ConflictException("El usuario '" + username + "' ya existe");
        }
        if (repository.existsByEmail(email)) {
            throw new ConflictException("El email '" + email + "' ya está registrado");
        }

        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(request.password()))
                .nombre(request.nombre().trim())
                .email(email)
                .role(request.role())
                .build();

        return toResponse(repository.save(user));
    }

    private UserResponse toResponse(User u) {
        return new UserResponse(u.getId(), u.getUsername(), u.getNombre(),
                u.getEmail(), u.getRole(), u.getCreatedAt());
    }
}
