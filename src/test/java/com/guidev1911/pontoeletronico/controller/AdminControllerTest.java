package com.guidev1911.pontoeletronico.controller;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.guidev1911.pontoeletronico.dto.CreateUserRequest;
import com.guidev1911.pontoeletronico.dto.UserResponse;
import com.guidev1911.pontoeletronico.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.*;

import java.time.Instant;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AdminControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private AdminController controller;

    private ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void configurar() {
        MockitoAnnotations.openMocks(this);
        mockMvc = org.springframework.test.web.servlet.setup.MockMvcBuilders
                .standaloneSetup(controller)
                .build();
    }

    @Test
    void deveCriarUsuarioComSucesso() throws Exception {
        CreateUserRequest req = new CreateUserRequest();
        req.setUsername("john");

        UserResponse resp = new UserResponse();
        resp.setId(1L);
        resp.setUsername("john");
        resp.setRole("USER");
        resp.setEnabled(true);
        resp.setMustChangePassword(true);
        resp.setCreatedAt(Instant.now());
        resp.setUpdatedAt(Instant.now());

        when(userService.createUser(any())).thenReturn(resp);

        mockMvc.perform(post("/api/admin/create-user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void deveResetarSenhaComSucesso() throws Exception {
        mockMvc.perform(post("/api/admin/reset-password/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password reset to default"));

        verify(userService).resetPassword(5L);
    }
}