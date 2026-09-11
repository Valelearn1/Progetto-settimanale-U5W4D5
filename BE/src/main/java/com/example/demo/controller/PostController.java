package com.example.demo.controller;

import com.example.demo.dto.request.PostCreateRequest;
import com.example.demo.dto.request.PostUpdateRequest;
import com.example.demo.dto.response.PostResponse;
import com.example.demo.service.PostService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    // CreaPost: la parte "post" e' JSON (testo, captureMode, location
    // opzionale); "photos" e "documents" sono file, entrambi
    // facoltativi (un post di solo testo e' valido).
    // Attenzione: ogni documento passa dall'OCR, che e' sincrono,
    // quindi allegandone si allunga il tempo di risposta.
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PostResponse> create(
            @RequestPart("post") @Valid PostCreateRequest request,
            @RequestPart(value = "photos", required = false) List<MultipartFile> photos,
            @RequestPart(value = "documents", required = false) List<MultipartFile> documents) {
        PostResponse response = postService.create(request, photos, documents);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ListaPost: paginato, i piu' recenti prima (l'ordinamento e'
    // forzato dal service, non serve passarlo come query param).
    @GetMapping
    public Page<PostResponse> list(Pageable pageable) {
        return postService.list(pageable);
    }

    @GetMapping("/{id}")
    public PostResponse getById(@PathVariable UUID id) {
        return postService.getById(id);
    }

    // ModificaPost: aggiorna solo testo e posizione, non tocca le foto.
    @PutMapping("/{id}")
    public PostResponse update(@PathVariable UUID id, @RequestBody @Valid PostUpdateRequest request) {
        return postService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        postService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // AggiungiFoto: solo per post UPLOAD (il service rifiuta i CAMERA).
    @PostMapping(value = "/{id}/photos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public PostResponse addPhoto(@PathVariable UUID id, @RequestParam("photo") MultipartFile photo) {
        return postService.addPhoto(id, photo);
    }

    @DeleteMapping("/{id}/photos/{photoId}")
    public PostResponse removePhoto(@PathVariable UUID id, @PathVariable UUID photoId) {
        return postService.removePhoto(id, photoId);
    }
}
