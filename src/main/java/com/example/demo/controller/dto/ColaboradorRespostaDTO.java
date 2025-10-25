package com.example.demo.controller.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

// Utilizado para formatar o JSON retornado pela consulta de colaboradores
public record ColaboradorRespostaDTO(
        UUID matricula,
        String nome,
        LocalDate dataAdmissao,
        String cargo,
        AvaliacaoComportamentoDTO avaliacaoComportamento,
        List<EntregaRepostaDTO> entregas
    ) {
}
