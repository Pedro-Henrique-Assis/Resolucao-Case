package com.example.demo.colaborador.resource.json;

import java.util.UUID;

public record ColaboradorPerformanceResponse(
        UUID matricula,
        String nome,
        ColaboradorMediaPerformanceResponse performance
) {
}
