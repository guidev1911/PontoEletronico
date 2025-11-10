package com.guidev1911.pontoeletronico.service;

import static org.junit.jupiter.api.Assertions.*;

import com.guidev1911.pontoeletronico.dto.CreateUserRequest;
import com.guidev1911.pontoeletronico.dto.UserResponse;
import com.guidev1911.pontoeletronico.exceptions.BusinessException;
import com.guidev1911.pontoeletronico.mapper.UserMapper;
import com.guidev1911.pontoeletronico.model.User;
import com.guidev1911.pontoeletronico.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository repo;

    @Mock
    private PasswordEncoder encoder;

    @Mock
    private UserMapper mapper;

    @InjectMocks
    private UserService service;

    private final String DEFAULT_PASSWORD = "detran2025";

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createUser_ShouldCreateNewUser_WhenUsernameNotExists() {

        CreateUserRequest req = new CreateUserRequest();
        req.setUsername("guilherme");
        req.setRole("USER");

        User entity = new User();
        entity.setUsername(req.getUsername());
        entity.setRole(req.getRole());

        User saved = new User();
        saved.setId(1L);
        saved.setUsername(req.getUsername());
        saved.setRole(req.getRole());

        UserResponse response = new UserResponse();
        response.setId(1L);
        response.setUsername("guilherme");

        when(repo.findByUsername("guilherme")).thenReturn(Optional.empty());
        when(mapper.toEntity(req)).thenReturn(entity);
        when(encoder.encode(DEFAULT_PASSWORD)).thenReturn("encodedPass");
        when(repo.save(entity)).thenReturn(saved);
        when(mapper.toResponse(saved)).thenReturn(response);

        UserResponse result = service.createUser(req);

        assertEquals("guilherme", result.getUsername());
        verify(repo).save(entity);
        verify(encoder).encode(DEFAULT_PASSWORD);
    }

    @Test
    void createUser_ShouldThrowException_WhenUsernameExists() {

        CreateUserRequest req = new CreateUserRequest();
        req.setUsername("existing");

        when(repo.findByUsername("existing")).thenReturn(Optional.of(new User()));

        assertThrows(BusinessException.class, () -> service.createUser(req));
        verify(repo, never()).save(any());
    }

    @Test
    void resetPassword_ShouldEncodeAndReset() {

        User user = new User();
        user.setId(1L);
        user.setPasswordHash("oldHash");

        when(repo.findById(1L)).thenReturn(Optional.of(user));
        when(encoder.encode(DEFAULT_PASSWORD)).thenReturn("newHash");

        service.resetPassword(1L);

        assertEquals("newHash", user.getPasswordHash());
        assertTrue(user.isMustChangePassword());
        verify(repo).save(user);
    }

    @Test
    void resetPassword_ShouldThrow_WhenUserNotFound() {
        when(repo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> service.resetPassword(1L));
    }

    @Test
    void updatePassword_ShouldEncodeNewPassword_WhenValid() {

        User user = new User();
        user.setId(1L);
        when(encoder.encode("novaSenha")).thenReturn("hashNova");

        service.updatePassword(user, "novaSenha");

        assertEquals("hashNova", user.getPasswordHash());
        assertFalse(user.isMustChangePassword());
        verify(repo).save(user);
    }

    @Test
    void updatePassword_ShouldThrow_WhenPasswordTooShort() {
        User user = new User();
        assertThrows(BusinessException.class, () -> service.updatePassword(user, "123"));
        verify(repo, never()).save(any());
    }
}