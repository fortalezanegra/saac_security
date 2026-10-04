package br.com.adbrascosmopolis.saac_security.dto.login;

public class LoginResponseDTO {

    private String token;
    private String tipo = "Bearer";

    public LoginResponseDTO(String token) {
        this.token = token;
    }

    public String getToken() { return token; }
    public String getTipo() { return tipo; }
}
