package com.example.demo.controller.dto;

import java.util.UUID;

public record PerformanceDTO(
        UUID matricula,
        String nome,
        MediaPerformanceDTO performance
) {
}
