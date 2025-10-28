package com.example.demo.colaborador.resource.json;

import java.math.BigDecimal;

public record ColaboradorMediaPerformanceResponse(
        BigDecimal mediaComportamental,
        BigDecimal mediaEntregas,
        BigDecimal notaFinal
) {
}
