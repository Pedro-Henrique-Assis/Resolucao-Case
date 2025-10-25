package com.example.demo.controller.dto;

public record AvaliacaoComportamentoDTO(
        Byte notaAvaliacaoComportamental,
        Byte notaAprendizado,
        Byte notaTomadaDecisao,
        Byte notaAutonomia,
        double mediaNotas) {
}
