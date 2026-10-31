package br.com.adbrascosmopolis.saac_security.repository;

import br.com.adbrascosmopolis.saac_security.model.User;
import br.com.adbrascosmopolis.saac_security.repository.projection.EffectivePermissionProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserPermissionRepository extends JpaRepository<User, Long> {

    @Query(value = """
        WITH RECURSIVE GroupHierarchy AS (
            -- Grupos diretamente vinculados ao usuário
            SELECT
                ug.groupId AS group_id
            FROM UserGroup ug
            WHERE ug.userId = :userId

            UNION ALL

            -- Grupos filhos (subgrupos) recursivamente
            SELECT
                gs.childGroupId AS group_id
            FROM GroupSubgroup gs
            INNER JOIN GroupHierarchy gh ON gs.parentGroupId = gh.group_id
        ),
        RawPermissions AS (
            SELECT DISTINCT
                p.code AS code,
                eg.scopeType AS scope
            FROM GroupHierarchy gh
            INNER JOIN EntityGroup eg ON eg.groupId = gh.group_id
            INNER JOIN GroupPermission gp ON gp.groupId = gh.group_id
            INNER JOIN Permission p ON p.permissionId = gp.permissionId
        )
        SELECT
            code,
            CASE
                WHEN MAX(CASE WHEN scope = 'GLOBAL' THEN 1 ELSE 0 END) = 1
                THEN 'GLOBAL'
                ELSE 'LOCAL'
            END AS scope
        FROM RawPermissions
        GROUP BY code
        """, nativeQuery = true)
    List<EffectivePermissionProjection> findEffectivePermissions(@Param("userId") Long userId);
}
