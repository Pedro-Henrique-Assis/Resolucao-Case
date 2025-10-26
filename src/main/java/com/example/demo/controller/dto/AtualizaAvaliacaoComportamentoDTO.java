package com.example.demo.controller.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;

public record AtualizaAvaliacaoComportamentoDTO(

        @DecimalMin(value = "1.0", message = "A nota deve ser no mínimo 1.0")
        @DecimalMax(value = "5.0", message = "A nota deve ser no máximo 5.0")
        Double notaAvaliacaoComportamental,
        @DecimalMin(value = "1.0", message = "A nota deve ser no mínimo 1.0")
        @DecimalMax(value = "5.0", message = "A nota deve ser no máximo 5.0")
        Double notaAprendizado,
        @DecimalMin(value = "1.0", message = "A nota deve ser no mínimo 1.0")
        @DecimalMax(value = "5.0", message = "A nota deve ser no máximo 5.0")
        Double notaTomadaDecisao,
        @DecimalMin(value = "1.0", message = "A nota deve ser no mínimo 1.0")
        @DecimalMax(value = "5.0", message = "A nota deve ser no máximo 5.0")
        Double notaAutonomia) {
}
