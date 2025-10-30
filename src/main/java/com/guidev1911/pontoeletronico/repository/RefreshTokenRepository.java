package com.guidev1911.pontoeletronico.repository;

import com.guidev1911.pontoeletronico.model.RefreshToken;
import com.guidev1911.pontoeletronico.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    void deleteByUser(User user);
}