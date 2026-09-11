package com.example.demo.validation;

import org.apache.tika.Tika;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

// Rifa' da zero, lato backend, i controlli gia' fatti dal frontend:
// non si fida ne' dell'estensione del nome file ne' del Content-Type
// dichiarato dal browser, perche' entrambi sono facili da falsificare
// (basta rinominare un file, o chiamare l'API direttamente con
// Postman saltando il frontend).
@Component
public class ImageFileValidator {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    private final Tika tika = new Tika();

    public record ValidatedImage(String contentType, int width, int height) {
    }

    public ValidatedImage validate(MultipartFile file) {
        if (file.isEmpty()) {
            throw new FileValidationException("Il file e' vuoto");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = extractExtension(originalFilename);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new FileValidationException("Estensione non consentita: ." + extension);
        }

        // MIME rilevato leggendo davvero i primi byte del file, non
        // dichiarato dal client: e' questo che smaschera un file
        // rinominato ad arte (es. un .txt rinominato in .jpg).
        String detectedMimeType;
        try {
            detectedMimeType = tika.detect(file.getInputStream(), originalFilename);
        } catch (IOException e) {
            throw new FileValidationException("Impossibile leggere il file");
        }
        if (!ALLOWED_MIME_TYPES.contains(detectedMimeType)) {
            throw new FileValidationException("Formato file non supportato: " + detectedMimeType);
        }

        // Ulteriore riprova: se i byte non sono un'immagine
        // decodificabile, ImageIO restituisce null anche se Tika ha
        // riconosciuto la firma giusta (es. file troncato/corrotto).
        BufferedImage image;
        try (InputStream is = file.getInputStream()) {
            image = ImageIO.read(is);
        } catch (IOException e) {
            throw new FileValidationException("Errore durante la lettura dell'immagine");
        }
        if (image == null) {
            throw new FileValidationException("Il file non e' un'immagine leggibile");
        }

        return new ValidatedImage(detectedMimeType, image.getWidth(), image.getHeight());
    }

    private String extractExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
    }
}
