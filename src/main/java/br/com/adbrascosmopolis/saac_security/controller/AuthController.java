package br.com.adbrascosmopolis.saac_security.controller;

import br.com.adbrascosmopolis.saac_security.dto.login.LoginRequestDTO;
import br.com.adbrascosmopolis.saac_security.dto.login.LoginResponseDTO;
import br.com.adbrascosmopolis.saac_security.dto.refresh_token.RefreshTokenRequestDTO;
import br.com.adbrascosmopolis.saac_security.dto.refresh_token.TokenResponseDTO;
import br.com.adbrascosmopolis.saac_security.exception.TokenRefreshException;
import br.com.adbrascosmopolis.saac_security.model.RefreshToken;
import br.com.adbrascosmopolis.saac_security.model.Usuario;
import br.com.adbrascosmopolis.saac_security.repository.UsuarioRepository;
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
    private final UsuarioRepository usuarioRepository;
    private final RefreshTokenService refreshTokenService;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtService jwtService,
                          UsuarioRepository usuarioRepository,
                          RefreshTokenService refreshTokenService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
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

        Usuario usuario = usuarioRepository.findByEmailAndDeletedAtIsNull(dto.getEmail())
                .orElseThrow(() -> new BadCredentialsException("E-mail ou senha inválidos"));

        String accessToken = jwtService.tokenGenerate(
                usuario.getEmail(),
                usuario.getUsuarioId(),
                usuario.getTipoEscopo().name(),
                usuario.getUnidadeId()
        );

        // Gera (e rotaciona, se já existir) o refresh token do usuário
        RefreshToken refreshToken = refreshTokenService.criar(usuario.getUsuarioId());

        return ResponseEntity.ok(new LoginResponseDTO(accessToken, refreshToken.getToken(), "Bearer"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponseDTO> refresh(@Valid @RequestBody RefreshTokenRequestDTO request) {
        RefreshToken storedToken = refreshTokenService.validarRefreshToken(request.refreshToken());

        Usuario usuario = usuarioRepository.findById(storedToken.getUserId())
                .orElseThrow(() -> new TokenRefreshException("Usuário não encontrado."));

        String novoAccessToken = jwtService.tokenGenerate(
                usuario.getEmail(),
                usuario.getUsuarioId(),
                usuario.getTipoEscopo().name(),
                usuario.getUnidadeId()
        );

        // Rotação: revoga o token usado e emite um novo
        RefreshToken novoRefreshToken = refreshTokenService.criar(usuario.getUsuarioId());

        return ResponseEntity.ok(new TokenResponseDTO(novoAccessToken, novoRefreshToken.getToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequestDTO request) {
        RefreshToken storedToken = refreshTokenService.validarRefreshToken(request.refreshToken());
        refreshTokenService.revogarTodosDoUsuario(storedToken.getUserId());
        return ResponseEntity.noContent().build();
    }

}
