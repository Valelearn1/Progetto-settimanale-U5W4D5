package com.example.demo.dto.response;

import com.example.demo.entity.Location;

import java.math.BigDecimal;

public record LocationResponse(
        BigDecimal latitude,
        BigDecimal longitude,
        String address
) {
    public static LocationResponse from(Location location) {
        // Se il post non ha posizione, Hibernate puo' restituire un
        // embeddable con tutti i campi null invece di un riferimento
        // null: si controlla la latitudine, non solo il riferimento.
        if (location == null || location.getLatitude() == null) {
            return null;
        }
        return new LocationResponse(location.getLatitude(), location.getLongitude(), location.getAddress());
    }
}
