package com.example.demo.storage;

import com.example.demo.exception.StorageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
public class LocalFileStorage implements FileStorageService {

    private final Path root;

    public LocalFileStorage(@Value("${app.storage.root}") String storageRoot) {
        this.root = Path.of(storageRoot).toAbsolutePath().normalize();
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new StorageException("Impossibile creare la cartella di storage: " + root, e);
        }
    }

    @Override
    public StoredFile store(MultipartFile file, String subfolder) {
        Path targetDir = root.resolve(subfolder).normalize();
        try {
            Files.createDirectories(targetDir);
        } catch (IOException e) {
            throw new StorageException("Impossibile creare la cartella: " + targetDir, e);
        }

        // Nome generato, mai quello originale: evita collisioni tra
        // utenti diversi e problemi di path traversal (es. un nome
        // file tipo "../../altra-cartella/file").
        String extension = extractExtension(file.getOriginalFilename());
        String fileName = UUID.randomUUID() + (extension.isEmpty() ? "" : "." + extension);
        Path targetPath = targetDir.resolve(fileName);

        try {
            file.transferTo(targetPath);
        } catch (IOException e) {
            throw new StorageException("Impossibile salvare il file: " + fileName, e);
        }

        String relativePath = root.relativize(targetPath).toString().replace('\\', '/');
        return new StoredFile(relativePath, file.getSize());
    }

    @Override
    public Path resolve(String relativePath) {
        return root.resolve(relativePath).normalize();
    }

    @Override
    public void delete(String relativePath) {
        Path target = root.resolve(relativePath).normalize();
        if (!target.startsWith(root)) {
            throw new StorageException("Percorso file non valido: " + relativePath, null);
        }
        try {
            Files.deleteIfExists(target);
        } catch (IOException e) {
            throw new StorageException("Impossibile eliminare il file: " + relativePath, e);
        }
    }

    private String extractExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
    }
}
