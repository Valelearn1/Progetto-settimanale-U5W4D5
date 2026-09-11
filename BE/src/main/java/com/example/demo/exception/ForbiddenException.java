package com.example.demo.exception;

// L'utente e' autenticato ma sta cercando di toccare roba di
// qualcun altro.
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }
}
