package com.example.demo.controller;

import com.example.demo.business.services.AvaliacaoComportamentoService;
import com.example.demo.controller.dto.AtualizaAvaliacaoComportamentoDTO;
import com.example.demo.controller.dto.AvaliacaoComportamentoDTO;
import com.example.demo.controller.dto.AvaliacaoComportamentoRespostaDTO;
import com.example.demo.infrastructure.model.AvaliacaoComportamento;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Optional;

@RestController
@RequestMapping("/api/colaborador/{matricula}/avaliacao")
public class AvaliacaoComportamentoController {

    private final AvaliacaoComportamentoService avaliacaoComportamentoService;

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

    @GetMapping
    public ResponseEntity<AvaliacaoComportamentoRespostaDTO> consultaAvaliacaoPorMatricula(@PathVariable("matricula") String matricula) {
        Optional<AvaliacaoComportamentoRespostaDTO> notasOpcional = avaliacaoComportamentoService.consultaAvaliacaoPorMatricula(matricula);

        if (notasOpcional.isPresent()) {
            var notas = notasOpcional.get();
            return ResponseEntity.ok(notas);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping
    public ResponseEntity<Void> atualizaAvaliacaoPorMarticula(
            @PathVariable("matricula") String matricula,
            @RequestBody AtualizaAvaliacaoComportamentoDTO atualizaAvaliacaoComportamentoDTO) {

        avaliacaoComportamentoService.atualizaAvaliacaoPorMarticula(matricula, atualizaAvaliacaoComportamentoDTO);

        return ResponseEntity.noContent().build();
    }
}
