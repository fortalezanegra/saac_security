package br.com.adbrascosmopolis.saac_security.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleBadCredentials_deveRetornar401() {
        ResponseEntity<Map<String, Object>> resp = handler.handleBadCredentials(new BadCredentialsException("Credenciais inválidas"));

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(resp.getBody().get("mensagem")).isEqualTo("Credenciais inválidas");
    }

    @Test
    void handleTokenRefresh_deveRetornar403() {
        ResponseEntity<Map<String, Object>> resp = handler.handleTokenRefresh(new TokenRefreshException("Token expirado"));

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void handleEmailJaCadastrado_deveRetornar409() {
        ResponseEntity<Map<String, Object>> resp = handler.handleEmailJaCadastrado(
                new EmailJaCadastradoException("E-mail duplicado"));

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void handleResourceNotFound_deveRetornar404() {
        ResponseEntity<Map<String, Object>> resp = handler.handleResourceNotFound(
                new ResourceNotFoundException("Não encontrado"));

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void handleAccessDeniedCustom_deveRetornar403() {
        ResponseEntity<Map<String, Object>> resp = handler.handleAccessDeniedCustom(
                new AccessDeniedCustomException("Sem permissão"));

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void handleAccessDenied_deveRetornar403() {
        ResponseEntity<Map<String, Object>> resp = handler.handleAccessDenied(new AccessDeniedException("negado"));

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(resp.getBody().get("mensagem")).isEqualTo("Acesso negado.");
    }

    @Test
    void handleSelfActionNotAllowed_deveRetornar409() {
        ResponseEntity<Map<String, Object>> resp = handler.handleSelfActionNotAllowed(
                new SelfActionNotAllowedException("Não pode excluir a si mesmo"));

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void handleInvalidCredentials_deveRetornar403() {
        ResponseEntity<Map<String, Object>> resp = handler.handleInvalidCredentials(
                new InvalidCredentialsException("Senha incorreta"));

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void handleGeneric_deveRetornar500() {
        ResponseEntity<Map<String, Object>> resp = handler.handleGeneric(new RuntimeException("erro qualquer"));

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(resp.getBody().get("mensagem")).isEqualTo("Erro interno no servidor. Contate o suporte.");
    }
}
