package com.guidev1911.pontoeletronico.controller;

import com.guidev1911.pontoeletronico.exceptions.BusinessException;
import com.guidev1911.pontoeletronico.model.RefreshToken;
import com.guidev1911.pontoeletronico.model.User;
import com.guidev1911.pontoeletronico.repository.UserRepository;
import com.guidev1911.pontoeletronico.security.JwtUtil;
import com.guidev1911.pontoeletronico.service.RefreshTokenService;
import com.guidev1911.pontoeletronico.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> req) {
        String username = req.get("username");
        String password = req.get("password");

        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new BusinessException("Invalid username or password"));

        if (!user.isEnabled()) throw new BusinessException("User is disabled");
        if (!encoder.matches(password, user.getPasswordHash()))
            throw new BusinessException("Invalid username or password");
        if (user.isMustChangePassword())
            return ResponseEntity.status(403).body(Map.of("mustChangePassword", true));

        String accessToken = jwtUtil.generateToken(user.getUsername(), user.getRole());
        RefreshToken refreshToken = refreshTokenService.create(user);

        return ResponseEntity.ok(Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshToken.getToken(),
                "username", user.getUsername(),
                "role", user.getRole()
        ));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody Map<String, String> req) {
        String refreshTokenStr = req.get("refreshToken");

        RefreshToken refresh = refreshTokenService.validate(refreshTokenStr);
        User user = refresh.getUser();

        String newAccess = jwtUtil.generateToken(user.getUsername(), user.getRole());
        RefreshToken newRefresh = refreshTokenService.create(user);

        return ResponseEntity.ok(Map.of(
                "accessToken", newAccess,
                "refreshToken", newRefresh.getToken()
        ));
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> req) {
        String username = req.get("username");
        String newPassword = req.get("newPassword");

        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new BusinessException("User not found"));

        if (!user.isMustChangePassword())
            throw new BusinessException("Password change not allowed");

        userService.updatePassword(user, newPassword);
        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }
}