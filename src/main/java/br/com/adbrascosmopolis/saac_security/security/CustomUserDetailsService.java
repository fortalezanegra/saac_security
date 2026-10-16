package br.com.adbrascosmopolis.saac_security.security;

import br.com.adbrascosmopolis.saac_security.model.User;
import br.com.adbrascosmopolis.saac_security.repository.UserPermissionRepository;
import br.com.adbrascosmopolis.saac_security.repository.UserRepository;
import br.com.adbrascosmopolis.saac_security.repository.projection.EffectivePermissionProjection;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserPermissionRepository userPermissionRepository;

    public CustomUserDetailsService(UserRepository userRepository,
                                    UserPermissionRepository userPermissionRepository) {
        this.userRepository = userRepository;
        this.userPermissionRepository = userPermissionRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + email));

        if (!user.isActive()) {
            throw new UsernameNotFoundException("Usuário inativo: " + email);
        }

        List<EffectivePermissionProjection> permissions =
                userPermissionRepository.findEffectivePermissions(user.getUserId());

        List<SimpleGrantedAuthority> authorities = permissions.stream()
                .map(p -> new SimpleGrantedAuthority(p.getCode() + "_" + p.getScope()))
                .collect(Collectors.toList());

        return new UserAuthenticated(
                user.getUserId(),
                user.getEmail(),
                user.getPassword(),
                user.getUnityId(),
                authorities
        );
    }
}
