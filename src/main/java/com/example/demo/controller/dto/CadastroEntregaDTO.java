package com.example.demo.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CadastroEntregaDTO(
        @NotBlank(message = "O preenchimento da descrição da entrega é obrigatório")
        String descricao,
        @NotNull(message = "O preenchimento da nota da entrega é obrigatório")
        Double nota) {
}
