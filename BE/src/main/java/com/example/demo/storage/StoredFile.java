package com.example.demo.storage;

// relativePath e' il percorso dentro la cartella di storage
// (es. "posts/2026/09/3f2a1b-c4.jpg"), non un path assoluto: e'
// quello che va salvato nel database (Photo.filePath, Document.filePath).
public record StoredFile(String relativePath, long sizeBytes) {
}
