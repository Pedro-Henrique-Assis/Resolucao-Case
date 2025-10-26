package com.example.demo.controller;

import com.example.demo.business.services.EntregaService;
import com.example.demo.controller.dto.AtualizaEntregaDTO;
import com.example.demo.controller.dto.CadastroEntregaDTO;
import com.example.demo.controller.dto.EntregaRepostaDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/colaborador/{matricula}/entrega")
public class EntregaController {

    private final EntregaService entregaService;

    public EntregaController(EntregaService entregaService) {
        this.entregaService = entregaService;
    }

    @PostMapping
    public ResponseEntity<EntregaRepostaDTO> cadastrarEntregaColaborador(@PathVariable("matricula") String matricula,
                                                                         @Valid @RequestBody CadastroEntregaDTO cadastroEntregaDTO) {

        var novaEntrega = entregaService.cadastrarEntregaColaborador(matricula, cadastroEntregaDTO);

        var entregaRespostaDTO = new EntregaRepostaDTO(
                novaEntrega.getId(),
                novaEntrega.getDescricao(),
                novaEntrega.getNota()
        );

        return ResponseEntity.created(
                URI.create("/api/colaborador/" + matricula + "/entrega/" + novaEntrega.getId()))
                .body(entregaRespostaDTO);
    }

    @GetMapping
    public ResponseEntity<List<EntregaRepostaDTO>> listarEntregasPorColaborador(
            @PathVariable("matricula") String matricula) {

        List<EntregaRepostaDTO> entregas = entregaService.listarEntregasPorColaborador(matricula);
        return ResponseEntity.ok(entregas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntregaRepostaDTO> consultarEntregaPorId(@PathVariable("matricula") String matricula, @PathVariable("id") Long id) {
        var entregas = entregaService.consultarEntregaPorId(matricula, id);

        return ResponseEntity.ok(entregas);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> atualizarEntregaPorId(@PathVariable("matricula") String matricula,
                                                      @PathVariable("id") Long id,
                                                      @Valid @RequestBody AtualizaEntregaDTO atualizaEntregaDTO) {

        entregaService.atualizarEntregaPorId(matricula, id, atualizaEntregaDTO);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarEntregaColaborador(@PathVariable("matricula") String matricula, @PathVariable("id") Long id) {
        entregaService.deletarEntregaColaborador(matricula, id);

        return ResponseEntity.noContent().build();
    }
}
