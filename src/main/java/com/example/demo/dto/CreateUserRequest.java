package com.example.demo.dto;

import com.example.demo.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank(message = "El usuario es requerido")
        @Size(min = 3, max = 50, message = "El usuario debe tener entre 3 y 50 caracteres")
        @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Solo letras, números, punto, guion y guion bajo")
        String username,

        @NotBlank(message = "La contraseña es requerida")
        @Size(min = 6, max = 72, message = "La contraseña debe tener entre 6 y 72 caracteres")
        String password,

        @NotBlank(message = "El nombre es requerido")
        @Size(max = 150, message = "Máximo 150 caracteres")
        String nombre,

        @NotBlank(message = "El email es requerido")
        @Email(message = "El email no es válido")
        @Size(max = 150, message = "Máximo 150 caracteres")
        String email,

        @NotNull(message = "El rol es requerido")
        Role role
) {}
