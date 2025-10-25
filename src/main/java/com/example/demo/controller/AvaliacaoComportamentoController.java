package com.example.demo.controller;

import com.example.demo.business.services.AvaliacaoComportamentoService;
import com.example.demo.business.services.ColaboradorService;
import com.example.demo.controller.dto.AvaliacaoComportamentoDTO;
import com.example.demo.infrastructure.model.AvaliacaoComportamento;
import com.example.demo.infrastructure.repository.ColaboradorRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/colaborador/{matricula}/avaliacao")
public class AvaliacaoComportamentoController {

    private AvaliacaoComportamentoService avaliacaoComportamentoService;

    public AvaliacaoComportamentoController(AvaliacaoComportamentoService avaliacaoComportamentoService) {
        this.avaliacaoComportamentoService = avaliacaoComportamentoService;
    }

    @PostMapping
    public ResponseEntity<AvaliacaoComportamento> cadastrarAvaliacao(
            @PathVariable("matricula") String matricula,
            @RequestBody AvaliacaoComportamentoDTO avaliacaoComportamentoDTO) {

        var idAvaliacaoComportamento = avaliacaoComportamentoService.cadastrarAvaliacaoComportamental(matricula, avaliacaoComportamentoDTO);

        if (idAvaliacaoComportamento.isPresent()) {
            return ResponseEntity.created(URI.create("/api/colaborador/"+ matricula + "/" + idAvaliacaoComportamento.get())).build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
