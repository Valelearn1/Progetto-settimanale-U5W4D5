package com.example.demo.dto.request;

import jakarta.validation.constraints.NotNull;

// L'OCR sbaglia spesso, soprattutto con scansioni storte, caratteri
// decorativi o sfondi colorati: questo permette di correggere a mano
// il testo estratto. Stringa vuota ammessa (per svuotarlo del tutto),
// null no.
public record DocumentTextUpdateRequest(

        @NotNull(message = "il testo e' obbligatorio (usa una stringa vuota per svuotarlo)")
        String extractedText
) {
}
