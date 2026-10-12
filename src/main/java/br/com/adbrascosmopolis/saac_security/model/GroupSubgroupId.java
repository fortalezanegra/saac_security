package br.com.adbrascosmopolis.saac_security.model;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class GroupSubgroupId implements Serializable {

    private Integer parentGroupId;
    private Integer childGroupId;

    public GroupSubgroupId() {}

    public GroupSubgroupId(Integer parentGroupId, Integer childGroupId) {
        this.parentGroupId = parentGroupId;
        this.childGroupId = childGroupId;
    }

    public Integer getParentGroupId() { return parentGroupId; }
    public void setParentGroupId(Integer parentGroupId) { this.parentGroupId = parentGroupId; }

    public Integer getChildGroupId() { return childGroupId; }
    public void setChildGroupId(Integer childGroupId) { this.childGroupId = childGroupId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GroupSubgroupId that)) return false;
        return Objects.equals(parentGroupId, that.parentGroupId) && Objects.equals(childGroupId, that.childGroupId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parentGroupId, childGroupId);
    }
}
