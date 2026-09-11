package com.example.demo.service;

import com.example.demo.dto.response.GeocodeResult;

import java.math.BigDecimal;
import java.util.List;

// Interfaccia separata dall'implementazione (come FileStorageService):
// oggi la implementa Google, ma il resto del codice non dipende da
// Google, solo da questo contratto.
public interface GeocodingService {

    List<GeocodeResult> search(String query);

    GeocodeResult reverse(BigDecimal latitude, BigDecimal longitude);
}
