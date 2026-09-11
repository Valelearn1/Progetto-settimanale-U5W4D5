package com.example.demo.controller;

import com.example.demo.dto.response.GeocodeResult;
import com.example.demo.service.GeocodingService;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/geo")
@Validated
public class GeoController {

    private final GeocodingService geocodingService;

    public GeoController(GeocodingService geocodingService) {
        this.geocodingService = geocodingService;
    }

    // CercaIndirizzo
    @GetMapping("/search")
    public List<GeocodeResult> search(@RequestParam("q") @NotBlank String query) {
        return geocodingService.search(query);
    }

    // IndirizzoDaCoordinate
    @GetMapping("/reverse")
    public GeocodeResult reverse(
            @RequestParam @DecimalMin("-90.0") @DecimalMax("90.0") BigDecimal lat,
            @RequestParam @DecimalMin("-180.0") @DecimalMax("180.0") BigDecimal lng) {
        return geocodingService.reverse(lat, lng);
    }
}
