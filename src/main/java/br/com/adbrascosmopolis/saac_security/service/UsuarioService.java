package br.com.adbrascosmopolis.saac_security.service;

import br.com.adbrascosmopolis.saac_security.dto.usuario.UsuarioRequestDTO;
import br.com.adbrascosmopolis.saac_security.enumeration.TipoEscopo;
import br.com.adbrascosmopolis.saac_security.models.Usuario;
import br.com.adbrascosmopolis.saac_security.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder){
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Usuario create(UsuarioRequestDTO dto){

        if (usuarioRepository.existsByEmailAndDeletedAtIsNull(dto.getEmail())) {
            throw new IllegalArgumentException("E-mail já cadastrado no sistema.");
        }

        Usuario usuario = new Usuario();

        usuario.setNome(dto.getNome());
        usuario.setEmail(dto.getEmail());
        usuario.setSenha(passwordEncoder.encode(dto.getSenha()));
        usuario.setUnidadeId(dto.getUnidadeId());
        usuario.setTipoEscopo(TipoEscopo.valueOf(dto.getTipoEscopo().toUpperCase()));
        usuario.setAtivo(true);

        return usuarioRepository.save(usuario);
    }

}
