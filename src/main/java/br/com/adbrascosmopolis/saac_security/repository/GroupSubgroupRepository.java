package br.com.adbrascosmopolis.saac_security.repository;

import br.com.adbrascosmopolis.saac_security.model.GroupSubgroup;
import br.com.adbrascosmopolis.saac_security.model.GroupSubgroupId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupSubgroupRepository extends JpaRepository<GroupSubgroup, GroupSubgroupId> {
    List<GroupSubgroup> findByIdParentGroupId(Integer parentGroupId);
    List<GroupSubgroup> findByIdChildGroupId(Integer childGroupId);
}
