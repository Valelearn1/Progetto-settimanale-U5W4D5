package com.example.demo.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

/*
  Crea e verifica i token JWT.

  Un JWT e' una stringa in tre parti separate da punti: intestazione,
  contenuto e firma. Il contenuto NON e' cifrato, chiunque puo'
  leggerlo: quello che garantisce e' la firma, calcolata con una
  chiave segreta nota solo al server. Se qualcuno modifica il
  contenuto la firma non torna piu' e il token viene rifiutato.

  Per questo nel token si mette solo lo username, mai la password.
*/
@Service
public class JwtService {

    private final SecretKey key;
    private final long expirationMillis;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                       @Value("${app.jwt.expiration}") long expirationMillis) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMillis = expirationMillis;
    }

    public String generateToken(String username) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(username)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expirationMillis)))
                .signWith(key)
                .compact();
    }

    /*
      Restituisce lo username se il token e' valido e non scaduto,
      altrimenti Optional vuoto. Non lancia eccezioni: un token
      scaduto o malformato e' un caso normale, non un errore del
      programma, e chi chiama decide cosa farne.
    */
    public Optional<String> extractUsername(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Optional.ofNullable(claims.getSubject());
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public long getExpirationMillis() {
        return expirationMillis;
    }
}
