package com.example.demo.business.services;

import com.example.demo.controller.dto.AtualizaAvaliacaoComportamentoDTO;
import com.example.demo.controller.dto.AvaliacaoComportamentoDTO;
import com.example.demo.controller.dto.AvaliacaoComportamentoRespostaDTO;
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

    public Optional<AvaliacaoComportamentoRespostaDTO> consultaAvaliacaoPorMatricula(String matricula) {
        var colaboradorOpcional = colaboradorRepository.findById(UUID.fromString(matricula));

        return colaboradorOpcional
                .map(Colaborador::getAvaliacaoComportamento)
                .map(this::formataRespostaDTO);
    }

    public void atualizaAvaliacaoPorMarticula(
            String matricula,
            AtualizaAvaliacaoComportamentoDTO novasNotas) {

        var matriculaUUID = UUID.fromString(matricula);

        var colaboradorEntity = colaboradorRepository.findById(matriculaUUID);

        if (colaboradorEntity.isPresent()) {
            var colaborador = colaboradorEntity.get();

            var notas = colaborador.getAvaliacaoComportamento();

            if (novasNotas.notaAvaliacaoComportamental() != null) {
                notas.setNotaAvaliacaoComportamental(novasNotas.notaAvaliacaoComportamental());
            }

            if (novasNotas.notaAprendizado() != null) {
                notas.setNotaAprendizado(novasNotas.notaAprendizado());
            }

            if (novasNotas.notaTomadaDecisao() != null) {
                notas.setNotaTomadaDecisao(novasNotas.notaTomadaDecisao());
            }

            if (novasNotas.notaAutonomia() != null) {
                notas.setNotaAutonomia(novasNotas.notaAutonomia());
            }

            avaliacaoComportamentoRepository.save(notas);
        }

    }

    private AvaliacaoComportamentoRespostaDTO formataRespostaDTO(AvaliacaoComportamento avaliacao) {

        Byte notaAvaliacaoComportamental = avaliacao.getNotaAvaliacaoComportamental();
        Byte notaAprendizado = avaliacao.getNotaAprendizado();
        Byte notaTomadaDecisao = avaliacao.getNotaTomadaDecisao();
        Byte notaAutonomia = avaliacao.getNotaAutonomia();
        int soma = notaAvaliacaoComportamental + notaAprendizado + notaTomadaDecisao + notaAutonomia;
        float media = soma / 4.0f;

        var novasNotasDTO = new AvaliacaoComportamentoDTO(
                avaliacao.getNotaAvaliacaoComportamental(),
                avaliacao.getNotaAprendizado(),
                avaliacao.getNotaTomadaDecisao(),
                avaliacao.getNotaAutonomia(),
                media
        );

        return new AvaliacaoComportamentoRespostaDTO(novasNotasDTO);
    }
}
