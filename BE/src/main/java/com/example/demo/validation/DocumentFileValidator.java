package com.example.demo.validation;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.tika.Tika;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

// Stessa filosofia di ImageFileValidator: non si fida ne'
// dell'estensione ne' del Content-Type dichiarato dal browser, rifa'
// i controlli leggendo davvero il contenuto del file.
@Component
public class DocumentFileValidator {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("pdf", "png", "jpg", "jpeg", "tif", "tiff");
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "application/pdf", "image/png", "image/jpeg", "image/tiff");

    private final Tika tika = new Tika();

    public record ValidatedDocument(String contentType) {
    }

    public ValidatedDocument validate(MultipartFile file) {
        if (file.isEmpty()) {
            throw new FileValidationException("Il file e' vuoto");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = extractExtension(originalFilename);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new FileValidationException("Estensione non consentita: ." + extension);
        }

        String detectedMimeType;
        try {
            detectedMimeType = tika.detect(file.getInputStream(), originalFilename);
        } catch (IOException e) {
            throw new FileValidationException("Impossibile leggere il file");
        }
        if (!ALLOWED_MIME_TYPES.contains(detectedMimeType)) {
            throw new FileValidationException("Formato file non supportato: " + detectedMimeType);
        }

        if ("application/pdf".equals(detectedMimeType)) {
            validatePdf(file);
        } else {
            validateImage(file);
        }

        return new ValidatedDocument(detectedMimeType);
    }

    private void validatePdf(MultipartFile file) {
        try (InputStream is = file.getInputStream(); PDDocument document = Loader.loadPDF(is.readAllBytes())) {
            if (document.getNumberOfPages() == 0) {
                throw new FileValidationException("Il PDF non contiene pagine");
            }
        } catch (IOException e) {
            throw new FileValidationException("Il file non e' un PDF leggibile");
        }
    }

    private void validateImage(MultipartFile file) {
        BufferedImage image;
        try (InputStream is = file.getInputStream()) {
            image = ImageIO.read(is);
        } catch (IOException e) {
            throw new FileValidationException("Errore durante la lettura dell'immagine");
        }
        if (image == null) {
            throw new FileValidationException("Il file non e' un'immagine leggibile");
        }
    }

    private String extractExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
    }
}
