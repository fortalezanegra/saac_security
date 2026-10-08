package br.com.adbrascosmopolis.saac_security.repository;

import br.com.adbrascosmopolis.saac_security.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmailAndDeletedAtIsNull(String email);

    Optional<User> findByEmailAndDeletedAtIsNull(String email);

    List<User> findAllByDeletedAtIsNull();

    // --- NOVO: paginação respeitando soft delete (uso GLOBAL) ---
    Page<User> findAllByDeletedAtIsNull(Pageable pageable);

    // --- NOVO: paginação filtrada por unidade (uso LOCAL) ---
    Page<User> findAllByDeletedAtIsNullAndUnityId(Long unityId, Pageable pageable);

    // --- NOVO: busca por id apenas entre os deletados (necessário para restore) ---
    @Query("SELECT u FROM User u WHERE u.userId = :id AND u.deletedAt IS NOT NULL")
    Optional<User> findDeletedById(@Param("id") Long id);
}
