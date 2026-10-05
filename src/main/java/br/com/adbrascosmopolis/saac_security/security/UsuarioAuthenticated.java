package br.com.adbrascosmopolis.saac_security.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public class UsuarioAuthenticated implements UserDetails {

    private final Long usuarioId;
    private final String email;
    private final String senha;
    private final Long unidadeId;
    private final Collection<? extends GrantedAuthority> authorities;

    public UsuarioAuthenticated(Long usuarioId, String email, String senha, Long unidadeId,
                                Collection<? extends GrantedAuthority> authorities) {
        this.usuarioId = usuarioId;
        this.email = email;
        this.senha = senha;
        this.unidadeId = unidadeId;
        this.authorities = authorities;
    }

    public Long getUsuarioId() { return usuarioId; }
    public Long getUnidadeId() { return unidadeId; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }

    @Override
    public String getPassword() { return senha; }

    @Override
    public String getUsername() { return email; }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}
