package com.example.demo.business.services;

import com.example.demo.controller.dto.AtualizaColaboradorDTO;
import com.example.demo.controller.dto.AvaliacaoComportamentoDTO;
import com.example.demo.controller.dto.ColaboradorDTO;
import com.example.demo.controller.dto.ColaboradorRespostaDTO;
import com.example.demo.infrastructure.model.AvaliacaoComportamento;
import com.example.demo.infrastructure.model.Colaborador;
import com.example.demo.infrastructure.repository.ColaboradorRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ColaboradorService {

    private final ColaboradorRepository colaboradorRepository;

    //Injeção de dependência da classe ColaboradorRepository
    public ColaboradorService(ColaboradorRepository colaboradorRepository) {
        this.colaboradorRepository = colaboradorRepository;
    }

    public UUID cadastrarColaborador(ColaboradorDTO colaboradorDTO) {

        // DTO -> Entity
        var colaborador = new Colaborador();
        colaborador.setNome(colaboradorDTO.nome());
        colaborador.setDataAdmissao(colaboradorDTO.dataAdmissao());
        colaborador.setCargo(colaboradorDTO.cargo());

        var colaboradorSalvo = colaboradorRepository.save(colaborador);

        return colaboradorSalvo.getMatricula();
    }

    public Optional<ColaboradorRespostaDTO> consultarColaboradorPorMatricula(String matricula) {
        return colaboradorRepository
                .findById(UUID.fromString(matricula))
                .map(this::formatarJsonDTO);
    }

    public List<ColaboradorRespostaDTO> listarColaboradores() {
        return colaboradorRepository.findAll().stream().map(this::formatarJsonDTO).collect(Collectors.toList());
    }

    public void deletarColaboradorPorMatricula(String matricula) {
        var matriculaUUID = UUID.fromString(matricula);

        boolean colaboradorExiste = colaboradorRepository.existsById(matriculaUUID);

        if (colaboradorExiste) {
            colaboradorRepository.deleteById(matriculaUUID);
        }
    }

    public void atualizaColaboradorPorMatricula(String matricula, AtualizaColaboradorDTO atualizaColaboradorDTO) {
        var matriculaUUID = UUID.fromString(matricula);

        var colaboradorEntity = colaboradorRepository.findById(matriculaUUID);

        if (colaboradorEntity.isPresent()) {
            var colaborador = colaboradorEntity.get();

            if (atualizaColaboradorDTO.nome() != null) {
                colaborador.setNome(atualizaColaboradorDTO.nome());
            }

            if (atualizaColaboradorDTO.cargo() != null) {
                colaborador.setCargo(atualizaColaboradorDTO.cargo());
            }

            // Realiza a operação de update daquele colaborador específico
            colaboradorRepository.save(colaborador);
        }
    }

    private ColaboradorRespostaDTO formatarJsonDTO(Colaborador colaborador) {
        AvaliacaoComportamentoDTO notas = null;
        AvaliacaoComportamento avaliacao = colaborador.getAvaliacaoComportamento();

        if (avaliacao != null) {
            int soma = avaliacao.getNotaAvaliacaoComportamental() +
                    avaliacao.getNotaAprendizado() +
                    avaliacao.getNotaTomadaDecisao() +
                    avaliacao.getNotaAutonomia();

            float media = soma / 4.0f;

            notas = new AvaliacaoComportamentoDTO(
                    avaliacao.getNotaAvaliacaoComportamental(),
                    avaliacao.getNotaAprendizado(),
                    avaliacao.getNotaTomadaDecisao(),
                    avaliacao.getNotaAutonomia(),
                    media
            );
        }

        return new ColaboradorRespostaDTO(
                colaborador.getMatricula(),
                colaborador.getNome(),
                colaborador.getDataAdmissao(),
                colaborador.getCargo(),
                notas
        );
    }
}
