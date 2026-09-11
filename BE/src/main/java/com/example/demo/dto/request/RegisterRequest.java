package com.example.demo.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank(message = "il nome completo e' obbligatorio")
        String nomeCompleto,

        @NotBlank(message = "lo username e' obbligatorio")
        @Size(min = 3, max = 30, message = "lo username deve avere tra 3 e 30 caratteri")
        String username,

        @NotBlank(message = "l'email e' obbligatoria")
        @Email(message = "l'email non e' valida")
        String email,

        @NotBlank(message = "la password e' obbligatoria")
        @Size(min = 8, message = "la password deve avere almeno 8 caratteri")
        String password
) {
}
