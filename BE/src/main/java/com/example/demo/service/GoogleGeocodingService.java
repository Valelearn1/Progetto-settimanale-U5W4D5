package com.example.demo.service;

import com.example.demo.dto.response.GeocodeResult;
import com.example.demo.exception.GeocodingException;
import com.example.demo.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/*
  Geocoding tramite la Geocoding API v4 di Google.

  Perche' la v4 e non quella classica: la vecchia
  maps.googleapis.com/maps/api/geocode/json richiede un account di
  fatturazione attivo sul progetto Cloud (risponde REQUEST_DENIED
  senza), mentre la v4 su geocode.googleapis.com funziona con una
  chiave gratuita.

  Cambia anche il formato delle risposte: la v4 restituisce
  "formattedAddress" e un oggetto "location" con "latitude" e
  "longitude", invece di "formatted_address" e "geometry.location"
  con "lat"/"lng".

  La chiave resta sul backend e non viene mai spedita al browser.
*/
@Service
@ConditionalOnProperty(name = "app.geocoding.provider", havingValue = "google", matchIfMissing = true)
public class GoogleGeocodingService implements GeocodingService {

    private static final int MAX_RESULTS = 5;

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;

    public GoogleGeocodingService(ObjectMapper objectMapper,
                                   @Value("${app.google.maps.api-key:}") String apiKey) {
        this.restClient = RestClient.builder()
                .baseUrl("https://geocode.googleapis.com/v4/geocode")
                .build();
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
    }

    @Override
    public List<GeocodeResult> search(String query) {
        // L'indirizzo fa parte del percorso, non e' un parametro:
        // /v4/geocode/address/{indirizzo}
        String body = get(uriBuilder -> uriBuilder
                .pathSegment("address", query)
                .queryParam("key", apiKey)
                .build());
        return parseResults(body);
    }

    @Override
    public GeocodeResult reverse(BigDecimal latitude, BigDecimal longitude) {
        // Qui latitudine e longitudine vanno unite da una virgola in
        // un unico segmento: /v4/geocode/location/{lat},{lng}
        String latlng = latitude.toPlainString() + "," + longitude.toPlainString();
        String body = get(uriBuilder -> uriBuilder
                .pathSegment("location", latlng)
                .queryParam("key", apiKey)
                .build());

        List<GeocodeResult> results = parseResults(body);
        if (results.isEmpty()) {
            throw new ResourceNotFoundException("Nessun indirizzo trovato per queste coordinate");
        }
        return results.getFirst();
    }

    private String get(java.util.function.Function<org.springframework.web.util.UriBuilder, java.net.URI> uri) {
        try {
            return restClient.get().uri(uri).retrieve().body(String.class);
        } catch (RestClientResponseException e) {
            throw new GeocodingException(
                    "Errore dal servizio di geocoding: " + extractError(e.getResponseBodyAsString()));
        }
    }

    // Gli errori della v4 arrivano come { "error": { "message": ... } }.
    private String extractError(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            String message = root.path("error").path("message").asString("");
            return message.isBlank() ? responseBody : message;
        } catch (JacksonException e) {
            return responseBody;
        }
    }

    // Pacchetto-privato: testato in GoogleGeocodingServiceTest con
    // risposte JSON di esempio, senza chiamare la rete.
    List<GeocodeResult> parseResults(String responseBody) {
        JsonNode root;
        try {
            root = objectMapper.readTree(responseBody);
        } catch (JacksonException e) {
            throw new GeocodingException("Risposta non valida dal servizio di geocoding");
        }

        List<GeocodeResult> results = new ArrayList<>();
        for (JsonNode result : root.path("results")) {
            JsonNode location = result.path("location");
            // BigDecimal dal testo grezzo del JSON, non da asDouble():
            // altrimenti si reintroduce l'errore di arrotondamento che
            // BigDecimal serve proprio ad evitare.
            BigDecimal lat = new BigDecimal(location.path("latitude").asString());
            BigDecimal lng = new BigDecimal(location.path("longitude").asString());
            String address = result.path("formattedAddress").asString();
            results.add(new GeocodeResult(lat, lng, address));

            if (results.size() == MAX_RESULTS) break;
        }
        return results;
    }
}
