package com.guidev1911.pontoeletronico.dto;


import lombok.Data;

import java.time.Instant;

@Data
public class UserResponse {
    private Long id;
    private String username;
    private String role;
    private boolean enabled;
    private boolean mustChangePassword;
    private String createdBy;
    private String updatedBy;
    private Instant createdAt;
    private Instant updatedAt;
}