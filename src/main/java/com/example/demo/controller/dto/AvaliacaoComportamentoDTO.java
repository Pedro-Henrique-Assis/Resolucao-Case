package com.example.demo.controller.dto;

import jakarta.validation.constraints.NotNull;

public record AvaliacaoComportamentoDTO(
        @NotNull(message = "A nota de avaliação comportamental é obrigatória.")
        Double notaAvaliacaoComportamental,
        @NotNull(message = "A nota de avaliação de aprendizagem é obrigatória.")
        Double notaAprendizado,
        @NotNull(message = "A nota de avaliação de tomada de decisão é obrigatória.")
        Double notaTomadaDecisao,
        @NotNull(message = "A nota de avaliação de autonomia é obrigatória.")
        Double notaAutonomia,
        Double mediaNotas) {
}
