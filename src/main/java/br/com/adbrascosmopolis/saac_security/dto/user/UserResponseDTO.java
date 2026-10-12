package br.com.adbrascosmopolis.saac_security.dto.user;

import java.time.LocalDateTime;

public class UserResponseDTO {

    private Long userId;
    private String name;
    private String email;
    private boolean active;
    private Long unityId;
    private LocalDateTime createdAt;

    public UserResponseDTO(Long userId, String name, String email, Boolean active,
                           Long unityId, LocalDateTime createdAt) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.active = active;
        this.unityId = unityId;
        this.createdAt = createdAt;
    }

    public Long getUserId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public boolean isActive() { return active; }
    public Long getUnityId() { return unityId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
