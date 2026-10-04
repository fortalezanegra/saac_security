package br.com.adbrascosmopolis.saac_security.models;

import br.com.adbrascosmopolis.saac_security.enumeration.TipoEscopo;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "Usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long usuarioId;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false)
    private String senha; // hash BCrypt

    @Column(nullable = false)
    private boolean ativo = true;

    // 👇 Apenas o ID da unidade organizacional (matriz OU filial)
    @Column(name = "unidadeId", nullable = false)
    private Long unidadeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipoEscopo", nullable = false, length = 20)
    private TipoEscopo tipoEscopo; // GLOBAL ou LOCAL

    @CreationTimestamp
    @Column(name = "createdAt", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updatedAt")
    private LocalDateTime updatedAt;

    @Column(name = "deletedAt")
    private LocalDateTime deletedAt; // continua manual (soft delete)

    @Column(name = "modifiedBy")
    private Long modifiedBy;

    public Usuario(){}

    public Usuario(
            String nome,
            String email,
            String senha,
            Long unidadeId,
            TipoEscopo tipoEscopo
    ){
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.unidadeId = unidadeId;
        this.tipoEscopo = tipoEscopo;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public Long getUnidadeId() {
        return unidadeId;
    }

    public void setUnidadeId(Long unidadeId) {
        this.unidadeId = unidadeId;
    }

    public TipoEscopo getTipoEscopo() {
        return tipoEscopo;
    }

    public void setTipoEscopo(TipoEscopo tipoEscopo) {
        this.tipoEscopo = tipoEscopo;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    public Long getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(Long modifiedBy) {
        this.modifiedBy = modifiedBy;
    }
}
