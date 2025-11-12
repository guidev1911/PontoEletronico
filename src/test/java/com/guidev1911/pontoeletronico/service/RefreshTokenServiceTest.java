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
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void create_ShouldDeleteOldTokensAndSaveNew() {

        User user = new User();
        user.setId(1L);

        RefreshToken savedToken = RefreshToken.builder()
                .id(10L)
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiryDate(Instant.now().plusSeconds(7 * 24 * 60 * 60))
                .build();

        when(repo.save(any(RefreshToken.class))).thenReturn(savedToken);

        RefreshToken result = service.create(user);

        verify(repo).deleteByUser(user);
        verify(repo).save(any(RefreshToken.class));
        assertNotNull(result);
        assertNotNull(result.getToken());
        assertEquals(user, result.getUser());
        assertTrue(result.getExpiryDate().isAfter(Instant.now()));
    }

    @Test
    void validate_ShouldReturnToken_WhenValid() {

        RefreshToken token = RefreshToken.builder()
                .token("validToken")
                .expiryDate(Instant.now().plusSeconds(3600))
                .build();

        when(repo.findByToken("validToken")).thenReturn(Optional.of(token));

        RefreshToken result = service.validate("validToken");

        assertEquals(token, result);
        verify(repo, never()).delete(any());
    }

    @Test
    void validate_ShouldThrow_WhenTokenNotFound() {
        when(repo.findByToken("unknown")).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> service.validate("unknown"));
    }

    @Test
    void validate_ShouldDeleteAndThrow_WhenTokenExpired() {

        RefreshToken expired = RefreshToken.builder()
                .token("expired")
                .expiryDate(Instant.now().minusSeconds(60))
                .build();

        when(repo.findByToken("expired")).thenReturn(Optional.of(expired));

        BusinessException ex = assertThrows(BusinessException.class, () -> service.validate("expired"));
        assertEquals("Refresh token expired", ex.getMessage());
        verify(repo).delete(expired);
    }

    @Test
    void delete_ShouldCallRepository() {
        RefreshToken token = new RefreshToken();
        service.delete(token);
        verify(repo).delete(token);
    }
}