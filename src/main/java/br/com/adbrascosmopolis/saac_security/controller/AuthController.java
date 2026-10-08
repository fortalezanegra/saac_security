package br.com.adbrascosmopolis.saac_security.controller;

import br.com.adbrascosmopolis.saac_security.dto.login.LoginRequestDTO;
import br.com.adbrascosmopolis.saac_security.dto.login.LoginResponseDTO;
import br.com.adbrascosmopolis.saac_security.dto.refresh_token.RefreshTokenRequestDTO;
import br.com.adbrascosmopolis.saac_security.dto.refresh_token.TokenResponseDTO;
import br.com.adbrascosmopolis.saac_security.exception.TokenRefreshException;
import br.com.adbrascosmopolis.saac_security.model.RefreshToken;
import br.com.adbrascosmopolis.saac_security.model.User;
import br.com.adbrascosmopolis.saac_security.repository.UserRepository;
import br.com.adbrascosmopolis.saac_security.security.JwtService;
import br.com.adbrascosmopolis.saac_security.service.RefreshTokenService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtService jwtService,
                          UserRepository userRepository,
                          RefreshTokenService refreshTokenService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO dto) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getSenha())
            );
        } catch (BadCredentialsException ex) {
            throw new BadCredentialsException("E-mail ou senha inválidos");
        }

        User user = userRepository.findByEmailAndDeletedAtIsNull(dto.getEmail())
                .orElseThrow(() -> new BadCredentialsException("E-mail ou senha inválidos"));

        String accessToken = jwtService.tokenGenerate(
                user.getEmail(),
                user.getUserId(),
                user.getScopeType().name(),
                user.getUnityId()
        );

        // Gera (e rotaciona, se já existir) o refresh token do usuário
        RefreshToken refreshToken = refreshTokenService.create(user.getUserId());

        return ResponseEntity.ok(new LoginResponseDTO(accessToken, refreshToken.getToken(), "Bearer"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponseDTO> refresh(@Valid @RequestBody RefreshTokenRequestDTO request) {
        RefreshToken storedToken = refreshTokenService.validateRefreshToken(request.refreshToken());

        User user = userRepository.findById(storedToken.getUserId())
                .orElseThrow(() -> new TokenRefreshException("Usuário não encontrado."));

        String newAccessToken = jwtService.tokenGenerate(
                user.getEmail(),
                user.getUserId(),
                user.getScopeType().name(),
                user.getUnityId()
        );

        // Rotação: revoga o token usado e emite um novo
        RefreshToken newRefreshToken = refreshTokenService.create(user.getUserId());

        return ResponseEntity.ok(new TokenResponseDTO(newAccessToken, newRefreshToken.getToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequestDTO request) {
        RefreshToken storedToken = refreshTokenService.validateRefreshToken(request.refreshToken());
        refreshTokenService.revokeAllByUser(storedToken.getUserId());
        return ResponseEntity.noContent().build();
    }

}
