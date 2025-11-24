package com.guidev1911.pontoeletronico.dto;


import com.guidev1911.pontoeletronico.model.enums.Role;
import lombok.Data;

@Data
public class CreateUserRequest {
    private String username;
    private Role role = Role.USER;
}