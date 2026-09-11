package com.example.demo.dto.response;

import java.math.BigDecimal;

public record GeocodeResult(
        BigDecimal latitude,
        BigDecimal longitude,
        String address
) {
}
