package com.example.demo.colaborador.entrega.resource;

import com.example.demo.colaborador.entrega.service.EntregaService;
import com.example.demo.colaborador.entrega.resource.json.EntregaAtualizaRequest;
import com.example.demo.colaborador.entrega.resource.json.EntregaCadastroRequest;
import com.example.demo.colaborador.entrega.resource.json.EntregaResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/colaborador/{matricula}/entrega")
public class EntregaResource {

    private final EntregaService entregaService;

    public EntregaResource(EntregaService entregaService) {
        this.entregaService = entregaService;
    }

    @PostMapping
    public ResponseEntity<EntregaResponse> cadastrarEntregaColaborador(@PathVariable("matricula") String matricula,
                                                                       @Valid @RequestBody EntregaCadastroRequest entregaCadastroRequest) {

        var novaEntrega = entregaService.cadastrarEntregaColaborador(matricula, entregaCadastroRequest);

        var entregaRespostaDTO = new EntregaResponse(
                novaEntrega.getId(),
                novaEntrega.getDescricao(),
                novaEntrega.getNota()
        );

        return ResponseEntity.created(
                URI.create("/api/v1/colaborador/" + matricula + "/entrega/" + novaEntrega.getId()))
                .body(entregaRespostaDTO);
    }

    @GetMapping
    public ResponseEntity<List<EntregaResponse>> listarEntregasPorColaborador(
            @PathVariable("matricula") String matricula) {

        List<EntregaResponse> entregas = entregaService.listarEntregasPorColaborador(matricula);
        return ResponseEntity.ok(entregas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntregaResponse> consultarEntregaPorId(@PathVariable("matricula") String matricula, @PathVariable("id") Long id) {
        var entregas = entregaService.consultarEntregaPorId(matricula, id);

        return ResponseEntity.ok(entregas);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> atualizarEntregaPorId(@PathVariable("matricula") String matricula,
                                                      @PathVariable("id") Long id,
                                                      @Valid @RequestBody EntregaAtualizaRequest entregaAtualizaRequest) {

        entregaService.atualizarEntregaPorId(matricula, id, entregaAtualizaRequest);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarEntregaColaborador(@PathVariable("matricula") String matricula, @PathVariable("id") Long id) {
        entregaService.deletarEntregaColaborador(matricula, id);

        return ResponseEntity.noContent().build();
    }
}
