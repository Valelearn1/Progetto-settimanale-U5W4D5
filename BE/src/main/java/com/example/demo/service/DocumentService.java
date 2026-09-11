package com.example.demo.service;

import com.example.demo.dto.response.DocumentResponse;
import com.example.demo.entity.Document;
import com.example.demo.entity.Post;
import com.example.demo.entity.User;
import com.example.demo.exception.ForbiddenException;
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
import java.util.ArrayList;
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

    // CaricaDocumento: senza post, finisce solo nell'archivio del profilo.
    @Transactional
    public DocumentResponse upload(MultipartFile file) {
        return DocumentResponse.from(store(file, null));
    }

    /*
      Salva il file, ne estrae il testo con l'OCR e crea il record.
      Se "post" e' valorizzato, il documento risulta allegato a quel
      post e comparira' nel feed; se e' null resta nell'archivio
      personale. Usato sia da CaricaDocumento sia da CreaPost.
    */
    @Transactional
    public Document store(MultipartFile file, Post post) {
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
                .post(post)
                .filePath(stored.relativePath())
                .contentType(info.contentType())
                .sizeBytes(stored.sizeBytes())
                .extractedText(extractedText)
                .build();

        return documentRepository.saveAndFlush(document);
    }

    @Transactional
    public List<Document> storeAll(List<MultipartFile> files, Post post) {
        List<Document> saved = new ArrayList<>();
        for (MultipartFile file : files) {
            saved.add(store(file, post));
        }
        return saved;
    }

    // MieiDocumenti: tutto l'archivio, inclusi quelli allegati a un post.
    public List<DocumentResponse> listMine() {
        User user = currentUserService.getCurrentUser();
        return documentRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(DocumentResponse::from)
                .toList();
    }

    public DocumentResponse getById(UUID id) {
        return DocumentResponse.from(findOwnOrThrow(id));
    }

    // Correzione a mano del testo letto dall'OCR, che sbaglia spesso
    // su scansioni storte o caratteri decorativi.
    @Transactional
    public DocumentResponse updateText(UUID id, String extractedText) {
        Document document = findOwnOrThrow(id);
        document.setExtractedText(extractedText);
        return DocumentResponse.from(documentRepository.saveAndFlush(document));
    }

    @Transactional
    public void delete(UUID id) {
        Document document = findOwnOrThrow(id);
        fileStorageService.delete(document.getFilePath());
        documentRepository.delete(document);
    }

    private Document findOrThrow(UUID id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Documento non trovato: " + id));
    }

    /*
      Trova il documento e verifica che sia di chi sta chiamando: un
      documento puo' contenere dati personali, quindi non basta essere
      autenticati per leggerlo o modificarlo.
    */
    private Document findOwnOrThrow(UUID id) {
        Document document = findOrThrow(id);
        if (!document.getUser().getId().equals(currentUserService.getCurrentUser().getId())) {
            throw new ForbiddenException("Questo documento non e' tuo");
        }
        return document;
    }
}
