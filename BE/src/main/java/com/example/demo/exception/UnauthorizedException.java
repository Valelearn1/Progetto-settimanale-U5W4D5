package com.example.demo.exception;

// Credenziali mancanti o non valide.
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
