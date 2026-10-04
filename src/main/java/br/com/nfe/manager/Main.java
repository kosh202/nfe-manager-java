package br.com.nfe.manager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Ponto de entrada da aplicação Spring Boot. */
@SpringBootApplication
public class Main {

    public static void main(String[] args) {
        // Delega ao Spring a criação do contexto e a inicialização do servidor HTTP.
        SpringApplication.run(Main.class, args);
    }
}