package com.schachspiel.chess;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Der Einstiegspunkt der Schach-Anwendung.
 * <p>
 * Diese Klasse startet den Spring Boot Server, der die REST-API bereitstellt
 * und die Spiel-Logik hostet.
 * </p>
 */
@SpringBootApplication
public class ChessApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChessApplication.class, args);
    }
}
