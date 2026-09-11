package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

// Non e' un'entita' a se stante: le sue colonne finiscono dentro la
// tabella "posts", perche' la posizione appartiene al post, non va
// riusata tra piu' post. Tutti i campi sono nullable, cosi' un post
// senza posizione resta valido (Location e' opzionale sul Post).
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Location {

    // BigDecimal e non Double: le coordinate devono restare esatte,
    // non approssimate dalla virgola mobile binaria.
    @Column(name = "latitude", precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 10, scale = 6)
    private BigDecimal longitude;

    @Column(name = "address")
    private String address;
}
