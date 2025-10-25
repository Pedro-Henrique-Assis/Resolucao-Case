package com.example.demo.business.services;

import com.example.demo.controller.dto.AvaliacaoComportamentoDTO;
import com.example.demo.infrastructure.model.AvaliacaoComportamento;
import com.example.demo.infrastructure.model.Colaborador;
import com.example.demo.infrastructure.repository.AvaliacaoComportamentoRepository;
import com.example.demo.infrastructure.repository.ColaboradorRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class AvaliacaoComportamentoService {

    private AvaliacaoComportamentoRepository avaliacaoComportamentoRepository;

    private ColaboradorRepository colaboradorRepository;

    public AvaliacaoComportamentoService(
            AvaliacaoComportamentoRepository avaliacaoComportamentoRepository,
            ColaboradorRepository colaboradorRepository) {

        this.avaliacaoComportamentoRepository = avaliacaoComportamentoRepository;
        this.colaboradorRepository = colaboradorRepository;
    }

    public Optional<Long> cadastrarAvaliacaoComportamental(
            String matricula,
            AvaliacaoComportamentoDTO avaliacaoComportamentoDTO) {

        Optional<Colaborador> colaboradorOpcional = colaboradorRepository.findById(UUID.fromString(matricula));

        if (colaboradorOpcional.isEmpty()) {
            return Optional.empty();
        }

        var colaborador = colaboradorOpcional.get();

        //DTO -> Entity
        var avaliacaoComportamento = new AvaliacaoComportamento();
        avaliacaoComportamento.setNotaAvaliacaoComportamental(avaliacaoComportamentoDTO.notaAvaliacaoComportamental());
        avaliacaoComportamento.setNotaAprendizado(avaliacaoComportamentoDTO.notaAprendizado());
        avaliacaoComportamento.setNotaTomadaDecisao(avaliacaoComportamentoDTO.notaTomadaDecisao());
        avaliacaoComportamento.setNotaAutonomia(avaliacaoComportamentoDTO.notaAutonomia());
        avaliacaoComportamento.setColaborador(colaborador);

        var avaliacaoComportamentoSalva = avaliacaoComportamentoRepository.save(avaliacaoComportamento);

        return Optional.of(avaliacaoComportamentoSalva.getId());
    }
}
