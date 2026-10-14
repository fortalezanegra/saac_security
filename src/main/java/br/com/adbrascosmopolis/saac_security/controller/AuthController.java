package br.com.adbrascosmopolis.saac_security.controller;

import br.com.adbrascosmopolis.saac_security.dto.login.LoginRequestDTO;
import br.com.adbrascosmopolis.saac_security.dto.login.LoginResponseDTO;
import br.com.adbrascosmopolis.saac_security.dto.refresh_token.RefreshTokenRequestDTO;
import br.com.adbrascosmopolis.saac_security.dto.refresh_token.TokenResponseDTO;
import br.com.adbrascosmopolis.saac_security.exception.TokenRefreshException;
import br.com.adbrascosmopolis.saac_security.model.User;
import br.com.adbrascosmopolis.saac_security.model.RefreshToken;
import br.com.adbrascosmopolis.saac_security.repository.UserPermissionRepository;
import br.com.adbrascosmopolis.saac_security.repository.UserRepository;
import br.com.adbrascosmopolis.saac_security.security.JwtService;
import br.com.adbrascosmopolis.saac_security.service.RefreshTokenService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final UserPermissionRepository userPermissionRepository;
    private final RefreshTokenService refreshTokenService;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtService jwtService,
                          UserRepository userRepository,
                          UserPermissionRepository userPermissionRepository,
                          RefreshTokenService refreshTokenService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.userPermissionRepository = userPermissionRepository;
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

        List<String> authorities = userPermissionRepository
                .findEffectivePermissionCodesByUserId(user.getUserId());

        String accessToken = jwtService.tokenGenerate(
                user.getEmail(),
                user.getUserId(),
                user.getUnityId(),
                authorities
        );

        RefreshToken refreshToken = refreshTokenService.create(user.getUserId());

        return ResponseEntity.ok(new LoginResponseDTO(accessToken, refreshToken.getToken(), "Bearer"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponseDTO> refresh(@Valid @RequestBody RefreshTokenRequestDTO request) {
        RefreshToken storedToken = refreshTokenService.validateRefreshToken(request.refreshToken());

        User user = userRepository.findById(storedToken.getUserId())
                .orElseThrow(() -> new TokenRefreshException("Usuário não encontrado."));

        // Resolve de novo as permissões: garante que, se mudaram desde o login
        // original, o novo access token já reflete o estado atual.
        List<String> authorities = userPermissionRepository
                .findEffectivePermissionCodesByUserId(user.getUserId());

        String newAccessToken = jwtService.tokenGenerate(
                user.getEmail(),
                user.getUserId(),
                user.getUnityId(),
                authorities
        );

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
