package com.example.demo.controller.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record DetalhesAvaliacaoComportamentoDTO(
        Double notaAvaliacaoComportamental,
        Double notaAprendizado,
        Double notaTomadaDecisao,
        Double notaAutonomia,
        BigDecimal mediaNotas) {
}
