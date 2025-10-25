package com.example.demo.controller.dto;

import java.time.LocalDate;

public record ColaboradorDTO(String nome, LocalDate dataAdmissao, String cargo) {
}
