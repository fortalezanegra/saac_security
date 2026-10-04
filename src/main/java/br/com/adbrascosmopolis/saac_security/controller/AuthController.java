package br.com.adbrascosmopolis.saac_security.controller;

import br.com.adbrascosmopolis.saac_security.dto.login.LoginRequestDTO;
import br.com.adbrascosmopolis.saac_security.dto.login.LoginResponseDTO;
import br.com.adbrascosmopolis.saac_security.models.Usuario;
import br.com.adbrascosmopolis.saac_security.repository.UsuarioRepository;
import br.com.adbrascosmopolis.saac_security.security.JwtService;
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

    public AuthController(AuthenticationManager authenticationManager,
                          JwtService jwtService,
                          UsuarioRepository usuarioRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
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

        String token = jwtService.tokenGenerate(
                usuario.getEmail(),
                usuario.getUsuarioId(),
                usuario.getTipoEscopo().name(),
                usuario.getUnidadeId()
        );

        return ResponseEntity.ok(new LoginResponseDTO(token));
    }
}
