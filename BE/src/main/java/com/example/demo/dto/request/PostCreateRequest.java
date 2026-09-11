package com.example.demo.dto.request;

import com.example.demo.entity.enums.CaptureMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

public record PostCreateRequest(

        @NotBlank(message = "il testo del post non puo' essere vuoto")
        String text,

        // Obbligatorio solo se il post ha almeno una foto: il
        // controllo sta in PostService, perche' dipende dai file
        // allegati e non si puo' esprimere con una sola annotazione.
        CaptureMode captureMode,

        @Valid
        LocationRequest location
) {
}
