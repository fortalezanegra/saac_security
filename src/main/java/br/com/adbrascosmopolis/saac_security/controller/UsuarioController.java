package br.com.adbrascosmopolis.saac_security.controller;

import br.com.adbrascosmopolis.saac_security.dto.usuario.UsuarioRequestDTO;
import br.com.adbrascosmopolis.saac_security.dto.usuario.UsuarioResponseDTO;
import br.com.adbrascosmopolis.saac_security.models.Usuario;
import br.com.adbrascosmopolis.saac_security.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> create(@Valid @RequestBody UsuarioRequestDTO dto) {
        Usuario usuario = usuarioService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new UsuarioResponseDTO(usuario));
    }
}
