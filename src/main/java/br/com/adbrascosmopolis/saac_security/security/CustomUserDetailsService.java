package br.com.adbrascosmopolis.saac_security.security;

import br.com.adbrascosmopolis.saac_security.model.Usuario;
import br.com.adbrascosmopolis.saac_security.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public CustomUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + email));

        if (!usuario.isAtivo()) {
            throw new UsernameNotFoundException("Usuário inativo: " + email);
        }

        return new UsuarioAuthenticated(
                usuario.getUsuarioId(),
                usuario.getEmail(),
                usuario.getSenha(),
                usuario.getUnidadeId(),
                Collections.singletonList(() -> "ROLE_" + usuario.getTipoEscopo().name())
        );
    }

}
