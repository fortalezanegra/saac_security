package br.com.adbrascosmopolis.saac_security.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "EntityGroup")
public class EntityGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer groupId;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "createdAt", updatable = false, insertable = false)
    private LocalDateTime createdAt;

    @Column(name = "updatedAt", insertable = false)
    private LocalDateTime updatedAt;

    public EntityGroup() {}

    public EntityGroup(String name) {
        this.name = name;
    }

    public Integer getGroupId() { return groupId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
