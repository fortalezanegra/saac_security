package br.com.adbrascosmopolis.saac_security.service;

import br.com.adbrascosmopolis.saac_security.dto.user.ChangePasswordDTO;
import br.com.adbrascosmopolis.saac_security.dto.user.UserRequestDTO;
import br.com.adbrascosmopolis.saac_security.dto.user.UserResponseDTO;
import br.com.adbrascosmopolis.saac_security.dto.user.UserUpdateDTO;
import br.com.adbrascosmopolis.saac_security.enumeration.ScopeType;
import br.com.adbrascosmopolis.saac_security.exception.EmailJaCadastradoException;
import br.com.adbrascosmopolis.saac_security.exception.InvalidCredentialsException;
import br.com.adbrascosmopolis.saac_security.exception.ResourceNotFoundException;
import br.com.adbrascosmopolis.saac_security.exception.SelfActionNotAllowedException;
import br.com.adbrascosmopolis.saac_security.model.User;
import br.com.adbrascosmopolis.saac_security.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUserId(1L);
        user.setName("João Silva");
        user.setEmail("joao@teste.com");
        user.setPassword("hashedPassword");
        user.setActive(true);
        user.setUnityId(10L);
        user.setScopeType(ScopeType.LOCAL);
        user.setCreatedAt(LocalDateTime.now());
    }

    // ---------- create ----------

    @Test
    void create_deveCriarUsuario_quandoEmailNaoExiste() {
        UserRequestDTO dto = new UserRequestDTO();
        dto.setName("João Silva");
        dto.setEmail("joao@teste.com");
        dto.setPassword("senha12345");
        dto.setUnityId(10L);
        dto.setScopeType(ScopeType.LOCAL);

        when(userRepository.findByEmailAndDeletedAtIsNull(dto.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(dto.getPassword())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponseDTO result = userService.create(dto);

        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("joao@teste.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void create_deveLancarExcecao_quandoEmailJaExiste() {
        UserRequestDTO dto = new UserRequestDTO();
        dto.setEmail("joao@teste.com");

        when(userRepository.findByEmailAndDeletedAtIsNull(dto.getEmail())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.create(dto))
                .isInstanceOf(EmailJaCadastradoException.class)
                .hasMessageContaining("Já existe um usuário ativo");

        verify(userRepository, never()).save(any());
    }

    // ---------- listAll / listAllPaged / listByUnity ----------

    @Test
    void listAll_deveRetornarApenasAtivos() {
        when(userRepository.findAllByDeletedAtIsNull()).thenReturn(List.of(user));

        List<UserResponseDTO> result = userService.listAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmail()).isEqualTo("joao@teste.com");
    }

    @Test
    void listAllPaged_deveRetornarPageDeUsuarios() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> page = new PageImpl<>(List.of(user));

        when(userRepository.findAllByDeletedAtIsNull(pageable)).thenReturn(page);

        Page<UserResponseDTO> result = userService.listAllPaged(pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getUnityId()).isEqualTo(10L);
    }

    @Test
    void listByUnity_deveFiltrarPorUnidade() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> page = new PageImpl<>(List.of(user));

        when(userRepository.findAllByDeletedAtIsNullAndUnityId(10L, pageable)).thenReturn(page);

        Page<UserResponseDTO> result = userService.listByUnity(10L, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(userRepository).findAllByDeletedAtIsNullAndUnityId(10L, pageable);
    }

    // ---------- searchById ----------

    @Test
    void searchById_deveRetornarUsuario_quandoExisteEAtivo() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponseDTO result = userService.searchById(1L);

        assertThat(result.getUserId()).isEqualTo(1L);
    }

    @Test
    void searchById_deveLancarResourceNotFound_quandoNaoExiste() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.searchById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("não encontrado");
    }

    @Test
    void searchById_deveLancarResourceNotFound_quandoDeletado() {
        user.setDeletedAt(LocalDateTime.now());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.searchById(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ---------- update ----------

    @Test
    void update_deveAtualizarCamposPermitidos() {
        UserUpdateDTO dto = new UserUpdateDTO();
        dto.setName("João Atualizado");
        dto.setUnityId(20L);
        dto.setScopeType(ScopeType.GLOBAL);
        dto.setActive(false);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponseDTO result = userService.update(1L, dto);

        assertThat(result.getName()).isEqualTo("João Atualizado");
        assertThat(result.getUnityId()).isEqualTo(20L);
        assertThat(result.getScopeType()).isEqualTo(ScopeType.GLOBAL);
        assertThat(result.isActive()).isFalse();
    }

    @Test
    void update_deveLancarResourceNotFound_quandoUsuarioNaoExiste() {
        UserUpdateDTO dto = new UserUpdateDTO();
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.update(99L, dto))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ---------- delete (soft delete) ----------

    @Test
    void delete_deveMarcarDeletedAtEModifiedBy() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.delete(1L, 99L);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        User saved = captor.getValue();
        assertThat(saved.getDeletedAt()).isNotNull();
        assertThat(saved.getModifiedBy()).isEqualTo(99L);
    }

    @Test
    void delete_deveLancarResourceNotFound_quandoUsuarioNaoExiste() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.delete(99L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ---------- restore ----------

    @Test
    void restore_deveLimparDeletedAt_quandoUsuarioDeletadoExiste() {
        user.setDeletedAt(LocalDateTime.now());
        when(userRepository.findDeletedById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponseDTO result = userService.restore(1L, 50L);

        assertThat(result.getUserId()).isEqualTo(1L);
        verify(userRepository).save(argThat(u -> u.getDeletedAt() == null && u.getModifiedBy().equals(50L)));
    }

    @Test
    void restore_deveLancarResourceNotFound_quandoNaoHaUsuarioDeletadoComEsseId() {
        when(userRepository.findDeletedById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.restore(99L, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("deletado não encontrado");
    }

    // ---------- changePassword ----------

    @Test
    void changePassword_deveAlterarSenha_quandoProprioUsuarioComSenhaCorreta() {
        ChangePasswordDTO dto = new ChangePasswordDTO("senhaAtual", "novaSenha123");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("senhaAtual", "hashedPassword")).thenReturn(true);
        when(passwordEncoder.encode("novaSenha123")).thenReturn("novoHash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.changePassword(1L, dto, 1L, false);

        verify(userRepository).save(argThat(u -> u.getPassword().equals("novoHash") && u.getModifiedBy().equals(1L)));
    }

    @Test
    void changePassword_deveLancarInvalidCredentials_quandoSenhaAtualIncorreta() {
        ChangePasswordDTO dto = new ChangePasswordDTO("senhaErrada", "novaSenha123");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("senhaErrada", "hashedPassword")).thenReturn(false);

        assertThatThrownBy(() -> userService.changePassword(1L, dto, 1L, false))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("Senha atual incorreta");

        verify(userRepository, never()).save(any());
    }

    @Test
    void changePassword_deveLancarInvalidCredentials_quandoTerceiroSemSerGlobal() {
        ChangePasswordDTO dto = new ChangePasswordDTO("qualquer", "novaSenha123");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user)); // userId=1

        assertThatThrownBy(() -> userService.changePassword(1L, dto, 2L, false))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("Sem permissão");

        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void changePassword_deveFuncionar_quandoRequesterIsGlobal_semValidarSenhaAtual() {
        ChangePasswordDTO dto = new ChangePasswordDTO("naoImporta", "novaSenha123");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user)); // userId=1, requester=2 (GLOBAL)
        when(passwordEncoder.encode("novaSenha123")).thenReturn("novoHash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.changePassword(1L, dto, 2L, true);

        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(userRepository).save(argThat(u -> u.getPassword().equals("novoHash") && u.getModifiedBy().equals(2L)));
    }

    // ---------- validateNotSelfDeletion ----------

    @Test
    void validateNotSelfDeletion_deveLancarExcecao_quandoTargetIgualRequester() {
        assertThatThrownBy(() -> userService.validateNotSelfDeletion(1L, 1L))
                .isInstanceOf(SelfActionNotAllowedException.class)
                .hasMessageContaining("próprio usuário");
    }

    @Test
    void validateNotSelfDeletion_naoDeveLancar_quandoIdsDiferentes() {
        userService.validateNotSelfDeletion(1L, 2L); // não deve lançar
    }
}
