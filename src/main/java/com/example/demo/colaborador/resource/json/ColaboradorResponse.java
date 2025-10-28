package com.example.demo.colaborador.resource.json;

import com.example.demo.colaborador.avaliacao.resource.json.AvaliacaoComportamentoResponse;
import com.example.demo.colaborador.entrega.resource.json.EntregaResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

// Utilizado para formatar o JSON retornado pela consulta de colaboradores
public record ColaboradorResponse(
        UUID matricula,
        String nome,
        LocalDate dataAdmissao,
        String cargo,
        AvaliacaoComportamentoResponse avaliacaoComportamento,
        List<EntregaResponse> entregas
    ) {
}
