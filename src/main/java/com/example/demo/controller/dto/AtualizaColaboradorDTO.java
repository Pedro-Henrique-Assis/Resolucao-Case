package com.example.demo.controller.dto;

import jakarta.validation.constraints.Size;

public record AtualizaColaboradorDTO(
        @Size(min = 1, message = "A descrição não pode ser vazia.")
        String nome,
        @Size(min = 1, message = "O cargo não pode ser vazio.")
        String cargo) {
}
