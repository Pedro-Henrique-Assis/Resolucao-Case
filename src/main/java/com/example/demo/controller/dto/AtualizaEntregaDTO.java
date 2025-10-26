package com.example.demo.controller.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AtualizaEntregaDTO(
        @Size(min = 1, message = "A descrição não pode ser vazia")
        String descricao,
        @DecimalMin(value = "1.0", message = "A nota deve ser no mínimo 1.0")
        @DecimalMax(value = "5.0", message = "A nota deve ser no máximo 5.0")
        Double nota) {
}
