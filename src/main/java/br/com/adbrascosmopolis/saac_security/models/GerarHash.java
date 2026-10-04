package br.com.adbrascosmopolis.saac_security.models;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GerarHash {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String senhaPura = "SuaSenhaForteAqui123!";
        String hash = encoder.encode(senhaPura);
        System.out.println(hash);
    }
}
