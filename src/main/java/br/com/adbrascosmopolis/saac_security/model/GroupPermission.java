package br.com.adbrascosmopolis.saac_security.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "GroupPermission")
public class GroupPermission {

    @EmbeddedId
    private GroupPermissionId id;

    @Column(name = "createdAt", updatable = false, insertable = false)
    private LocalDateTime createdAt;

    public GroupPermission() {}

    public GroupPermission(Integer groupId, Integer permissionId) {
        this.id = new GroupPermissionId(groupId, permissionId);
    }

    public GroupPermissionId getId() { return id; }
    public void setId(GroupPermissionId id) { this.id = id; }

    public Integer getGroupId() { return id.getGroupId(); }
    public Integer getPermissionId() { return id.getPermissionId(); }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
