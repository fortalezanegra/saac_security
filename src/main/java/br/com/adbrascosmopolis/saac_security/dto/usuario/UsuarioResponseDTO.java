package br.com.adbrascosmopolis.saac_security.dto.usuario;

import br.com.adbrascosmopolis.saac_security.enumeration.TipoEscopo;
import java.time.LocalDateTime;

public class UsuarioResponseDTO {

    private Long usuarioId;
    private String nome;
    private String email;
    private boolean ativo;
    private Long unidadeId;
    private TipoEscopo tipoEscopo;
    private LocalDateTime createdAt;

    public UsuarioResponseDTO(Long usuarioId, String nome, String email, Boolean ativo,
                              Long unidadeId, TipoEscopo tipoEscopo, LocalDateTime createdAt) {
        this.usuarioId = usuarioId;
        this.nome = nome;
        this.email = email;
        this.ativo = ativo;
        this.unidadeId = unidadeId;
        this.tipoEscopo = tipoEscopo;
        this.createdAt = createdAt;
    }

    // Getters

    public Long getUsuarioId() { return usuarioId; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public Boolean getAtivo() { return ativo; }
    public Long getUnidadeId() { return unidadeId; }
    public TipoEscopo getTipoEscopo() { return tipoEscopo; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
