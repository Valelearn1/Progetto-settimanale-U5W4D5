package com.example.demo.service;

import com.example.demo.dto.response.GeocodeResult;
import com.example.demo.exception.GeocodingException;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/*
  Verifica il parsing delle risposte della Geocoding API v4 usando
  JSON di esempio, senza chiamare la rete: cosi' i test restano veloci
  e non consumano quota.

  Nota sul formato: la v4 usa "formattedAddress" e un oggetto
  "location" con "latitude"/"longitude", mentre la vecchia API usava
  "formatted_address" e "geometry.location" con "lat"/"lng".
*/
class GoogleGeocodingServiceTest {

    private final GoogleGeocodingService service =
            new GoogleGeocodingService(new ObjectMapper(), "");

    @Test
    void parsaUnRisultatoValido() {
        String json = """
                {
                  "results": [
                    {
                      "placeId": "ChIJ34j7SnBtiEcRH3-vac7QRjQ",
                      "location": { "latitude": 45.0704118, "longitude": 7.6846901 },
                      "granularity": "ROOFTOP",
                      "formattedAddress": "Via Roma, 1, 10123 Torino TO, Italia"
                    }
                  ]
                }
                """;

        List<GeocodeResult> results = service.parseResults(json);

        assertThat(results).hasSize(1);
        GeocodeResult result = results.getFirst();
        assertThat(result.address()).isEqualTo("Via Roma, 1, 10123 Torino TO, Italia");
        // BigDecimal esatto: non 45.07041179999... come sarebbe con un double.
        assertThat(result.latitude()).isEqualByComparingTo(new BigDecimal("45.0704118"));
        assertThat(result.longitude()).isEqualByComparingTo(new BigDecimal("7.6846901"));
    }

    @Test
    void nessunRisultatoRestituisceListaVuota() {
        assertThat(service.parseResults("{}")).isEmpty();
        assertThat(service.parseResults("{\"results\": []}")).isEmpty();
    }

    @Test
    void piuRisultatiVengonoParsatiTutti() {
        String json = """
                {
                  "results": [
                    { "location": { "latitude": 45.07, "longitude": 7.68 }, "formattedAddress": "Torino" },
                    { "location": { "latitude": 45.46, "longitude": 9.19 }, "formattedAddress": "Milano" }
                  ]
                }
                """;

        assertThat(service.parseResults(json))
                .extracting(GeocodeResult::address)
                .containsExactly("Torino", "Milano");
    }

    @Test
    void rispostaNonJsonLanciaGeocodingException() {
        assertThatThrownBy(() -> service.parseResults("<html>errore</html>"))
                .isInstanceOf(GeocodingException.class)
                .hasMessageContaining("Risposta non valida");
    }
}
