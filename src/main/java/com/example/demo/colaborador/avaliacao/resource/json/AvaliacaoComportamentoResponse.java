package com.example.demo.colaborador.avaliacao.resource.json;

import java.math.BigDecimal;

public record AvaliacaoComportamentoResponse(
        Double notaAvaliacaoComportamental,
        Double notaAprendizado,
        Double notaTomadaDecisao,
        Double notaAutonomia,
        BigDecimal mediaNotas) {
}
