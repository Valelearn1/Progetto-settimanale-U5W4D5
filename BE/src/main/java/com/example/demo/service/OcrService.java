package com.example.demo.service;

import com.example.demo.exception.OcrException;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;

// L'OCR e' sincrono (vedi PROGETTAZIONE.txt): CaricaDocumento aspetta
// che questo servizio finisca prima di rispondere.
@Service
public class OcrService {

    private static final float PDF_RENDER_DPI = 300f;

    private final String datapath;
    private final String language;

    public OcrService(@Value("${app.ocr.datapath}") String datapath,
                       @Value("${app.ocr.language}") String language,
                       @Value("${app.ocr.native-library-path:}") String nativeLibraryPath) {
        this.datapath = datapath;
        this.language = language;
        // JNA cerca libtesseract.dylib nei percorsi nativi standard,
        // ma Homebrew (specialmente su Apple Silicon) la installa
        // sotto /opt/homebrew/lib, che non ne fa parte di default.
        if (!nativeLibraryPath.isBlank()) {
            System.setProperty("jna.library.path", nativeLibraryPath);
        }
    }

    public String extractText(Path filePath, String contentType) {
        if ("application/pdf".equals(contentType)) {
            return extractFromPdf(filePath);
        }
        return extractFromImage(filePath);
    }

    private String extractFromImage(Path filePath) {
        try {
            return newTesseract().doOCR(filePath.toFile()).trim();
        } catch (TesseractException e) {
            throw new OcrException("Errore durante l'OCR dell'immagine", e);
        }
    }

    private String extractFromPdf(Path filePath) {
        // Una pagina per volta, renderizzata come immagine a 300 DPI:
        // Tesseract legge immagini, non il formato PDF direttamente.
        StringBuilder text = new StringBuilder();
        try (PDDocument document = Loader.loadPDF(filePath.toFile())) {
            PDFRenderer renderer = new PDFRenderer(document);
            // Nuova istanza per ogni documento: Tesseract non e'
            // thread-safe e non va riutilizzata tra chiamate.
            Tesseract tesseract = newTesseract();
            int pageCount = document.getNumberOfPages();

            for (int i = 0; i < pageCount; i++) {
                BufferedImage pageImage = renderer.renderImageWithDPI(i, PDF_RENDER_DPI);
                String pageText = tesseract.doOCR(pageImage).trim();
                if (pageCount > 1) {
                    text.append("--- pagina ").append(i + 1).append(" ---\n");
                }
                text.append(pageText).append("\n");
            }
        } catch (IOException | TesseractException e) {
            throw new OcrException("Errore durante l'OCR del PDF", e);
        }
        return text.toString().trim();
    }

    private Tesseract newTesseract() {
        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath(datapath);
        tesseract.setLanguage(language);
        return tesseract;
    }
}
