package com.example.demo.service;

import com.example.demo.dto.response.GeocodeResult;
import com.example.demo.exception.GeocodingException;
import com.example.demo.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

// Geocoding tramite Nominatim (OpenStreetMap): gratuito, senza chiave
// e senza account di fatturazione, a differenza di Google Maps che
// richiede una carta registrata anche per la fascia gratuita.
//
// Le regole d'uso di Nominatim impongono due cose, entrambe
// rispettate qui sotto:
//  1. un User-Agent che identifichi l'applicazione (senza, bloccano);
//  2. al massimo 1 richiesta al secondo.
@Service
@ConditionalOnProperty(name = "app.geocoding.provider", havingValue = "nominatim", matchIfMissing = true)
public class NominatimGeocodingService implements GeocodingService {

    private static final long MIN_INTERVAL_MILLIS = 1000L;

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    // Ultimo istante in cui abbiamo chiamato Nominatim, per non
    // superare 1 richiesta al secondo.
    private long lastRequestAt = 0L;

    public NominatimGeocodingService(ObjectMapper objectMapper,
                                      @Value("${app.geocoding.user-agent}") String userAgent) {
        this.restClient = RestClient.builder()
                .baseUrl("https://nominatim.openstreetmap.org")
                .defaultHeader("User-Agent", userAgent)
                .build();
        this.objectMapper = objectMapper;
    }

    @Override
    public List<GeocodeResult> search(String query) {
        throttle();
        String body = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search")
                        .queryParam("q", query)
                        .queryParam("format", "jsonv2")
                        .queryParam("limit", 5)
                        .build())
                .retrieve()
                .body(String.class);
        return parseSearchResults(body);
    }

    @Override
    public GeocodeResult reverse(BigDecimal latitude, BigDecimal longitude) {
        throttle();
        String body = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/reverse")
                        .queryParam("lat", latitude.toPlainString())
                        .queryParam("lon", longitude.toPlainString())
                        .queryParam("format", "jsonv2")
                        .build())
                .retrieve()
                .body(String.class);

        JsonNode root = readTree(body);
        // Nominatim non usa un 404: segnala "nessun risultato" con un
        // campo "error" dentro una risposta HTTP 200.
        if (root.has("error")) {
            throw new ResourceNotFoundException("Nessun indirizzo trovato per queste coordinate");
        }
        return toResult(root);
    }

    // Aspetta, se necessario, perche' sia passato almeno un secondo
    // dalla chiamata precedente.
    private synchronized void throttle() {
        long now = System.currentTimeMillis();
        long elapsed = now - lastRequestAt;
        if (elapsed < MIN_INTERVAL_MILLIS) {
            try {
                Thread.sleep(MIN_INTERVAL_MILLIS - elapsed);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new GeocodingException("Richiesta di geocoding interrotta");
            }
        }
        lastRequestAt = System.currentTimeMillis();
    }

    // Pacchetto-privato: testato con risposte JSON di esempio in
    // NominatimGeocodingServiceTest, senza chiamare la rete.
    List<GeocodeResult> parseSearchResults(String responseBody) {
        JsonNode root = readTree(responseBody);
        List<GeocodeResult> results = new ArrayList<>();
        for (JsonNode node : root) {
            results.add(toResult(node));
        }
        return results;
    }

    private GeocodeResult toResult(JsonNode node) {
        // Nominatim restituisce lat/lon gia' come stringhe: le usiamo
        // direttamente, senza passare da double, per non perdere la
        // precisione che BigDecimal serve a garantire.
        BigDecimal lat = new BigDecimal(node.path("lat").asString());
        BigDecimal lon = new BigDecimal(node.path("lon").asString());
        String address = node.path("display_name").asString();
        return new GeocodeResult(lat, lon, address);
    }

    private JsonNode readTree(String responseBody) {
        try {
            return objectMapper.readTree(responseBody);
        } catch (JacksonException e) {
            throw new GeocodingException("Risposta non valida dal servizio di geocoding");
        }
    }
}
