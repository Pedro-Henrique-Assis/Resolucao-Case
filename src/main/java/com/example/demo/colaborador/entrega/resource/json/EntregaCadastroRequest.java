package com.example.demo.colaborador.entrega.resource.json;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EntregaCadastroRequest(
        @NotBlank(message = "O preenchimento da descrição da entrega é obrigatório")
        String descricao,
        @NotNull(message = "O preenchimento da nota da entrega é obrigatório")
        @DecimalMin(value = "1.0", message = "A nota deve ser no mínimo 1.0")
        @DecimalMax(value = "5.0", message = "A nota deve ser no máximo 5.0")
        Double nota) {
}
