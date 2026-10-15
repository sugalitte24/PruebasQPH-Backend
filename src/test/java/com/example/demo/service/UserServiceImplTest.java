package com.example.demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.demo.dto.CreateUserRequest;
import com.example.demo.dto.UserResponse;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.exception.ConflictException;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private final CreateUserRequest request = new CreateUserRequest(
            "  APerez ", "secret123", "Ana Pérez", "Ana@Empresa.com", Role.USER);

    @Test
    @DisplayName("Crea el usuario con la contraseña cifrada y username/email normalizados")
    void create_success() {
        when(userRepository.existsByUsername("aperez")).thenReturn(false);
        when(userRepository.existsByEmail("ana@empresa.com")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("$2a$10$hash");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponse response = userService.create(request);

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(saved.capture());
        assertThat(saved.getValue().getPassword()).isEqualTo("$2a$10$hash");
        assertThat(saved.getValue().getUsername()).isEqualTo("aperez");
        assertThat(saved.getValue().getEmail()).isEqualTo("ana@empresa.com");
        assertThat(response.role()).isEqualTo(Role.USER);
    }

    @Test
    @DisplayName("Rechaza un username que ya existe")
    void create_duplicateUsername() {
        when(userRepository.existsByUsername("aperez")).thenReturn(true);

        assertThatThrownBy(() -> userService.create(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("aperez");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Rechaza un email que ya está registrado")
    void create_duplicateEmail() {
        when(userRepository.existsByUsername("aperez")).thenReturn(false);
        when(userRepository.existsByEmail("ana@empresa.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.create(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("ana@empresa.com");

        verify(userRepository, never()).save(any());
    }
}
