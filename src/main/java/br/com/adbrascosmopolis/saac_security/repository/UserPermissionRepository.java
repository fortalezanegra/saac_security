package br.com.adbrascosmopolis.saac_security.repository;

import br.com.adbrascosmopolis.saac_security.model.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserPermissionRepository extends JpaRepository<Permission, Integer> {

    /**
     * Retorna todas as Permissions efetivas de um usuário.
     *
     * Herança: usuário no Grupo A (pai) herda as permissions diretas de A
     * + permissions de TODOS os descendentes (subgrupos, sub-subgrupos, etc.)
     */
    @Query(value = """
        WITH RECURSIVE GroupHierarchy AS (
            SELECT ug.groupId AS groupId
            FROM UserGroup ug
            WHERE ug.userId = :userId

            UNION

            SELECT gs.childGroupId AS groupId
            FROM GroupSubgroup gs
            INNER JOIN GroupHierarchy gh ON gs.parentGroupId = gh.groupId
        )
        SELECT DISTINCT p.*
        FROM Permission p
        INNER JOIN GroupPermission gp ON gp.permissionId = p.permissionId
        INNER JOIN GroupHierarchy gh ON gh.groupId = gp.groupId
        """, nativeQuery = true)
    List<Permission> findEffectivePermissionsByUserId(@Param("userId") Long userId);

    /**
     * Versão enxuta: retorna apenas os códigos das permissions
     * (ex: "USER_READ"), ideal para montar GrantedAuthority no Spring Security.
     */
    @Query(value = """
        WITH RECURSIVE GroupHierarchy AS (
            SELECT ug.groupId AS groupId
            FROM UserGroup ug
            WHERE ug.userId = :userId

            UNION

            SELECT gs.childGroupId AS groupId
            FROM GroupSubgroup gs
            INNER JOIN GroupHierarchy gh ON gs.parentGroupId = gh.groupId
        )
        SELECT DISTINCT p.code
        FROM Permission p
        INNER JOIN GroupPermission gp ON gp.permissionId = p.permissionId
        INNER JOIN GroupHierarchy gh ON gh.groupId = gp.groupId
        """, nativeQuery = true)
    List<String> findEffectivePermissionCodesByUserId(@Param("userId") Long userId);
}
