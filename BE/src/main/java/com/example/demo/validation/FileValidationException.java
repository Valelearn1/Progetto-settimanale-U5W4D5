package com.example.demo.validation;

// Motivo per cui un singolo file non ha passato la validazione.
// Chi chiama il validatore sa gia' il nome del file, quindi qui
// basta il motivo: il nome viene aggiunto da chi cattura l'eccezione.
public class FileValidationException extends RuntimeException {

    public FileValidationException(String reason) {
        super(reason);
    }
}
