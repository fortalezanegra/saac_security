package br.com.adbrascosmopolis.saac_security.controller;

import br.com.adbrascosmopolis.saac_security.dto.user.ChangePasswordDTO;
import br.com.adbrascosmopolis.saac_security.dto.user.UserRequestDTO;
import br.com.adbrascosmopolis.saac_security.dto.user.UserResponseDTO;
import br.com.adbrascosmopolis.saac_security.dto.user.UserUpdateDTO;
import br.com.adbrascosmopolis.saac_security.exception.AccessDeniedCustomException;
import br.com.adbrascosmopolis.saac_security.security.UserAuthenticated;
import br.com.adbrascosmopolis.saac_security.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('GLOBAL') or hasRole('LOCAL')")
    public ResponseEntity<UserResponseDTO> findById(@PathVariable Long id, Authentication authentication) {
        UserAuthenticated principal = extractPrincipal(authentication);
        UserResponseDTO user = userService.searchById(id);

        if (isLocal(principal) && !principal.getUnityId().equals(user.getUnityId())) {
            throw new AccessDeniedCustomException("Sem permissão para acessar usuário de outra unidade.");
        }

        return ResponseEntity.ok(user);
    }

    // --- ALTERADO: agora com paginação e filtro automático por unidade quando LOCAL ---
    @GetMapping
    @PreAuthorize("hasRole('GLOBAL') or hasRole('LOCAL')")
    public ResponseEntity<Page<UserResponseDTO>> findAll(Authentication authentication, Pageable pageable) {
        UserAuthenticated principal = extractPrincipal(authentication);

        Page<UserResponseDTO> result = isLocal(principal)
                ? userService.listByUnity(principal.getUnityId(), pageable)
                : userService.listAllPaged(pageable);

        return ResponseEntity.ok(result);
    }

    @PostMapping
    @PreAuthorize("hasRole('GLOBAL')")
    public ResponseEntity<UserResponseDTO> create(@Valid @RequestBody UserRequestDTO dto) {
        UserResponseDTO criado = userService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('GLOBAL')")
    public ResponseEntity<UserResponseDTO> update(@PathVariable Long id,
                                                  @Valid @RequestBody UserUpdateDTO dto) {
        return ResponseEntity.ok(userService.update(id, dto));
    }

    // --- ALTERADO: valida auto-exclusão antes de deletar ---
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('GLOBAL')")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
        Long modifiedBy = extractPrincipal(authentication).getUserId();
        userService.validateNotSelfDeletion(id, modifiedBy);
        userService.delete(id, modifiedBy);
        return ResponseEntity.noContent().build();
    }

    // --- NOVO: restaurar usuário soft-deletado ---
    @PatchMapping("/{id}/restore")
    @PreAuthorize("hasRole('GLOBAL')")
    public ResponseEntity<UserResponseDTO> restore(@PathVariable Long id, Authentication authentication) {
        Long modifiedBy = extractPrincipal(authentication).getUserId();
        return ResponseEntity.ok(userService.restore(id, modifiedBy));
    }

    // --- NOVO: troca de senha (próprio usuário ou GLOBAL) ---
    @PatchMapping("/{id}/password")
    @PreAuthorize("hasRole('GLOBAL') or hasRole('LOCAL')")
    public ResponseEntity<Void> changePassword(@PathVariable Long id,
                                               @Valid @RequestBody ChangePasswordDTO dto,
                                               Authentication authentication) {
        UserAuthenticated principal = extractPrincipal(authentication);
        boolean requesterIsGlobal = principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_GLOBAL"));

        userService.changePassword(id, dto, principal.getUserId(), requesterIsGlobal);
        return ResponseEntity.noContent().build();
    }

    private UserAuthenticated extractPrincipal(Authentication authentication) {
        return (UserAuthenticated) authentication.getPrincipal();
    }

    private boolean isLocal(UserAuthenticated principal) {
        return principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_LOCAL"));
    }
}
