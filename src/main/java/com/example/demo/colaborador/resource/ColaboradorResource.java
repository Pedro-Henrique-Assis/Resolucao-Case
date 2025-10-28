package com.example.demo.colaborador.resource;

import com.example.demo.colaborador.service.ColaboradorService;
import com.example.demo.colaborador.resource.json.ColaboradorAtualizaRequest;
import com.example.demo.colaborador.resource.json.ColaboradorCadastroRequest;
import com.example.demo.colaborador.resource.json.ColaboradorResponse;
import com.example.demo.colaborador.resource.json.ColaboradorPerformanceResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/api/v1/colaborador")
public class ColaboradorResource {

    private final ColaboradorService colaboradorService;

    // Injeção de dependência de ColaboradorService
    public ColaboradorResource(ColaboradorService colaboradorService) {
        this.colaboradorService = colaboradorService;
    }

    @PostMapping
    public ResponseEntity<Void> cadastrarColaborador(@Valid @RequestBody ColaboradorCadastroRequest colaboradorCadastroRequest) {
        var matriculaColaborador = colaboradorService.cadastrarColaborador(colaboradorCadastroRequest);

        // Cria a requisição e retorna o path com a matricula do colaborador cadastrado
        return ResponseEntity.created(URI.create("/api/v1/colaborador/" + matriculaColaborador.toString())).build();
    }

    @GetMapping("/{matricula}")
    public ResponseEntity<ColaboradorResponse> consultarColaboradorPorMatricula(@PathVariable("matricula") String matricula) {
        var colaboradorOpcional = colaboradorService.consultarColaboradorPorMatricula(matricula);

        if (colaboradorOpcional.isPresent()) {
            var colaborador = colaboradorOpcional.get();
            return ResponseEntity.ok(colaborador);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<ColaboradorResponse>> listarColaboradores() {
        var colaboradores = colaboradorService.listarColaboradores();

        return ResponseEntity.ok(colaboradores);
    }

    @GetMapping("/{matricula}/performance")
    public ResponseEntity<ColaboradorPerformanceResponse> calcularPerformanceFinal(@PathVariable("matricula") String matricula) {
        ColaboradorPerformanceResponse resultado = colaboradorService.calcularPerformanceFinal(matricula);

        return ResponseEntity.ok(resultado);
    }

    @DeleteMapping("/{matricula}")
    public ResponseEntity<Void> deletarColaboradorPorMatricula(@PathVariable("matricula") String matricula) {
        colaboradorService.deletarColaboradorPorMatricula(matricula);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{matricula}")
    public ResponseEntity<Void> atualizarColaboradorPorMatricula(@PathVariable("matricula") String matricula,
                                                                 @Valid @RequestBody ColaboradorAtualizaRequest colaboradorAtualizaRequest) {

        colaboradorService.atualizaColaboradorPorMatricula(matricula, colaboradorAtualizaRequest);
        return ResponseEntity.noContent().build();
    }
}
