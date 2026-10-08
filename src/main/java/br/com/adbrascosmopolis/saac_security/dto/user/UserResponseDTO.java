package br.com.adbrascosmopolis.saac_security.dto.user;

import br.com.adbrascosmopolis.saac_security.enumeration.ScopeType;
import java.time.LocalDateTime;

public class UserResponseDTO {

    private Long userId;
    private String name;
    private String email;
    private boolean active;
    private Long unityId;
    private ScopeType scopeType;
    private LocalDateTime createdAt;

    public UserResponseDTO(Long userId, String name, String email, Boolean active,
                           Long unityId, ScopeType scopeType, LocalDateTime createdAt) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.active = active;
        this.unityId = unityId;
        this.scopeType = scopeType;
        this.createdAt = createdAt;
    }

    // Getters

    public Long getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public boolean isActive() {
        return active;
    }

    public Long getUnityId() {
        return unityId;
    }

    public ScopeType getScopeType() {
        return scopeType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
