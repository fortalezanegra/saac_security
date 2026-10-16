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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    public static final String PERMISSION_USER_READ_GLOBAL = "PERMISSION_USER_READ_GLOBAL";
    public static final String PERMISSION_USER_READ_LOCAL = "PERMISSION_USER_READ_LOCAL";
    public static final String PERMISSION_USER_CREATE_GLOBAL = "PERMISSION_USER_CREATE_GLOBAL";
    public static final String PERMISSION_USER_UPDATE_GLOBAL = "PERMISSION_USER_UPDATE_GLOBAL";
    public static final String PERMISSION_USER_DELETE_GLOBAL = "PERMISSION_USER_DELETE_GLOBAL";
    public static final String PERMISSION_USER_DELETE_LOCAL = "PERMISSION_USER_DELETE_LOCAL";
    public static final String PERMISSION_USER_RESTORE_GLOBAL = "PERMISSION_USER_RESTORE_GLOBAL";
    public static final String PERMISSION_USER_PASSWORD_GLOBAL = "PERMISSION_USER_PASSWORD_GLOBAL";
    public static final String PERMISSION_USER_PASSWORD_LOCAL = "PERMISSION_USER_PASSWORD_LOCAL";

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority(T(br.com.adbrascosmopolis.saac_security.controller.UserController).PERMISSION_USER_READ_GLOBAL) " +
            "or hasAuthority(T(br.com.adbrascosmopolis.saac_security.controller.UserController).PERMISSION_USER_READ_LOCAL)")
    public ResponseEntity<UserResponseDTO> findById(@PathVariable Long id, Authentication authentication) {
        UserAuthenticated principal = extractPrincipal(authentication);
        UserResponseDTO user = userService.searchById(id);

        if (hasOnlyLocalAuthority(principal, PERMISSION_USER_READ_LOCAL, PERMISSION_USER_READ_GLOBAL)) {
            validateSameUnity(principal, user.getUnityId());
        }

        return ResponseEntity.ok(user);
    }

    @GetMapping
    @PreAuthorize("hasAuthority(T(br.com.adbrascosmopolis.saac_security.controller.UserController).PERMISSION_USER_READ_GLOBAL) " +
            "or hasAuthority(T(br.com.adbrascosmopolis.saac_security.controller.UserController).PERMISSION_USER_READ_LOCAL)")
    public ResponseEntity<Page<UserResponseDTO>> findAll(Authentication authentication, Pageable pageable) {
        UserAuthenticated principal = extractPrincipal(authentication);

        Page<UserResponseDTO> result = hasAuthority(principal, PERMISSION_USER_READ_GLOBAL)
                ? userService.listAllPaged(pageable)
                : userService.listByUnity(principal.getUnityId(), pageable);

        return ResponseEntity.ok(result);
    }

    @PostMapping
    @PreAuthorize("hasAuthority(T(br.com.adbrascosmopolis.saac_security.controller.UserController).PERMISSION_USER_CREATE_GLOBAL)")
    public ResponseEntity<UserResponseDTO> create(@Valid @RequestBody UserRequestDTO dto) {
        UserResponseDTO criado = userService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority(T(br.com.adbrascosmopolis.saac_security.controller.UserController).PERMISSION_USER_UPDATE_GLOBAL)")
    public ResponseEntity<UserResponseDTO> update(@PathVariable Long id,
                                                  @Valid @RequestBody UserUpdateDTO dto) {
        return ResponseEntity.ok(userService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority(T(br.com.adbrascosmopolis.saac_security.controller.UserController).PERMISSION_USER_DELETE_GLOBAL) " +
            "or hasAuthority(T(br.com.adbrascosmopolis.saac_security.controller.UserController).PERMISSION_USER_DELETE_LOCAL)")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
        UserAuthenticated principal = extractPrincipal(authentication);

        if (hasOnlyLocalAuthority(principal, PERMISSION_USER_DELETE_LOCAL, PERMISSION_USER_DELETE_GLOBAL)) {
            UserResponseDTO user = userService.searchById(id);
            validateSameUnity(principal, user.getUnityId());
        }

        Long modifiedBy = principal.getUserId();
        userService.validateNotSelfDeletion(id, modifiedBy);
        userService.delete(id, modifiedBy);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/restore")
    @PreAuthorize("hasAuthority(T(br.com.adbrascosmopolis.saac_security.controller.UserController).PERMISSION_USER_RESTORE_GLOBAL)")
    public ResponseEntity<UserResponseDTO> restore(@PathVariable Long id, Authentication authentication) {
        Long modifiedBy = extractPrincipal(authentication).getUserId();
        return ResponseEntity.ok(userService.restore(id, modifiedBy));
    }

    @PatchMapping("/{id}/password")
    @PreAuthorize("hasAuthority(T(br.com.adbrascosmopolis.saac_security.controller.UserController).PERMISSION_USER_PASSWORD_GLOBAL) " +
            "or hasAuthority(T(br.com.adbrascosmopolis.saac_security.controller.UserController).PERMISSION_USER_PASSWORD_LOCAL)")
    public ResponseEntity<Void> changePassword(@PathVariable Long id,
                                               @Valid @RequestBody ChangePasswordDTO dto,
                                               Authentication authentication) {
        UserAuthenticated principal = extractPrincipal(authentication);
        boolean requesterIsGlobal = hasAuthority(principal, PERMISSION_USER_PASSWORD_GLOBAL);

        userService.changePassword(id, dto, principal.getUserId(), requesterIsGlobal);
        return ResponseEntity.noContent().build();
    }

    private UserAuthenticated extractPrincipal(Authentication authentication) {
        return (UserAuthenticated) authentication.getPrincipal();
    }

    private boolean hasAuthority(UserAuthenticated principal, String authority) {
        return principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(authority));
    }

    private boolean hasOnlyLocalAuthority(UserAuthenticated principal, String localAuthority, String globalAuthority) {
        return hasAuthority(principal, localAuthority) && !hasAuthority(principal, globalAuthority);
    }

    private void validateSameUnity(UserAuthenticated principal, Long unityId) {
        if (!principal.getUnityId().equals(unityId)) {
            throw new AccessDeniedCustomException("Sem permissão para acessar usuário de outra unidade.");
        }
    }
}
