package br.com.adbrascosmopolis.saac_security.controller;

import br.com.adbrascosmopolis.saac_security.dto.user.ChangePasswordDTO;
import br.com.adbrascosmopolis.saac_security.dto.user.UserRequestDTO;
import br.com.adbrascosmopolis.saac_security.dto.user.UserResponseDTO;
import br.com.adbrascosmopolis.saac_security.dto.user.UserUpdateDTO;
import br.com.adbrascosmopolis.saac_security.enumeration.ScopeType;
import br.com.adbrascosmopolis.saac_security.exception.*;
import br.com.adbrascosmopolis.saac_security.security.UserAuthenticated;
import br.com.adbrascosmopolis.saac_security.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;






/**
 * Observação: usamos authentication(...) com um Authentication cujo principal é
 * UserAuthenticated, pois o controller faz cast direto para essa classe.
 * O @PreAuthorize exige que as GrantedAuthorities estejam no Authentication mockado.
 */
@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    private UserResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        responseDTO = new UserResponseDTO(
                1L, "João Silva", "joao@teste.com", true, 10L, ScopeType.LOCAL, LocalDateTime.now());
    }

    private RequestPostProcessor asGlobal(Long userId, Long unityId) {
        UserAuthenticated principal = new UserAuthenticated(
                userId, "global@teste.com", "hash", unityId,
                List.of(new SimpleGrantedAuthority("ROLE_GLOBAL")));
        Authentication auth = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities());
        return authentication(auth);
    }

    private RequestPostProcessor asLocal(Long userId, Long unityId) {
        UserAuthenticated principal = new UserAuthenticated(
                userId, "local@teste.com", "hash", unityId,
                List.of(new SimpleGrantedAuthority("ROLE_LOCAL")));
        Authentication auth = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities());
        return authentication(auth);
    }

    // ---------- findById ----------

    @Test
    void findById_deveRetornar200_quandoGlobal() throws Exception {
        when(userService.searchById(1L)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/users/1").with(asGlobal(99L, 1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("joao@teste.com"));
    }

    @Test
    void findById_deveRetornar403_quandoLocalDeOutraUnidade() throws Exception {
        when(userService.searchById(1L)).thenReturn(responseDTO); // unityId = 10

        mockMvc.perform(get("/api/users/1").with(asLocal(2L, 999L)))
                .andExpect(status().isForbidden());
    }

    @Test
    void findById_deveRetornar200_quandoLocalDaMesmaUnidade() throws Exception {
        when(userService.searchById(1L)).thenReturn(responseDTO); // unityId = 10

        mockMvc.perform(get("/api/users/1").with(asLocal(2L, 10L)))
                .andExpect(status().isOk());
    }

    @Test
    void findById_deveRetornar404_quandoNaoEncontrado() throws Exception {
        when(userService.searchById(99L)).thenThrow(new ResourceNotFoundException("Usuário não encontrado com id: 99"));

        mockMvc.perform(get("/api/users/99").with(asGlobal(1L, 1L)))
                .andExpect(status().isNotFound());
    }

    // ---------- findAll ----------

    @Test
    void findAll_deveListarTodos_quandoGlobal() throws Exception {
        Page<UserResponseDTO> page = new PageImpl<>(List.of(responseDTO));
        when(userService.listAllPaged(any())).thenReturn(page);

        mockMvc.perform(get("/api/users").with(asGlobal(1L, 1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].email").value("joao@teste.com"));
    }

    @Test
    void findAll_deveFiltrarPorUnidade_quandoLocal() throws Exception {
        Page<UserResponseDTO> page = new PageImpl<>(List.of(responseDTO));
        when(userService.listByUnity(eq(10L), any())).thenReturn(page);

        mockMvc.perform(get("/api/users").with(asLocal(2L, 10L)))
                .andExpect(status().isOk());
    }

    // ---------- create ----------

    @Test
    void create_deveRetornar201_quandoGlobal() throws Exception {
        UserRequestDTO dto = new UserRequestDTO();
        dto.setName("João Silva");
        dto.setEmail("joao@teste.com");
        dto.setPassword("senha12345");
        dto.setUnityId(10L);
        dto.setScopeType(ScopeType.LOCAL);

        when(userService.create(any())).thenReturn(responseDTO);

        mockMvc.perform(post("/api/users")
                        .with(asGlobal(1L, 1L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("joao@teste.com"));
    }

    @Test
    void create_deveRetornar403_quandoLocal() throws Exception {
        UserRequestDTO dto = new UserRequestDTO();
        dto.setName("João Silva");
        dto.setEmail("joao@teste.com");
        dto.setPassword("senha12345");
        dto.setUnityId(10L);
        dto.setScopeType(ScopeType.LOCAL);

        mockMvc.perform(post("/api/users")
                        .with(asLocal(1L, 1L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void create_deveRetornar400_quandoEmailInvalido() throws Exception {
        UserRequestDTO dto = new UserRequestDTO();
        dto.setName("João Silva");
        dto.setEmail("email-invalido");
        dto.setPassword("senha12345");
        dto.setUnityId(10L);
        dto.setScopeType(ScopeType.LOCAL);

        mockMvc.perform(post("/api/users")
                        .with(asGlobal(1L, 1L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_deveRetornar409_quandoEmailDuplicado() throws Exception {
        UserRequestDTO dto = new UserRequestDTO();
        dto.setName("João Silva");
        dto.setEmail("joao@teste.com");
        dto.setPassword("senha12345");
        dto.setUnityId(10L);
        dto.setScopeType(ScopeType.LOCAL);

        when(userService.create(any())).thenThrow(new EmailJaCadastradoException("Já existe um usuário ativo com este e-mail"));

        mockMvc.perform(post("/api/users")
                        .with(asGlobal(1L, 1L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    // ---------- update ----------

    @Test
    void update_deveRetornar200_quandoGlobal() throws Exception {
        UserUpdateDTO dto = new UserUpdateDTO();
        dto.setName("João Atualizado");
        dto.setUnityId(10L);
        dto.setScopeType(ScopeType.LOCAL);

        when(userService.update(eq(1L), any())).thenReturn(responseDTO);

        mockMvc.perform(put("/api/users/1")
                        .with(asGlobal(1L, 1L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void update_deveRetornar403_quandoLocal() throws Exception {
        UserUpdateDTO dto = new UserUpdateDTO();
        dto.setName("João Atualizado");
        dto.setUnityId(10L);
        dto.setScopeType(ScopeType.LOCAL);

        mockMvc.perform(put("/api/users/1")
                        .with(asLocal(1L, 1L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    // ---------- delete ----------

    @Test
    void delete_deveRetornar204_quandoGlobalDeletaOutroUsuario() throws Exception {
        mockMvc.perform(delete("/api/users/2").with(asGlobal(1L, 1L)))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_deveRetornar409_quandoAutoExclusao() throws Exception {
        org.mockito.Mockito.doThrow(new SelfActionNotAllowedException("Não é permitido excluir o próprio usuário."))
                .when(userService).validateNotSelfDeletion(1L, 1L);

        mockMvc.perform(delete("/api/users/1").with(asGlobal(1L, 1L)))
                .andExpect(status().isConflict());
    }

    @Test
    void delete_deveRetornar403_quandoLocal() throws Exception {
        mockMvc.perform(delete("/api/users/2").with(asLocal(1L, 1L)))
                .andExpect(status().isForbidden());
    }

    // ---------- restore ----------

    @Test
    void restore_deveRetornar200_quandoGlobal() throws Exception {
        when(userService.restore(eq(1L), eq(99L))).thenReturn(responseDTO);

        mockMvc.perform(patch("/api/users/1/restore").with(asGlobal(99L, 1L)))
                .andExpect(status().isOk());
    }

    @Test
    void restore_deveRetornar404_quandoNaoEncontrado() throws Exception {
        when(userService.restore(eq(99L), anyLong()))
                .thenThrow(new ResourceNotFoundException("Usuário deletado não encontrado com id: 99"));

        mockMvc.perform(patch("/api/users/99/restore").with(asGlobal(1L, 1L)))
                .andExpect(status().isNotFound());
    }

    // ---------- changePassword ----------

    @Test
    void changePassword_deveRetornar204_quandoProprioUsuario() throws Exception {
        ChangePasswordDTO dto = new ChangePasswordDTO("senhaAtual", "novaSenha123");

        mockMvc.perform(patch("/api/users/1/password")
                        .with(asLocal(1L, 10L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNoContent());
    }

    @Test
    void changePassword_deveRetornar403_quandoSenhaIncorretaOuSemPermissao() throws Exception {
        ChangePasswordDTO dto = new ChangePasswordDTO("senhaErrada", "novaSenha123");

        org.mockito.Mockito.doThrow(new InvalidCredentialsException("Senha atual incorreta."))
                .when(userService).changePassword(eq(1L), any(), eq(1L), eq(false));

        mockMvc.perform(patch("/api/users/1/password")
                        .with(asLocal(1L, 10L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void changePassword_deveRetornar400_quandoNovaSenhaMuitoCurta() throws Exception {
        ChangePasswordDTO dto = new ChangePasswordDTO("senhaAtual", "123");

        mockMvc.perform(patch("/api/users/1/password")
                        .with(asLocal(1L, 10L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }
}
