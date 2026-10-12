package br.com.adbrascosmopolis.saac_security.repository;

import br.com.adbrascosmopolis.saac_security.model.EntityGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EntityGroupRepository extends JpaRepository<EntityGroup, Integer> {
    Optional<EntityGroup> findByName(String name);
}
