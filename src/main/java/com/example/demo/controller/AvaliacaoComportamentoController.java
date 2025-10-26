package com.example.demo.controller;

import com.example.demo.business.services.AvaliacaoComportamentoService;
import com.example.demo.controller.dto.AtualizaAvaliacaoComportamentoDTO;
import com.example.demo.controller.dto.CadastroAvaliacaoComportamentoDTO;
import com.example.demo.controller.dto.AvaliacaoComportamentoRespostaDTO;
import jakarta.validation.Valid;
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
    public ResponseEntity<Void> cadastrarAvaliacaoComportamental(
            @PathVariable("matricula") String matricula,
            @Valid @RequestBody CadastroAvaliacaoComportamentoDTO cadastroAvaliacaoComportamentoDTO) {

        Long idAvaliacaoComportamento = avaliacaoComportamentoService.cadastrarAvaliacaoComportamental(matricula, cadastroAvaliacaoComportamentoDTO);

        URI location = URI.create(String.format("/api/colaborador/" + matricula + "/avaliacao/"+ idAvaliacaoComportamento));
        return ResponseEntity.created(location).build();
    }

    @GetMapping
    public ResponseEntity<AvaliacaoComportamentoRespostaDTO> consultaAvaliacaoPorMatricula(@PathVariable("matricula") String matricula) {
        var notas = avaliacaoComportamentoService.consultaAvaliacaoPorMatricula(matricula);

        return ResponseEntity.ok(notas);
    }

    @DeleteMapping
    public ResponseEntity<Void> deletarAvaliacoesPorMatricula(@PathVariable("matricula") String matricula) {
        avaliacaoComportamentoService.deletarAvaliacoesPorMatricula(matricula);

        return ResponseEntity.noContent().build();
    }

    @PutMapping
    public ResponseEntity<Void> atualizaAvaliacaoPorMarticula(
            @PathVariable("matricula") String matricula,
            @Valid @RequestBody AtualizaAvaliacaoComportamentoDTO atualizaAvaliacaoComportamentoDTO) {

        avaliacaoComportamentoService.atualizaAvaliacaoPorMarticula(matricula, atualizaAvaliacaoComportamentoDTO);

        return ResponseEntity.noContent().build();
    }
}
