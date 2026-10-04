package br.com.adbrascosmopolis.saac_security.repository;

import br.com.adbrascosmopolis.saac_security.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    boolean existsByEmailAndDeletedAtIsNull(String email);
}
