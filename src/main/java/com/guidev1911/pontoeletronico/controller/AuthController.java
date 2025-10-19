package com.guidev1911.pontoeletronico.controller;

import com.guidev1911.pontoeletronico.model.User;
import com.guidev1911.pontoeletronico.repository.UserRepository;
import com.guidev1911.pontoeletronico.security.JwtUtil;
import com.guidev1911.pontoeletronico.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepo;
    private final PasswordEncoder encoder;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> req) {
        String username = req.get("username");
        String password = req.get("password");

        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.isEnabled()) {
            throw new RuntimeException("User disabled");
        }

        if (!encoder.matches(password, user.getPasswordHash())) {
            throw new RuntimeException("Invalid password");
        }

        if (user.isMustChangePassword()) {
            return Map.of("mustChangePassword", true, "message", "Password change required");
        }

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
        return Map.of("token", token, "username", user.getUsername());
    }

    @PostMapping("/change-password")
    public Map<String, String> changePassword(@RequestBody Map<String, String> req) {
        String username = req.get("username");
        String newPassword = req.get("newPassword");

        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.isMustChangePassword()) {
            throw new RuntimeException("Password change not allowed");
        }

        userService.updatePassword(user, newPassword);
        return Map.of("message", "Password changed successfully");
    }
}