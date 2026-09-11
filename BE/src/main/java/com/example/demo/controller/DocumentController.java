package com.example.demo.controller;

import com.example.demo.dto.request.DocumentTextUpdateRequest;
import com.example.demo.dto.response.DocumentResponse;
import com.example.demo.service.DocumentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    // CaricaDocumento
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentResponse> upload(@RequestParam("file") MultipartFile file) {
        DocumentResponse response = documentService.upload(file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // MieiDocumenti
    @GetMapping
    public List<DocumentResponse> listMine() {
        return documentService.listMine();
    }

    // DettaglioDocumento
    @GetMapping("/{id}")
    public DocumentResponse getById(@PathVariable UUID id) {
        return documentService.getById(id);
    }

    // CorreggiTesto: l'OCR non e' infallibile, il testo si puo' sistemare a mano.
    @PatchMapping("/{id}/text")
    public DocumentResponse updateText(@PathVariable UUID id,
                                        @RequestBody @Valid DocumentTextUpdateRequest request) {
        return documentService.updateText(id, request.extractedText());
    }

    // EliminaDocumento
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        documentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
