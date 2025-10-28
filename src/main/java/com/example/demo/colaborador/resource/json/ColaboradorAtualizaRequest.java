package com.example.demo.colaborador.resource.json;

import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record ColaboradorAtualizaRequest(
        @Size(min = 1, message = "A descrição não pode ser vazia.")
        String nome,
        @PastOrPresent(message = "A data de admissão não pode ser no futuro.")
        LocalDate dataAdmissao,
        @Size(min = 1, message = "O cargo não pode ser vazio.")
        String cargo) {
}
