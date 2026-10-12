package br.com.adbrascosmopolis.saac_security.repository;

import br.com.adbrascosmopolis.saac_security.model.GroupPermission;
import br.com.adbrascosmopolis.saac_security.model.GroupPermissionId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupPermissionRepository extends JpaRepository<GroupPermission, GroupPermissionId> {
    List<GroupPermission> findByIdGroupId(Integer groupId);
    List<GroupPermission> findByIdPermissionId(Integer permissionId);
}
