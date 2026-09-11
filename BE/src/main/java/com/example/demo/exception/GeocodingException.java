package com.example.demo.exception;

// Il servizio esterno di geocoding (Google) ha risposto con un
// errore o con un formato inatteso.
public class GeocodingException extends RuntimeException {

    public GeocodingException(String message) {
        super(message);
    }
}
