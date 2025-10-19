package com.guidev1911.pontoeletronico.controller;

import com.guidev1911.pontoeletronico.model.User;
import com.guidev1911.pontoeletronico.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserService service;

    public AdminController(UserService service) {
        this.service = service;
    }

    @PostMapping("/create-user")
    public User createUser(@RequestParam String username, @RequestParam(defaultValue = "USER") String role) {
        return service.createUser(username, role);
    }

    @PostMapping("/reset-password/{id}")
    public String resetPassword(@PathVariable Long id) {
        service.resetPassword(id);
        return "Password reset to default.";
    }
}