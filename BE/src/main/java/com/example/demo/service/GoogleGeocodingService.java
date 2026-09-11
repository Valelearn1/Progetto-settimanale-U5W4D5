package com.example.demo.service;

import com.example.demo.dto.response.GeocodeResult;
import com.example.demo.exception.GeocodingException;
import com.example.demo.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

// Chiama la Geocoding API di Google direttamente dal backend: la
// chiave non viene mai spedita al browser, resta solo qui.
@Service
public class GoogleGeocodingService implements GeocodingService {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;

    public GoogleGeocodingService(ObjectMapper objectMapper,
                                   @Value("${app.google.maps.api-key:}") String apiKey) {
        this.restClient = RestClient.builder()
                .baseUrl("https://maps.googleapis.com/maps/api/geocode/json")
                .build();
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
    }

    @Override
    public List<GeocodeResult> search(String query) {
        String body = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("address", query)
                        .queryParam("key", apiKey)
                        .build())
                .retrieve()
                .body(String.class);
        return parseResults(body);
    }

    @Override
    public GeocodeResult reverse(BigDecimal latitude, BigDecimal longitude) {
        String latlng = latitude.toPlainString() + "," + longitude.toPlainString();
        String body = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("latlng", latlng)
                        .queryParam("key", apiKey)
                        .build())
                .retrieve()
                .body(String.class);

        List<GeocodeResult> results = parseResults(body);
        if (results.isEmpty()) {
            throw new ResourceNotFoundException("Nessun indirizzo trovato per queste coordinate");
        }
        return results.getFirst();
    }

    // Pacchetto-privato apposta: testato direttamente in
    // GoogleGeocodingServiceTest con risposte JSON di esempio, senza
    // bisogno di chiamare davvero Google.
    List<GeocodeResult> parseResults(String responseBody) {
        JsonNode root;
        try {
            root = objectMapper.readTree(responseBody);
        } catch (JacksonException e) {
            throw new GeocodingException("Risposta non valida dal servizio di geocoding");
        }

        String status = root.path("status").asString();
        if ("ZERO_RESULTS".equals(status)) {
            return List.of();
        }
        if (!"OK".equals(status)) {
            String errorMessage = root.path("error_message").asString(status);
            throw new GeocodingException("Errore dal servizio di geocoding: " + errorMessage);
        }

        List<GeocodeResult> results = new ArrayList<>();
        for (JsonNode result : root.path("results")) {
            JsonNode location = result.path("geometry").path("location");
            // BigDecimal dal testo grezzo del JSON, non da asDouble():
            // altrimenti si reintroduce l'errore di arrotondamento
            // che BigDecimal serve proprio ad evitare.
            BigDecimal lat = new BigDecimal(location.path("lat").asString());
            BigDecimal lng = new BigDecimal(location.path("lng").asString());
            String address = result.path("formatted_address").asString();
            results.add(new GeocodeResult(lat, lng, address));
        }
        return results;
    }
}
