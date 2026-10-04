package br.com.adbrascosmopolis.saac_security.dto.usuario;

import br.com.adbrascosmopolis.saac_security.models.Usuario;

public class UsuarioResponseDTO {

    private Long usuarioId;
    private String nome;
    private String email;
    private Long unidadeId;
    private String tipoEscopo;

    public UsuarioResponseDTO(Usuario usuario) {
        this.usuarioId = usuario.getUsuarioId();
        this.nome = usuario.getNome();
        this.email = usuario.getEmail();
        this.unidadeId = usuario.getUnidadeId();
        this.tipoEscopo = usuario.getTipoEscopo().name();
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public Long getUnidadeId() {
        return unidadeId;
    }

    public String getTipoEscopo() {
        return tipoEscopo;
    }
}
