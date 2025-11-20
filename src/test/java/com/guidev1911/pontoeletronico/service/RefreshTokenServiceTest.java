package com.guidev1911.pontoeletronico.service;

import static org.junit.jupiter.api.Assertions.*;

import com.guidev1911.pontoeletronico.exceptions.BusinessException;
import com.guidev1911.pontoeletronico.model.RefreshToken;
import com.guidev1911.pontoeletronico.model.User;
import com.guidev1911.pontoeletronico.repository.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;

class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository repo;

    @InjectMocks
    private RefreshTokenService service;

    @BeforeEach
    void configurar() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void criar_DeveExcluirTokensAntigosESalvarNovo() {

        User user = new User();
        user.setId(1L);

        RefreshToken savedToken = RefreshToken.builder()
                .id(10L)
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiryDate(Instant.now().plusSeconds(7 * 24 * 60 * 60))
                .build();

        when(repo.save(any(RefreshToken.class))).thenReturn(savedToken);

        RefreshToken resultado = service.create(user);

        verify(repo).deleteByUser(user);
        verify(repo).save(any(RefreshToken.class));

        assertNotNull(resultado);
        assertNotNull(resultado.getToken());
        assertEquals(user, resultado.getUser());
        assertTrue(resultado.getExpiryDate().isAfter(Instant.now()));
    }

    @Test
    void validar_DeveRetornarToken_QuandoValido() {

        RefreshToken token = RefreshToken.builder()
                .token("validToken")
                .expiryDate(Instant.now().plusSeconds(3600))
                .build();

        when(repo.findByToken("validToken")).thenReturn(Optional.of(token));

        RefreshToken resultado = service.validate("validToken");

        assertEquals(token, resultado);
        verify(repo, never()).delete(any());
    }

    @Test
    void validar_DeveLancarExcecao_QuandoTokenNaoEncontrado() {
        when(repo.findByToken("desconhecido")).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> service.validate("desconhecido"));
    }

    @Test
    void validar_DeveExcluirELancarExcecao_QuandoTokenExpirado() {

        RefreshToken expirado = RefreshToken.builder()
                .token("expirado")
                .expiryDate(Instant.now().minusSeconds(60))
                .build();

        when(repo.findByToken("expirado")).thenReturn(Optional.of(expirado));

        BusinessException ex = assertThrows(BusinessException.class, () -> service.validate("expirado"));

        assertEquals("Refresh token expired", ex.getMessage());
        verify(repo).delete(expirado);
    }

    @Test
    void deletar_DeveChamarRepositorio() {
        RefreshToken token = new RefreshToken();
        service.delete(token);
        verify(repo).delete(token);
    }
}
