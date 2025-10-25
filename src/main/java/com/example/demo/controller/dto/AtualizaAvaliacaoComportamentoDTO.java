package com.example.demo.controller.dto;

public record AtualizaAvaliacaoComportamentoDTO(
        Byte notaAvaliacaoComportamental,
        Byte notaAprendizado,
        Byte notaTomadaDecisao,
        Byte notaAutonomia) {
}
