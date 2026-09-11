package com.example.demo.service;

import com.example.demo.dto.response.DocumentResponse;
import com.example.demo.entity.Document;
import com.example.demo.entity.User;
import com.example.demo.exception.InvalidFileException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.DocumentRepository;
import com.example.demo.storage.FileStorageService;
import com.example.demo.storage.StoredFile;
import com.example.demo.validation.DocumentFileValidator;
import com.example.demo.validation.FileValidationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final CurrentUserService currentUserService;
    private final DocumentFileValidator documentFileValidator;
    private final FileStorageService fileStorageService;
    private final OcrService ocrService;

    public DocumentService(DocumentRepository documentRepository,
                            CurrentUserService currentUserService,
                            DocumentFileValidator documentFileValidator,
                            FileStorageService fileStorageService,
                            OcrService ocrService) {
        this.documentRepository = documentRepository;
        this.currentUserService = currentUserService;
        this.documentFileValidator = documentFileValidator;
        this.fileStorageService = fileStorageService;
        this.ocrService = ocrService;
    }

    // CaricaDocumento: salva il file, esegue subito l'OCR (sincrono)
    // e risponde solo a estrazione finita.
    @Transactional
    public DocumentResponse upload(MultipartFile file) {
        DocumentFileValidator.ValidatedDocument info;
        try {
            info = documentFileValidator.validate(file);
        } catch (FileValidationException e) {
            throw new InvalidFileException(
                    List.of(new InvalidFileException.FileError(file.getOriginalFilename(), e.getMessage())));
        }

        StoredFile stored = fileStorageService.store(file, "documents");
        Path storedPath = fileStorageService.resolve(stored.relativePath());
        String extractedText = ocrService.extractText(storedPath, info.contentType());

        Document document = Document.builder()
                .user(currentUserService.getCurrentUser())
                .filePath(stored.relativePath())
                .contentType(info.contentType())
                .sizeBytes(stored.sizeBytes())
                .extractedText(extractedText)
                .build();

        return DocumentResponse.from(documentRepository.saveAndFlush(document));
    }

    // MieiDocumenti
    public List<DocumentResponse> listMine() {
        User user = currentUserService.getCurrentUser();
        return documentRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(DocumentResponse::from)
                .toList();
    }

    public DocumentResponse getById(UUID id) {
        return DocumentResponse.from(findOrThrow(id));
    }

    @Transactional
    public void delete(UUID id) {
        Document document = findOrThrow(id);
        fileStorageService.delete(document.getFilePath());
        documentRepository.delete(document);
    }

    private Document findOrThrow(UUID id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Documento non trovato: " + id));
    }
}
