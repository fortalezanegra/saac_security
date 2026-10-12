package br.com.adbrascosmopolis.saac_security.dto.user;

import jakarta.validation.constraints.*;

public class UserUpdateDTO {

    @NotBlank(message = "O nome é obrigatório")
    @Size(min = 3, max = 150)
    private String name;

    @NotNull(message = "A unidade é obrigatória")
    private Long unityId;

    private Boolean active;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Long getUnityId() { return unityId; }
    public void setUnityId(Long unityId) { this.unityId = unityId; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
