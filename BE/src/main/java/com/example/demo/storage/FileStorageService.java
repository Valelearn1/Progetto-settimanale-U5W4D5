package com.example.demo.storage;

import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

// Interfaccia separata dall'implementazione apposta: oggi i file
// finiscono su disco locale (LocalFileStorage), ma domani si potrebbe
// passare a uno storage esterno (es. Cloudinary) scrivendo una sola
// nuova classe, senza toccare i service che la usano.
public interface FileStorageService {

    StoredFile store(MultipartFile file, String subfolder);

    void delete(String relativePath);

    // Serve a chi deve leggere il contenuto del file gia' salvato
    // (es. OcrService, che passa il file a Tesseract/PDFBox).
    Path resolve(String relativePath);
}
