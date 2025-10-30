package com.guidev1911.pontoeletronico.service;

import com.guidev1911.pontoeletronico.exceptions.BusinessException;
import com.guidev1911.pontoeletronico.model.RefreshToken;
import com.guidev1911.pontoeletronico.model.User;
import com.guidev1911.pontoeletronico.repository.RefreshTokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class RefreshTokenService {

    private final RefreshTokenRepository repo;

    private static final long REFRESH_TOKEN_VALIDITY = 7 * 24 * 60 * 60;

    public RefreshToken create(User user) {
        repo.deleteByUser(user);

        RefreshToken token = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiryDate(Instant.now().plusSeconds(REFRESH_TOKEN_VALIDITY))
                .build();

        return repo.save(token);
    }

    public RefreshToken validate(String token) {
        RefreshToken refresh = repo.findByToken(token)
                .orElseThrow(() -> new BusinessException("Invalid refresh token"));

        if (refresh.getExpiryDate().isBefore(Instant.now())) {
            repo.delete(refresh);
            throw new BusinessException("Refresh token expired");
        }

        return refresh;
    }

    public void delete(RefreshToken token) {
        repo.delete(token);
    }
}