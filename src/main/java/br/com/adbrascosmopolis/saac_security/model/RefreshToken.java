package br.com.adbrascosmopolis.saac_security.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "RefreshToken")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 500)
    private String token;

    @Column(name = "userId", nullable = false)
    private Long userId;

    @Column(name = "expiresOn", nullable = false)
    private LocalDateTime expiresOn;

    @Column(nullable = false)
    private boolean revoked = false;

    @Column(name = "createdAt", nullable = false, updatable = false,
            insertable = false) // preenchido pelo DEFAULT CURRENT_TIMESTAMP do banco
    private LocalDateTime createdAt;

    public RefreshToken() {}

    public RefreshToken(String token, Long userId, LocalDateTime expiresOn) {
        this.token = token;
        this.userId = userId;
        this.expiresOn = expiresOn;
    }

    public Long getId() { return id; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public LocalDateTime getExpiresOn() { return expiresOn; }
    public void setExpiresOn(LocalDateTime expiresOn) { this.expiresOn = expiresOn; }

    public boolean isRevoked() { return revoked; }
    public void setRevoked(boolean revoked) { this.revoked = revoked; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresOn);
    }
}
