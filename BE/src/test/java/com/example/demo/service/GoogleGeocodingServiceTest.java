package com.example.demo.service;

import com.example.demo.dto.response.GeocodeResult;
import com.example.demo.exception.GeocodingException;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// La chiave di Google Maps richiede la fatturazione attiva sul
// progetto Cloud per rispondere davvero (vedi PROGETTAZIONE.txt):
// questo test verifica il parsing usando risposte JSON di esempio
// nello stesso formato documentato da Google, senza chiamare la rete.
class GoogleGeocodingServiceTest {

    private final GoogleGeocodingService service = new GoogleGeocodingService(new ObjectMapper(), "");

    @Test
    void parsaUnRisultatoValido() {
        String json = """
                {
                  "results": [
                    {
                      "formatted_address": "Piazza del Duomo, 20122 Milano MI, Italy",
                      "geometry": {
                        "location": { "lat": 45.4641943, "lng": 9.1918655 }
                      }
                    }
                  ],
                  "status": "OK"
                }
                """;

        List<GeocodeResult> results = service.parseResults(json);

        assertThat(results).hasSize(1);
        GeocodeResult result = results.getFirst();
        assertThat(result.address()).isEqualTo("Piazza del Duomo, 20122 Milano MI, Italy");
        // BigDecimal esatto: non 45.464194299999... come sarebbe con un double.
        assertThat(result.latitude()).isEqualByComparingTo(new BigDecimal("45.4641943"));
        assertThat(result.longitude()).isEqualByComparingTo(new BigDecimal("9.1918655"));
    }

    @Test
    void nessunRisultatoRestituisceListaVuota() {
        String json = """
                { "results": [], "status": "ZERO_RESULTS" }
                """;

        assertThat(service.parseResults(json)).isEmpty();
    }

    @Test
    void statusDiErroreLanciaGeocodingException() {
        String json = """
                {
                  "results": [],
                  "status": "REQUEST_DENIED",
                  "error_message": "You must enable Billing on the Google Cloud Project"
                }
                """;

        assertThatThrownBy(() -> service.parseResults(json))
                .isInstanceOf(GeocodingException.class)
                .hasMessageContaining("Billing");
    }

    @Test
    void piuRisultatiVengonoParsatiTutti() {
        String json = """
                {
                  "results": [
                    { "formatted_address": "Via Roma 1, Torino", "geometry": { "location": { "lat": 45.07, "lng": 7.68 } } },
                    { "formatted_address": "Via Roma 1, Milano", "geometry": { "location": { "lat": 45.46, "lng": 9.19 } } }
                  ],
                  "status": "OK"
                }
                """;

        assertThat(service.parseResults(json)).hasSize(2);
    }
}
