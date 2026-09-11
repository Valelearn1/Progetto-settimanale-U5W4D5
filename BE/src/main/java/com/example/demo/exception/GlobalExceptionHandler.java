package com.example.demo.exception;

import com.example.demo.dto.response.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.util.List;

// I metodi qui sotto non vengono mai chiamati da codice nostro: e'
// Spring a invocarli via reflection quando un controller lancia
// l'eccezione corrispondente. L'analisi statica non puo' saperlo e
// li segnalerebbe come inutilizzati.
@SuppressWarnings("unused")
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Formato file non valido (controllo backend, indipendente dal
    // frontend): elenca file per file cosa non va.
    @ExceptionHandler(InvalidFileException.class)
    public ResponseEntity<ErrorResponse> handleInvalidFile(InvalidFileException ex) {
        List<String> details = ex.getErrors().stream()
                .map(e -> e.fileName() + ": " + e.reason())
                .toList();
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body(ErrorResponse.of(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(), ex.getMessage(), details));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(HttpStatus.NOT_FOUND.value(), ex.getMessage()));
    }

    // Username o email gia' registrati.
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(ConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of(HttpStatus.CONFLICT.value(), ex.getMessage()));
    }

    // Credenziali sbagliate, oppure operazione che richiede l'accesso.
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(UnauthorizedException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.of(HttpStatus.UNAUTHORIZED.value(), ex.getMessage()));
    }

    // Autenticato, ma la risorsa e' di qualcun altro.
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(ForbiddenException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.of(HttpStatus.FORBIDDEN.value(), ex.getMessage()));
    }

    @ExceptionHandler(StorageException.class)
    public ResponseEntity<ErrorResponse> handleStorage(StorageException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage()));
    }

    // Regole di dominio violate, es. "CAMERA ammette una sola foto".
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(HttpStatus.BAD_REQUEST.value(), ex.getMessage()));
    }

    // Fallimenti di @Valid sui DTO (es. lat/lng fuori range).
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .toList();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(HttpStatus.BAD_REQUEST.value(), "Dati non validi", details));
    }

    // File piu' grande del limite configurato in application.properties.
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSize() {
        return ResponseEntity.status(HttpStatus.CONTENT_TOO_LARGE)
                .body(ErrorResponse.of(HttpStatus.CONTENT_TOO_LARGE.value(), "File troppo grande"));
    }

    // Manca del tutto la parte "post" o "photos" nella richiesta multipart.
    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ErrorResponse> handleMissingPart(MissingServletRequestPartException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(HttpStatus.BAD_REQUEST.value(), ex.getMessage()));
    }

    // Validazione sui @RequestParam (es. GeoController), diversa da
    // MethodArgumentNotValidException che riguarda solo @RequestBody.
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        List<String> details = ex.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .toList();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(HttpStatus.BAD_REQUEST.value(), "Dati non validi", details));
    }

    // Il servizio esterno di geocoding ha risposto con un errore.
    @ExceptionHandler(GeocodingException.class)
    public ResponseEntity<ErrorResponse> handleGeocoding(GeocodingException ex) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(ErrorResponse.of(HttpStatus.BAD_GATEWAY.value(), ex.getMessage()));
    }

    // L'estrazione del testo (Tesseract/PDFBox) e' fallita, es. file
    // corrotto sopravvissuto alla validazione o problema di setup.
    @ExceptionHandler(OcrException.class)
    public ResponseEntity<ErrorResponse> handleOcr(OcrException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage()));
    }
}
