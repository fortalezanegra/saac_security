package br.com.adbrascosmopolis.saac_security.service;

import br.com.adbrascosmopolis.saac_security.dto.user.ChangePasswordDTO;
import br.com.adbrascosmopolis.saac_security.dto.user.UserRequestDTO;
import br.com.adbrascosmopolis.saac_security.dto.user.UserResponseDTO;
import br.com.adbrascosmopolis.saac_security.dto.user.UserUpdateDTO;
import br.com.adbrascosmopolis.saac_security.exception.EmailJaCadastradoException;
import br.com.adbrascosmopolis.saac_security.exception.InvalidCredentialsException;
import br.com.adbrascosmopolis.saac_security.exception.ResourceNotFoundException;
import br.com.adbrascosmopolis.saac_security.exception.SelfActionNotAllowedException;
import br.com.adbrascosmopolis.saac_security.model.User;
import br.com.adbrascosmopolis.saac_security.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponseDTO create(UserRequestDTO dto) {
        userRepository.findByEmailAndDeletedAtIsNull(dto.getEmail())
                .ifPresent(u -> {
                    throw new EmailJaCadastradoException("Já existe um usuário ativo com este e-mail");
                });

        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setUnityId(dto.getUnityId());
        user.setScopeType(dto.getScopeType());
        user.setActive(true);

        User salvo = userRepository.save(user);
        return toResponseDTO(salvo);
    }

    public List<UserResponseDTO> listAll() {
        return userRepository.findAllByDeletedAtIsNull()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    // --- NOVO: listagem paginada sem filtro (GLOBAL) ---
    public Page<UserResponseDTO> listAllPaged(Pageable pageable) {
        return userRepository.findAllByDeletedAtIsNull(pageable).map(this::toResponseDTO);
    }

    // --- NOVO: listagem paginada filtrada por unidade (LOCAL) ---
    public Page<UserResponseDTO> listByUnity(Long unityId, Pageable pageable) {
        return userRepository.findAllByDeletedAtIsNullAndUnityId(unityId, pageable).map(this::toResponseDTO);
    }

    public UserResponseDTO searchById(Long id) {
        User user = searchActiveOrFail(id);
        return toResponseDTO(user);
    }

    @Transactional
    public UserResponseDTO update(Long id, UserUpdateDTO dto) {
        User user = searchActiveOrFail(id);

        user.setName(dto.getName());
        user.setUnityId(dto.getUnityId());
        user.setScopeType(dto.getScopeType());

        if (dto.getActive() != null) {
            user.setActive(dto.getActive());
        }

        User updated = userRepository.save(user);
        return toResponseDTO(updated);
    }

    @Transactional
    public void delete(Long id, Long modifiedBy) {
        User user = searchActiveOrFail(id);
        user.setDeletedAt(LocalDateTime.now());
        user.setModifiedBy(modifiedBy);
        userRepository.save(user);
    }

    // --- NOVO: restaurar usuário soft-deletado ---
    @Transactional
    public UserResponseDTO restore(Long id, Long modifiedBy) {
        User user = userRepository.findDeletedById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuário deletado não encontrado com id: " + id));

        user.setDeletedAt(null);
        user.setModifiedBy(modifiedBy);

        User saved = userRepository.save(user);
        return toResponseDTO(saved);
    }

    // --- NOVO: troca de senha (próprio usuário ou GLOBAL) ---
    @Transactional
    public void changePassword(Long id, ChangePasswordDTO dto, Long requesterId, boolean requesterIsGlobal) {
        User user = searchActiveOrFail(id);

        boolean isSelf = user.getUserId().equals(requesterId);

        if (!isSelf && !requesterIsGlobal) {
            throw new InvalidCredentialsException("Sem permissão para alterar senha de outro usuário.");
        }

        if (isSelf && !passwordEncoder.matches(dto.currentPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Senha atual incorreta.");
        }

        user.setPassword(passwordEncoder.encode(dto.newPassword()));
        user.setModifiedBy(requesterId);

        userRepository.save(user);
    }

    // --- NOVO: proteção contra auto-exclusão ---
    public void validateNotSelfDeletion(Long targetId, Long requesterId) {
        if (targetId.equals(requesterId)) {
            throw new SelfActionNotAllowedException("Não é permitido excluir o próprio usuário.");
        }
    }

    private User searchActiveOrFail(Long id) {
        return userRepository.findById(id)
                .filter(u -> u.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com id: " + id));
    }

    private UserResponseDTO toResponseDTO(User user) {
        return new UserResponseDTO(
                user.getUserId(),
                user.getName(),
                user.getEmail(),
                user.isActive(),
                user.getUnityId(),
                user.getScopeType(),
                user.getCreatedAt()
        );
    }
}
