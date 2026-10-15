package com.example.demo.service;

import com.example.demo.dto.CreateUserRequest;
import com.example.demo.dto.UserResponse;
import java.util.List;

public interface UserService {

    List<UserResponse> findAll();

    UserResponse create(CreateUserRequest request);
}
