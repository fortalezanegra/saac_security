package br.com.adbrascosmopolis.saac_security.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "GroupSubgroup")
public class GroupSubgroup {

    @EmbeddedId
    private GroupSubgroupId id;

    @Column(name = "createdAt", updatable = false, insertable = false)
    private LocalDateTime createdAt;

    public GroupSubgroup() {}

    public GroupSubgroup(Integer parentGroupId, Integer childGroupId) {
        this.id = new GroupSubgroupId(parentGroupId, childGroupId);
    }

    public GroupSubgroupId getId() { return id; }

    public Integer getParentGroupId() { return id.getParentGroupId(); }
    public Integer getChildGroupId() { return id.getChildGroupId(); }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
