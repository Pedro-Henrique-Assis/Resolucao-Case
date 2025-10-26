package com.example.demo.controller.dto;

import java.math.BigDecimal;

public record MediaPerformanceDTO(
        BigDecimal mediaComportamental,
        BigDecimal mediaEntregas,
        BigDecimal notaFinal
) {
}
