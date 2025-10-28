package com.example.demo.colaborador.resource.json;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;

public record ColaboradorCadastroRequest(
        @NotBlank(message = "O preenchimento do nome é obrigatório")
        String nome,
        @NotNull(message = "O preenchimento da data de admissão é obrigatório")
        @PastOrPresent(message = "A data de admissão não pode ser no futuro.")
        LocalDate dataAdmissao,
        @NotBlank(message = "O preenchimento do cargo é obrigatório")
        String cargo) {
}
