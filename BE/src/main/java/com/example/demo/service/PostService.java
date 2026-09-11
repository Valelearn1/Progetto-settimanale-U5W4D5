package com.example.demo.service;

import com.example.demo.dto.request.LocationRequest;
import com.example.demo.dto.request.PostCreateRequest;
import com.example.demo.dto.request.PostUpdateRequest;
import com.example.demo.dto.response.PostResponse;
import com.example.demo.entity.Location;
import com.example.demo.entity.Photo;
import com.example.demo.entity.Post;
import com.example.demo.entity.enums.CaptureMode;
import com.example.demo.exception.ForbiddenException;
import com.example.demo.exception.InvalidFileException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.PostRepository;
import com.example.demo.storage.FileStorageService;
import com.example.demo.storage.StoredFile;
import com.example.demo.validation.FileValidationException;
import com.example.demo.validation.ImageFileValidator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class PostService {

    private static final int MAX_PHOTOS_PER_POST = 10;
    private static final int MAX_DOCUMENTS_PER_POST = 5;
    private static final DateTimeFormatter SUBFOLDER_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM");

    private final PostRepository postRepository;
    private final CurrentUserService currentUserService;
    private final ImageFileValidator imageFileValidator;
    private final FileStorageService fileStorageService;
    private final DocumentService documentService;

    public PostService(PostRepository postRepository,
                        CurrentUserService currentUserService,
                        ImageFileValidator imageFileValidator,
                        FileStorageService fileStorageService,
                        DocumentService documentService) {
        this.postRepository = postRepository;
        this.currentUserService = currentUserService;
        this.imageFileValidator = imageFileValidator;
        this.fileStorageService = fileStorageService;
        this.documentService = documentService;
    }

    @Transactional
    public PostResponse create(PostCreateRequest request,
                                List<MultipartFile> photos,
                                List<MultipartFile> documents) {
        // Foto e documenti sono entrambi facoltativi: un post di solo
        // testo e' valido.
        List<MultipartFile> files = photos == null ? List.of() : photos;
        List<MultipartFile> docs = documents == null ? List.of() : documents;
        validateCaptureModeRule(request.captureMode(), files.size());
        if (docs.size() > MAX_DOCUMENTS_PER_POST) {
            throw new IllegalArgumentException(
                    "un post puo' avere al massimo " + MAX_DOCUMENTS_PER_POST + " documenti");
        }

        // Si validano TUTTI i file prima di scriverne anche uno solo
        // su disco: cosi' se un file e' invalido non resta nessun
        // file orfano ne' un post a meta'.
        List<ImageFileValidator.ValidatedImage> validated = validateAll(files);

        Post post = Post.builder()
                .text(request.text())
                // Senza foto captureMode non ha significato: resta null.
                .captureMode(files.isEmpty() ? null : request.captureMode())
                .location(toLocation(request.location()))
                .user(currentUserService.getCurrentUser())
                .build();

        String subfolder = "posts/" + LocalDate.now().format(SUBFOLDER_FORMAT);
        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            ImageFileValidator.ValidatedImage info = validated.get(i);
            StoredFile stored = fileStorageService.store(file, subfolder);

            Photo photo = Photo.builder()
                    .filePath(stored.relativePath())
                    .contentType(info.contentType())
                    .sizeBytes(stored.sizeBytes())
                    .position(i)
                    .build();
            post.addPhoto(photo);
        }

        // saveAndFlush, non save: @CreationTimestamp/@UpdateTimestamp
        // vengono valorizzati da Hibernate solo al momento del flush
        // verso il DB, e la risposta li deve gia' avere popolati.
        Post saved = postRepository.saveAndFlush(post);

        // I documenti si allegano dopo che il post ha un id. Ognuno
        // passa dall'OCR, quindi con allegati pesanti la creazione del
        // post richiede piu' tempo.
        if (!docs.isEmpty()) {
            saved.getDocuments().addAll(documentService.storeAll(docs, saved));
        }

        return PostResponse.from(saved);
    }

    public Page<PostResponse> list(Pageable pageable) {
        Pageable sorted = orderedByCreatedAtDesc(pageable);
        return postRepository.findAll(sorted).map(PostResponse::from);
    }

    public PostResponse getById(UUID id) {
        return PostResponse.from(findPostOrThrow(id));
    }

    @Transactional
    public PostResponse update(UUID id, PostUpdateRequest request) {
        Post post = findOwnPostOrThrow(id);
        post.setText(request.text());
        post.setLocation(toLocation(request.location()));
        return PostResponse.from(postRepository.saveAndFlush(post));
    }

    @Transactional
    public void delete(UUID id) {
        Post post = findOwnPostOrThrow(id);
        for (Photo photo : post.getPhotos()) {
            fileStorageService.delete(photo.getFilePath());
        }
        // I documenti allegati non si cancellano: tornano nell'archivio
        // personale, perche' restano roba dell'utente. Vanno pero'
        // staccati prima, o la chiave esterna impedirebbe la delete.
        post.getDocuments().forEach(document -> document.setPost(null));
        post.getDocuments().clear();

        postRepository.delete(post);
    }

    @Transactional
    public PostResponse addPhoto(UUID postId, MultipartFile file) {
        Post post = findOwnPostOrThrow(postId);
        if (post.getCaptureMode() == CaptureMode.CAMERA) {
            throw new IllegalArgumentException("un post creato con la fotocamera puo' avere una sola foto");
        }
        if (post.getPhotos().size() >= MAX_PHOTOS_PER_POST) {
            throw new IllegalArgumentException("un post puo' avere al massimo " + MAX_PHOTOS_PER_POST + " foto");
        }

        ImageFileValidator.ValidatedImage info = validateSingle(file);
        String subfolder = "posts/" + LocalDate.now().format(SUBFOLDER_FORMAT);
        StoredFile stored = fileStorageService.store(file, subfolder);

        Photo photo = Photo.builder()
                .filePath(stored.relativePath())
                .contentType(info.contentType())
                .sizeBytes(stored.sizeBytes())
                .position(post.getPhotos().size())
                .build();
        post.addPhoto(photo);

        // Post nato senza foto: ora ne ha una, caricata via upload.
        if (post.getCaptureMode() == null) {
            post.setCaptureMode(CaptureMode.UPLOAD);
        }

        return PostResponse.from(postRepository.saveAndFlush(post));
    }

    @Transactional
    public PostResponse removePhoto(UUID postId, UUID photoId) {
        Post post = findOwnPostOrThrow(postId);
        Photo photo = post.getPhotos().stream()
                .filter(p -> p.getId().equals(photoId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Foto non trovata: " + photoId));

        fileStorageService.delete(photo.getFilePath());
        post.removePhoto(photo);

        // Rimossa l'ultima foto, il post resta valido come solo testo
        // e captureMode torna a non avere significato.
        if (post.getPhotos().isEmpty()) {
            post.setCaptureMode(null);
        }

        return PostResponse.from(postRepository.saveAndFlush(post));
    }

    private Post findPostOrThrow(UUID id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post non trovato: " + id));
    }

    /*
      Trova il post e verifica che appartenga a chi sta chiamando.
      Senza questo controllo un utente autenticato potrebbe modificare
      o cancellare i post di chiunque altro: essere collegati non
      basta, bisogna anche esserne i proprietari.
    */
    private Post findOwnPostOrThrow(UUID id) {
        Post post = findPostOrThrow(id);
        if (!post.getUser().getId().equals(currentUserService.getCurrentUser().getId())) {
            throw new ForbiddenException("Questo post non e' tuo");
        }
        return post;
    }

    private void validateCaptureModeRule(CaptureMode captureMode, int photoCount) {
        // Nessuna foto: post di solo testo, captureMode non serve.
        if (photoCount == 0) {
            return;
        }
        if (captureMode == null) {
            throw new IllegalArgumentException("captureMode e' obbligatorio quando il post ha delle foto");
        }
        if (captureMode == CaptureMode.CAMERA && photoCount != 1) {
            throw new IllegalArgumentException("un post creato con la fotocamera deve avere esattamente una foto");
        }
        if (captureMode == CaptureMode.UPLOAD && photoCount > MAX_PHOTOS_PER_POST) {
            throw new IllegalArgumentException("un post puo' avere al massimo " + MAX_PHOTOS_PER_POST + " foto");
        }
    }

    private List<ImageFileValidator.ValidatedImage> validateAll(List<MultipartFile> photos) {
        List<ImageFileValidator.ValidatedImage> results = new ArrayList<>();
        List<InvalidFileException.FileError> errors = new ArrayList<>();

        for (MultipartFile file : photos) {
            try {
                results.add(imageFileValidator.validate(file));
            } catch (FileValidationException e) {
                errors.add(new InvalidFileException.FileError(file.getOriginalFilename(), e.getMessage()));
            }
        }

        if (!errors.isEmpty()) {
            throw new InvalidFileException(errors);
        }
        return results;
    }

    private ImageFileValidator.ValidatedImage validateSingle(MultipartFile file) {
        try {
            return imageFileValidator.validate(file);
        } catch (FileValidationException e) {
            throw new InvalidFileException(List.of(new InvalidFileException.FileError(file.getOriginalFilename(), e.getMessage())));
        }
    }

    private Location toLocation(LocationRequest request) {
        if (request == null) {
            return null;
        }
        return Location.builder()
                .latitude(request.latitude())
                .longitude(request.longitude())
                .address(request.address())
                .build();
    }

    private Pageable orderedByCreatedAtDesc(Pageable pageable) {
        return org.springframework.data.domain.PageRequest.of(
                pageable.getPageNumber(), pageable.getPageSize(), Sort.by("createdAt").descending());
    }
}
