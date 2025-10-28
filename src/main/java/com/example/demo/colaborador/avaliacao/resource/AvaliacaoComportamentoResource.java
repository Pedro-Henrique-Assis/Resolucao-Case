package com.example.demo.colaborador.avaliacao.resource;

import com.example.demo.colaborador.avaliacao.service.AvaliacaoComportamentoService;
import com.example.demo.colaborador.avaliacao.resource.json.AvaliacaoComportamentoAtualizaRequest;
import com.example.demo.colaborador.avaliacao.resource.json.AvaliacaoComportamentoCadastroRequest;
import com.example.demo.colaborador.avaliacao.resource.json.AvaliacaoComportamentoResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/colaborador/{matricula}/avaliacao")
public class AvaliacaoComportamentoResource {

    private final AvaliacaoComportamentoService avaliacaoComportamentoService;

    public AvaliacaoComportamentoResource(AvaliacaoComportamentoService avaliacaoComportamentoService) {
        this.avaliacaoComportamentoService = avaliacaoComportamentoService;
    }

    @PostMapping
    public ResponseEntity<Void> cadastrarAvaliacaoComportamental(
            @PathVariable("matricula") String matricula,
            @Valid @RequestBody AvaliacaoComportamentoCadastroRequest avaliacaoComportamentoCadastroRequest) {

        Long idAvaliacaoComportamento = avaliacaoComportamentoService.cadastrarAvaliacaoComportamental(matricula, avaliacaoComportamentoCadastroRequest);

        URI location = URI.create(String.format("/api/v1/colaborador/" + matricula + "/avaliacao/"+ idAvaliacaoComportamento));
        return ResponseEntity.created(location).build();
    }

    @GetMapping
    public ResponseEntity<AvaliacaoComportamentoResponse> consultaAvaliacaoPorMatricula(@PathVariable("matricula") String matricula) {
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
            @Valid @RequestBody AvaliacaoComportamentoAtualizaRequest avaliacaoComportamentoAtualizaRequest) {

        avaliacaoComportamentoService.atualizaAvaliacaoPorMarticula(matricula, avaliacaoComportamentoAtualizaRequest);

        return ResponseEntity.noContent().build();
    }
}
