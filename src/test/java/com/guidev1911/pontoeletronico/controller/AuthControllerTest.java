package com.guidev1911.pontoeletronico.controller;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.guidev1911.pontoeletronico.exceptions.GlobalExceptionHandler;
import com.guidev1911.pontoeletronico.model.RefreshToken;
import com.guidev1911.pontoeletronico.model.User;
import com.guidev1911.pontoeletronico.model.enums.Role;
import com.guidev1911.pontoeletronico.repository.UserRepository;
import com.guidev1911.pontoeletronico.security.JwtUtil;
import com.guidev1911.pontoeletronico.service.RefreshTokenService;
import com.guidev1911.pontoeletronico.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.*;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock private UserRepository userRepo;
    @Mock private PasswordEncoder encoder;
    @Mock private JwtUtil jwtUtil;
    @Mock private UserService userService;
    @Mock private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthController controller;

    ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void configurar() {
        MockitoAnnotations.openMocks(this);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)

                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void deveRetornar403QuandoUsuarioPrecisaTrocarSenha() throws Exception {
        User user = new User();
        user.setUsername("john");
        user.setEnabled(true);
        user.setPasswordHash("xx");
        user.setMustChangePassword(true);

        when(userRepo.findByUsername("john")).thenReturn(Optional.of(user));
        when(encoder.matches("123","xx")).thenReturn(true);

        Map<String,String> req = Map.of("username","john","password","123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mustChangePassword").value(true));
    }

    @Test
    void deveFalharQuandoUsuarioNaoExistir() throws Exception {
        when(userRepo.findByUsername("john")).thenReturn(Optional.empty());

        Map<String,String> req = Map.of("username","john","password","123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveFalharQuandoSenhaEstiverErrada() throws Exception {
        User user = new User();
        user.setEnabled(true);
        user.setPasswordHash("correct");

        when(userRepo.findByUsername("john")).thenReturn(Optional.of(user));
        when(encoder.matches("wrong","correct")).thenReturn(false);

        Map<String,String> req = Map.of("username","john","password","wrong");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveFalharQuandoUsuarioEstiverDesativado() throws Exception {
        User user = new User();
        user.setEnabled(false);

        when(userRepo.findByUsername("john")).thenReturn(Optional.of(user));

        Map<String,String> req = Map.of("username","john","password","123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }



    @Test
    void deveGerarNovosTokensAoAtualizarRefreshToken() throws Exception {
        User user = new User();
        user.setUsername("john");
        user.setRole(Role.USER);

        RefreshToken antigo = RefreshToken.builder()
                .token("old")
                .user(user)
                .expiryDate(Instant.now().plusSeconds(3600))
                .build();

        RefreshToken novo = RefreshToken.builder()
                .token("new-refresh")
                .user(user)
                .expiryDate(Instant.now().plusSeconds(3600))
                .build();

        when(refreshTokenService.validate("old")).thenReturn(antigo);
        when(jwtUtil.generateToken("john","USER")).thenReturn("new-access");
        when(refreshTokenService.create(user)).thenReturn(novo);

        Map<String,String> req = Map.of("refreshToken","old");

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access"))
                .andExpect(jsonPath("$.refreshToken").value("new-refresh"));
    }



    @Test
    void deveTrocarSenhaComSucesso() throws Exception {
        User user = new User();
        user.setUsername("john");
        user.setMustChangePassword(true);

        when(userRepo.findByUsername("john")).thenReturn(Optional.of(user));

        Map<String,String> req = Map.of("username","john","newPassword","abc123");

        mockMvc.perform(post("/api/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password changed successfully"));

        verify(userService).updatePassword(user, "abc123");
    }

    @Test
    void deveFalharAoTrocarSenhaQuandoUsuarioNaoExistir() throws Exception {
        when(userRepo.findByUsername("john")).thenReturn(Optional.empty());

        Map<String,String> req = Map.of("username","john","newPassword","123");

        mockMvc.perform(post("/api/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveFalharAoTrocarSenhaQuandoUsuarioNaoPrecisarTrocar() throws Exception {
        User user = new User();
        user.setUsername("john");
        user.setMustChangePassword(false);

        when(userRepo.findByUsername("john")).thenReturn(Optional.of(user));

        Map<String,String> req = Map.of("username","john","newPassword","123");

        mockMvc.perform(post("/api/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }
}