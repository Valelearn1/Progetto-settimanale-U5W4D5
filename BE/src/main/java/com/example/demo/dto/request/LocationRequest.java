package com.example.demo.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

// O il post ha una posizione completa (lat + lng, address opzionale),
// o non ce l'ha affatto: se questo oggetto e' presente, lat/lng sono
// obbligatorie.
public record LocationRequest(

        @NotNull(message = "la latitudine e' obbligatoria se si indica una posizione")
        @DecimalMin(value = "-90.0", message = "la latitudine deve essere tra -90 e 90")
        @DecimalMax(value = "90.0", message = "la latitudine deve essere tra -90 e 90")
        BigDecimal latitude,

        @NotNull(message = "la longitudine e' obbligatoria se si indica una posizione")
        @DecimalMin(value = "-180.0", message = "la longitudine deve essere tra -180 e 180")
        @DecimalMax(value = "180.0", message = "la longitudine deve essere tra -180 e 180")
        BigDecimal longitude,

        String address
) {
}
