package com.guidev1911.pontoeletronico.controller;

import com.guidev1911.pontoeletronico.dto.CreateUserRequest;
import com.guidev1911.pontoeletronico.dto.UserResponse;
import com.guidev1911.pontoeletronico.model.User;
import com.guidev1911.pontoeletronico.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService service;

    @PostMapping("/create-user")
    public ResponseEntity<UserResponse> createUser(@RequestBody CreateUserRequest request) {
        return ResponseEntity.ok(service.createUser(request));
    }

    @PostMapping("/reset-password/{id}")
    public ResponseEntity<Map<String, String>> resetPassword(@PathVariable Long id) {
        service.resetPassword(id);
        return ResponseEntity.ok(Map.of("message", "Password reset to default"));
    }
}