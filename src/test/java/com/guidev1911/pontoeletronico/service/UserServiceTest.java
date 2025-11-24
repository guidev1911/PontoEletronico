package com.guidev1911.pontoeletronico.service;

import static org.junit.jupiter.api.Assertions.*;

import com.guidev1911.pontoeletronico.dto.CreateUserRequest;
import com.guidev1911.pontoeletronico.dto.UserResponse;
import com.guidev1911.pontoeletronico.exceptions.BusinessException;
import com.guidev1911.pontoeletronico.mapper.UserMapper;
import com.guidev1911.pontoeletronico.model.User;
import com.guidev1911.pontoeletronico.model.enums.Role;
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
    void configurar() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void criarUsuario_DeveCriarNovoUsuario_QuandoUsernameNaoExiste() {

        CreateUserRequest req = new CreateUserRequest();
        req.setUsername("guilherme");
        req.setRole(Role.USER);

        User entity = new User();
        entity.setUsername("guilherme");
        entity.setRole(Role.USER);

        User salvo = new User();
        salvo.setId(1L);
        salvo.setUsername("guilherme");
        salvo.setRole(Role.USER);

        UserResponse response = new UserResponse();
        response.setId(1L);
        response.setUsername("guilherme");

        when(repo.findByUsername("guilherme")).thenReturn(Optional.empty());
        when(mapper.toEntity(req)).thenReturn(entity);
        when(encoder.encode(DEFAULT_PASSWORD)).thenReturn("senhaCodificada");
        when(repo.save(entity)).thenReturn(salvo);
        when(mapper.toResponse(salvo)).thenReturn(response);

        UserResponse resultado = service.createUser(req);

        assertEquals("guilherme", resultado.getUsername());
        verify(repo).save(entity);
        verify(encoder).encode(DEFAULT_PASSWORD);
    }

    @Test
    void criarUsuario_DeveLancarExcecao_QuandoUsernameJaExiste() {

        CreateUserRequest req = new CreateUserRequest();
        req.setUsername("existente");

        when(repo.findByUsername("existente"))
                .thenReturn(Optional.of(new User()));

        assertThrows(BusinessException.class, () -> service.createUser(req));
        verify(repo, never()).save(any());
    }

    @Test
    void resetarSenha_DeveGerarHashENecessidadeDeAlteracao() {

        User user = new User();
        user.setId(1L);
        user.setPasswordHash("hashAntigo");

        when(repo.findById(1L)).thenReturn(Optional.of(user));
        when(encoder.encode(DEFAULT_PASSWORD)).thenReturn("novoHash");

        service.resetPassword(1L);

        assertEquals("novoHash", user.getPasswordHash());
        assertTrue(user.isMustChangePassword());
        verify(repo).save(user);
    }

    @Test
    void resetarSenha_DeveLancarExcecao_QuandoUsuarioNaoEncontrado() {

        when(repo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> service.resetPassword(1L));
    }

    @Test
    void atualizarSenha_DeveCodificarNovaSenha_QuandoValida() {

        User user = new User();
        user.setId(1L);

        when(encoder.encode("novaSenha")).thenReturn("hashNova");

        service.updatePassword(user, "novaSenha");

        assertEquals("hashNova", user.getPasswordHash());
        assertFalse(user.isMustChangePassword());
        verify(repo).save(user);
    }

    @Test
    void atualizarSenha_DeveLancarExcecao_QuandoSenhaMuitoCurta() {

        User user = new User();

        assertThrows(BusinessException.class, () -> service.updatePassword(user, "123"));
        verify(repo, never()).save(any());
    }
}
