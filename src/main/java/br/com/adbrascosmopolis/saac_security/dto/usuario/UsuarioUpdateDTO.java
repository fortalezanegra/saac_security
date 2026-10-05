package br.com.adbrascosmopolis.saac_security.dto.usuario;

import br.com.adbrascosmopolis.saac_security.enumeration.TipoEscopo;
import jakarta.validation.constraints.*;

public class UsuarioUpdateDTO {

    @NotBlank(message = "O nome é obrigatório")
    @Size(min = 3, max = 150)
    private String nome;

    @NotNull(message = "A unidade é obrigatória")
    private Long unidadeId;

    @NotNull(message = "O tipo de escopo é obrigatório")
    private TipoEscopo tipoEscopo;

    private Boolean ativo;

    // Getters e Setters

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public Long getUnidadeId() { return unidadeId; }
    public void setUnidadeId(Long unidadeId) { this.unidadeId = unidadeId; }

    public TipoEscopo getTipoEscopo() { return tipoEscopo; }
    public void setTipoEscopo(TipoEscopo tipoEscopo) { this.tipoEscopo = tipoEscopo; }

    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }
}
