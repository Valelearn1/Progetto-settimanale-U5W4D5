package com.example.demo.service;

import com.example.demo.dto.response.GeocodeResult;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// Verifica il parsing con risposte JSON di esempio nel formato
// documentato da Nominatim, senza chiamare la rete (cosi' il test
// resta veloce e non consuma il rate limit del servizio pubblico).
class NominatimGeocodingServiceTest {

    private final NominatimGeocodingService service =
            new NominatimGeocodingService(new ObjectMapper(), "test-agent");

    @Test
    void parsaIRisultatiDellaRicerca() {
        String json = """
                [
                  {
                    "lat": "45.0063191",
                    "lon": "7.8232574",
                    "display_name": "1, Via Roma, Chieri, Torino, Piemonte, 10023, Italia"
                  },
                  {
                    "lat": "45.1370571",
                    "lon": "7.0481821",
                    "display_name": "1, Via Roma, Susa, Torino, Piemonte, 10059, Italia"
                  }
                ]
                """;

        List<GeocodeResult> results = service.parseSearchResults(json);

        assertThat(results).hasSize(2);
        GeocodeResult first = results.getFirst();
        assertThat(first.address()).contains("Via Roma", "Chieri");
        // Nominatim manda lat/lon come stringhe: le convertiamo senza
        // passare da double, quindi il valore resta identico.
        assertThat(first.latitude()).isEqualByComparingTo(new BigDecimal("45.0063191"));
        assertThat(first.longitude()).isEqualByComparingTo(new BigDecimal("7.8232574"));
    }

    @Test
    void nessunRisultatoRestituisceListaVuota() {
        assertThat(service.parseSearchResults("[]")).isEmpty();
    }
}
