package br.com.adbrascosmopolis.saac_security.dto.login;

public class LoginResponseDTO {

    private String token;
    private String refreshToken;
    private String tipo = "Bearer";

    public LoginResponseDTO(String token) {
        this.token = token;
    }

    public LoginResponseDTO(String token, String refreshToken) {
        this.token = token;
        this.refreshToken = refreshToken;
    }

    public LoginResponseDTO(String token, String refreshToken, String tipo) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.tipo = tipo;
    }

    public String getToken() { return token; }
    public String getRefreshToken() { return refreshToken; }
    public String getTipo() { return tipo; }
}
