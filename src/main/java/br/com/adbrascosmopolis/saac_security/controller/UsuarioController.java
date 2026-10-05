package br.com.adbrascosmopolis.saac_security.controller;

import br.com.adbrascosmopolis.saac_security.dto.usuario.UsuarioRequestDTO;
import br.com.adbrascosmopolis.saac_security.dto.usuario.UsuarioResponseDTO;
import br.com.adbrascosmopolis.saac_security.dto.usuario.UsuarioUpdateDTO;
import br.com.adbrascosmopolis.saac_security.security.UsuarioAuthenticated;
import br.com.adbrascosmopolis.saac_security.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping
    @PreAuthorize("hasRole('GLOBAL')")
    public ResponseEntity<UsuarioResponseDTO> criar(@Valid @RequestBody UsuarioRequestDTO dto) {
        UsuarioResponseDTO criado = usuarioService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    @GetMapping
    @PreAuthorize("hasRole('GLOBAL')")
    public ResponseEntity<List<UsuarioResponseDTO>> listarTodos() {
        return ResponseEntity.ok(usuarioService.listarTodos());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('GLOBAL') or hasRole('LOCAL')")
    public ResponseEntity<UsuarioResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('GLOBAL')")
    public ResponseEntity<UsuarioResponseDTO> atualizar(@PathVariable Long id,
                                                        @Valid @RequestBody UsuarioUpdateDTO dto) {
        return ResponseEntity.ok(usuarioService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('GLOBAL')")
    public ResponseEntity<Void> deletar(@PathVariable Long id, Authentication authentication) {
        Long modifiedBy = extrairUsuarioIdDoToken(authentication);
        usuarioService.deletar(id, modifiedBy);
        return ResponseEntity.noContent().build();
    }

    private Long extrairUsuarioIdDoToken(Authentication authentication) {
        UsuarioAuthenticated principal = (UsuarioAuthenticated) authentication.getPrincipal();
        return principal.getUsuarioId();
    }

}
