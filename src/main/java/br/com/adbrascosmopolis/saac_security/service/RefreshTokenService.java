package br.com.adbrascosmopolis.saac_security.service;

import br.com.adbrascosmopolis.saac_security.exception.TokenRefreshException;
import br.com.adbrascosmopolis.saac_security.model.RefreshToken;
import br.com.adbrascosmopolis.saac_security.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository repository;

    @Value("${jwt.refresh-token.expiration-days:7}")
    private int expirationDays;

    public RefreshTokenService(RefreshTokenRepository repository) {
        this.repository = repository;
    }

    /**
     * Gera e persiste um novo refresh token para o usuário.
     * Revoga tokens anteriores ativos (rotação de token).
     */
    public RefreshToken criar(Long userId) {
        repository.revokeAllByUserId(userId); // garante um único token válido por usuário

        RefreshToken refreshToken = new RefreshToken(
                UUID.randomUUID().toString(),
                userId,
                LocalDateTime.now().plusDays(expirationDays)
        );

        return repository.save(refreshToken);
    }

    /**
     * Valida se o token existe, não está revogado e não expirou.
     * Lança TokenRefreshException se inválido ou expirado.
     */
    public RefreshToken validarRefreshToken(String token) {
        RefreshToken refreshToken = repository.findByTokenAndRevokedFalse(token)
                .orElseThrow(() -> new TokenRefreshException("Refresh token inválido ou revogado."));

        if (refreshToken.isExpired()) {
            throw new TokenRefreshException("Refresh token expirado. Faça login novamente.");
        }

        return refreshToken;
    }

    public void revogarTodosDoUsuario(Long userId) {
        repository.revokeAllByUserId(userId);
    }
}
