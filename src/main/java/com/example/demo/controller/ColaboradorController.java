package com.example.demo.controller;

import com.example.demo.business.services.ColaboradorService;
import com.example.demo.controller.dto.AtualizaColaboradorDTO;
import com.example.demo.controller.dto.CadastroColaboradorDTO;
import com.example.demo.controller.dto.ColaboradorRespostaDTO;
import com.example.demo.controller.dto.PerformanceDTO;
import com.example.demo.infrastructure.model.Colaborador;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/colaborador")
public class ColaboradorController {

    private final ColaboradorService colaboradorService;

    // Injeção de dependência de ColaboradorService
    public ColaboradorController(ColaboradorService colaboradorService) {
        this.colaboradorService = colaboradorService;
    }

    @PostMapping
    public ResponseEntity<Colaborador> cadastrarColaborador(@RequestBody CadastroColaboradorDTO cadastroColaboradorDTO) {
        var matriculaColaborador = colaboradorService.cadastrarColaborador(cadastroColaboradorDTO);

        // Cria a requisição e retorna o path com a matricula do colaborador cadastrado
        return ResponseEntity.created(URI.create("/api/colaborador/" + matriculaColaborador.toString())).build();
    }

    @GetMapping("/{matricula}")
    public ResponseEntity<ColaboradorRespostaDTO> consultarColaboradorPorMatricula(@PathVariable("matricula") String matricula) {
        var colaboradorOpcional = colaboradorService.consultarColaboradorPorMatricula(matricula);

        if (colaboradorOpcional.isPresent()) {
            var colaborador = colaboradorOpcional.get();
            return ResponseEntity.ok(colaborador);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping()
    public ResponseEntity<List<ColaboradorRespostaDTO>> listarColaboradores() {
        var colaboradores = colaboradorService.listarColaboradores();

        return ResponseEntity.ok(colaboradores);
    }

    @GetMapping("/{matricula}/performance")
    public ResponseEntity<PerformanceDTO> calcularPerformanceFinal(@PathVariable("matricula") String matricula) {
        PerformanceDTO resultado = colaboradorService.calcularPerformanceFinal(matricula);

        return ResponseEntity.ok(resultado);
    }

    @DeleteMapping("/{matricula}")
    public ResponseEntity<Void> deletarColaboradorPorMatricula(@PathVariable("matricula") String matricula) {
        colaboradorService.deletarColaboradorPorMatricula(matricula);

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{matricula}")
    public ResponseEntity<Void> atualizarColaboradorPorMatricula(@PathVariable("matricula") String matricula,
                                                                 @RequestBody AtualizaColaboradorDTO atualizaColaboradorDTO) {

        colaboradorService.atualizaColaboradorPorMatricula(matricula, atualizaColaboradorDTO);
        return ResponseEntity.noContent().build();
    }
}
