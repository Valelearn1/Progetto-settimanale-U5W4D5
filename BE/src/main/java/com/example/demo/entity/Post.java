package com.example.demo.entity;

import com.example.demo.entity.enums.CaptureMode;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "posts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "text", columnDefinition = "TEXT")
    private String text;

    // Embedded: le colonne di Location finiscono dentro "posts".
    // Tutti nullable, quindi un post senza posizione e' valido.
    @Embedded
    private Location location;

    // Null quando il post non ha foto: il campo dice da dove arriva
    // la foto, quindi senza foto non ha significato.
    @Enumerated(EnumType.STRING)
    @Column(name = "capture_mode")
    private CaptureMode captureMode;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    // Corrisponde a "idUtente" nella progettazione: la colonna FK in
    // tabella si chiama "user_id", il campo Java e' l'oggetto User.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder.Default
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private List<Photo> photos = new ArrayList<>();

    // Documenti allegati al post. Niente orphanRemoval: staccando un
    // documento dal post questo torna nell'archivio del profilo,
    // non viene cancellato.
    @Builder.Default
    @OneToMany(mappedBy = "post")
    @OrderBy("createdAt ASC")
    private List<Document> documents = new ArrayList<>();

    // Helper per tenere sincronizzati i due lati della relazione
    // quando si aggiunge/rimuove una foto (serve a CreaPost e
    // AggiungiFoto/RimuoviFoto).
    public void addPhoto(Photo photo) {
        photos.add(photo);
        photo.setPost(this);
    }

    public void removePhoto(Photo photo) {
        photos.remove(photo);
        photo.setPost(null);
    }
}
