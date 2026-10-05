package br.com.adbrascosmopolis.saac_security.service;

import br.com.adbrascosmopolis.saac_security.dto.usuario.UsuarioRequestDTO;
import br.com.adbrascosmopolis.saac_security.dto.usuario.UsuarioResponseDTO;
import br.com.adbrascosmopolis.saac_security.dto.usuario.UsuarioUpdateDTO;
import br.com.adbrascosmopolis.saac_security.exception.EmailJaCadastradoException;
import br.com.adbrascosmopolis.saac_security.exception.ResourceNotFoundException;
import br.com.adbrascosmopolis.saac_security.model.Usuario;
import br.com.adbrascosmopolis.saac_security.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UsuarioResponseDTO criar(UsuarioRequestDTO dto) {
        usuarioRepository.findByEmailAndDeletedAtIsNull(dto.getEmail())
                .ifPresent(u -> {
                    throw new EmailJaCadastradoException("Já existe um usuário ativo com este e-mail");
                });

        Usuario usuario = new Usuario();
        usuario.setNome(dto.getNome());
        usuario.setEmail(dto.getEmail());
        usuario.setSenha(passwordEncoder.encode(dto.getSenha()));
        usuario.setUnidadeId(dto.getUnidadeId());
        usuario.setTipoEscopo(dto.getTipoEscopo());
        usuario.setAtivo(true);
        usuario.setCreatedAt(LocalDateTime.now());

        Usuario salvo = usuarioRepository.save(usuario);
        return toResponseDTO(salvo);
    }

    public List<UsuarioResponseDTO> listarTodos() {
        return usuarioRepository.findAllByDeletedAtIsNull()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public UsuarioResponseDTO buscarPorId(Long id) {
        Usuario usuario = buscarAtivoOuFalhar(id);
        return toResponseDTO(usuario);
    }

    @Transactional
    public UsuarioResponseDTO atualizar(Long id, UsuarioUpdateDTO dto) {
        Usuario usuario = buscarAtivoOuFalhar(id);

        usuario.setNome(dto.getNome());
        usuario.setUnidadeId(dto.getUnidadeId());
        usuario.setTipoEscopo(dto.getTipoEscopo());

        if (dto.getAtivo() != null) {
            usuario.setAtivo(dto.getAtivo());
        }

        usuario.setUpdatedAt(LocalDateTime.now());

        Usuario atualizado = usuarioRepository.save(usuario);
        return toResponseDTO(atualizado);
    }

    @Transactional
    public void deletar(Long id, Long modifiedBy) {
        Usuario usuario = buscarAtivoOuFalhar(id);
        usuario.setDeletedAt(LocalDateTime.now());
        usuario.setModifiedBy(modifiedBy);
        usuarioRepository.save(usuario);
    }

    private Usuario buscarAtivoOuFalhar(Long id) {
        return usuarioRepository.findById(id)
                .filter(u -> u.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com id: " + id));
    }

    private UsuarioResponseDTO toResponseDTO(Usuario usuario) {
        return new UsuarioResponseDTO(
                usuario.getUsuarioId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.isAtivo(),
                usuario.getUnidadeId(),
                usuario.getTipoEscopo(),
                usuario.getCreatedAt()
        );
    }
}
