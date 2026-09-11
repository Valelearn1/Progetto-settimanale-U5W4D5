package com.example.demo.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

// PUT ModificaPost: sostituisce testo e posizione, non tocca le foto.
// Se "location" e' assente/null, il post resta senza posizione.
public record PostUpdateRequest(

        @NotBlank(message = "il testo del post non puo' essere vuoto")
        String text,

        @Valid
        LocationRequest location
) {
}
