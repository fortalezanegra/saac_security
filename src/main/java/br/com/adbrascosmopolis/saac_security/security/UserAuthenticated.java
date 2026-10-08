package br.com.adbrascosmopolis.saac_security.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public class UserAuthenticated implements UserDetails {

    private final Long userId;
    private final String email;
    private final String password;
    private final Long unityId;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserAuthenticated(Long userId, String email, String password, Long unityId,
                             Collection<? extends GrantedAuthority> authorities) {
        this.userId = userId;
        this.email = email;
        this.password = password;
        this.unityId = unityId;
        this.authorities = authorities;
    }

    public Long getUserId() { return userId; }
    public Long getUnityId() { return unityId; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }

    @Override
    public String getPassword() { return password; }

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
