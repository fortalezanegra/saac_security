package br.com.adbrascosmopolis.saac_security.model;

import java.util.Base64;
import java.util.Scanner;

public class GerarJwtSecret {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Digite a senha/texto a ser codificado em Base64: ");
        String senhaPura = scanner.nextLine();

        String base64 = Base64.getEncoder().encodeToString(senhaPura.getBytes());

        System.out.println("Base64 gerado: " + base64);
        scanner.close();
    }
}
