package com.example.demo.controller.dto;

import java.time.LocalDate;

public record CadastroColaboradorDTO(String nome, LocalDate dataAdmissao, String cargo) {
}
