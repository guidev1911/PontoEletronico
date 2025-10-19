package com.guidev1911.pontoeletronico.repository;

import com.guidev1911.pontoeletronico.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}