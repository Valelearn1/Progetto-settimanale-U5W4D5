package com.example.demo.exception;

import lombok.Getter;

import java.util.List;

// Raggruppa gli errori di validazione di tutti i file di una stessa
// richiesta, cosi' con un upload multiplo si puo' dire all'utente
// esattamente quale file e' stato rifiutato e perche'.
@Getter
public class InvalidFileException extends RuntimeException {

    public record FileError(String fileName, String reason) {
    }

    private final List<FileError> errors;

    public InvalidFileException(List<FileError> errors) {
        super("Uno o piu' file non sono validi");
        this.errors = errors;
    }
}
