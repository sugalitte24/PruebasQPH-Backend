package com.example.demo.dto;

import com.example.demo.entity.Role;
import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String username,
        String nombre,
        String email,
        Role role,
        LocalDateTime createdAt
) {}
