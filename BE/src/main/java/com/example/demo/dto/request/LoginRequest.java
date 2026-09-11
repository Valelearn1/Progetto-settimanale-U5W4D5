package com.example.demo.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

        @NotBlank(message = "lo username e' obbligatorio")
        String username,

        @NotBlank(message = "la password e' obbligatoria")
        String password
) {
}
