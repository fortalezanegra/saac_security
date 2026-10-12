package br.com.adbrascosmopolis.saac_security.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "UserGroup")
public class UserGroup {

    @EmbeddedId
    private UserGroupId id;

    @Column(name = "createdAt", updatable = false, insertable = false)
    private LocalDateTime createdAt;

    public UserGroup() {}

    public UserGroup(Long userId, Integer groupId) {
        this.id = new UserGroupId(userId, groupId);
    }

    public UserGroupId getId() { return id; }

    public Long getUserId() { return id.getUserId(); }
    public Integer getGroupId() { return id.getGroupId(); }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
