package com.guidev1911.pontoeletronico.service;

import com.guidev1911.pontoeletronico.dto.CreateUserRequest;
import com.guidev1911.pontoeletronico.dto.UserResponse;
import com.guidev1911.pontoeletronico.exceptions.BusinessException;
import com.guidev1911.pontoeletronico.mapper.UserMapper;
import com.guidev1911.pontoeletronico.model.User;
import com.guidev1911.pontoeletronico.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repo;
    private final PasswordEncoder encoder;
    private final UserMapper mapper;

    private static final String DEFAULT_PASSWORD = "detran2025";

    public UserResponse createUser(CreateUserRequest request) {
        if (repo.findByUsername(request.getUsername()).isPresent()) {
            throw new BusinessException("Username already exists");
        }

        User user = mapper.toEntity(request);
        user.setPasswordHash(encoder.encode(DEFAULT_PASSWORD));

        return mapper.toResponse(repo.save(user));
    }

    public void resetPassword(Long userId) {
        User u = repo.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found"));
        u.setPasswordHash(encoder.encode(DEFAULT_PASSWORD));
        u.setMustChangePassword(true);
        repo.save(u);
    }

    public void updatePassword(User user, String newPassword) {
        if (newPassword == null || newPassword.length() < 6) {
            throw new BusinessException("Password must have at least 6 characters");
        }
        user.setPasswordHash(encoder.encode(newPassword));
        user.setMustChangePassword(false);
        repo.save(user);
    }
}