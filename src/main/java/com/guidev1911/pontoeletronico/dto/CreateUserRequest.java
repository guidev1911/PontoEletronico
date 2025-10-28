package com.guidev1911.pontoeletronico.dto;


import lombok.Data;

@Data
public class CreateUserRequest {
    private String username;
    private String role = "USER";
}