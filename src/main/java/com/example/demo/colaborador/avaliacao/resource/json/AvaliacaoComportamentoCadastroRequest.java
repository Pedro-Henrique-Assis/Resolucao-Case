package com.example.demo.colaborador.avaliacao.resource.json;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record AvaliacaoComportamentoCadastroRequest(
        @NotNull(message = "A nota de avaliação comportamental é obrigatória")
        @DecimalMin(value = "1.0", message = "A nota deve ser no mínimo 1.0")
        @DecimalMax(value = "5.0", message = "A nota deve ser no máximo 5.0")
        Double notaAvaliacaoComportamental,

        @NotNull(message = "A nota de avaliação de aprendizagem é obrigatória")
        @DecimalMin(value = "1.0", message = "A nota deve ser no mínimo 1.0")
        @DecimalMax(value = "5.0", message = "A nota deve ser no máximo 5.0")
        Double notaAprendizado,

        @NotNull(message = "A nota de avaliação de tomada de decisão é obrigatória")
        @DecimalMin(value = "1.0", message = "A nota deve ser no mínimo 1.0")
        @DecimalMax(value = "5.0", message = "A nota deve ser no máximo 5.0")
        Double notaTomadaDecisao,

        @NotNull(message = "A nota de avaliação de autonomia é obrigatória")
        @DecimalMin(value = "1.0", message = "A nota deve ser no mínimo 1.0")
        @DecimalMax(value = "5.0", message = "A nota deve ser no máximo 5.0")
        Double notaAutonomia
) {
}
