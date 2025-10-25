package com.example.demo.controller;

import com.example.demo.business.services.ColaboradorService;

import com.example.demo.controller.dto.EntregaDTO;
import com.example.demo.controller.dto.EntregaRepostaDTO;
import com.example.demo.controller.dto.PerformanceDTO;
import com.example.demo.infrastructure.model.Entrega;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/colaborador/{matricula}/entrega")
public class EntregaController {

    private final ColaboradorService colaboradorService;

    public EntregaController(ColaboradorService colaboradorService) {
        this.colaboradorService = colaboradorService;
    }

    @PostMapping
    public ResponseEntity<EntregaRepostaDTO> cadastrarEntregaColaborador(@PathVariable("matricula") String matricula,
                                                                         @RequestBody EntregaDTO entregaDTO) {

        var novaEntrega = colaboradorService.cadastrarEntregaColaborador(matricula, entregaDTO);

        var entregaRespostaDTO = new EntregaRepostaDTO(
                novaEntrega.getId(),
                novaEntrega.getDescricao(),
                novaEntrega.getNota()
        );

        return ResponseEntity.created(
                URI.create("/api/colaborador/" + matricula + "/entrega" + novaEntrega.getId()))
                .body(entregaRespostaDTO);
    }

    @GetMapping
    public ResponseEntity<List<EntregaRepostaDTO>> listarEntregasPorColaborador(
            @PathVariable("matricula") String matricula) {

        List<EntregaRepostaDTO> entregas = colaboradorService.listarEntregasPorColaborador(matricula);
        return ResponseEntity.ok(entregas);
    }
}
