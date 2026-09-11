package com.example.demo.exception;

// Il dato inviato e' formalmente valido ma confligge con qualcosa che
// esiste gia' (es. uno username gia' preso).
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
