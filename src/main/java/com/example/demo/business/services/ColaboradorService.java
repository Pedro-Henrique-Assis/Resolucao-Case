package com.example.demo.business.services;

import com.example.demo.controller.dto.*;
import com.example.demo.infrastructure.model.AvaliacaoComportamento;
import com.example.demo.infrastructure.model.Colaborador;
import com.example.demo.infrastructure.model.Entrega;
import com.example.demo.infrastructure.repository.ColaboradorRepository;
import com.example.demo.infrastructure.repository.EntregaRepository;
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

    public UUID cadastrarColaborador(CadastroColaboradorDTO cadastroColaboradorDTO) {

        // DTO -> Entity
        var colaborador = new Colaborador();
        colaborador.setNome(cadastroColaboradorDTO.nome());
        colaborador.setDataAdmissao(cadastroColaboradorDTO.dataAdmissao());
        colaborador.setCargo(cadastroColaboradorDTO.cargo());

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

    public PerformanceDTO calcularPerformanceFinal(String matricula) {
        var matriculaUUID = UUID.fromString(matricula);

        var colaborador = colaboradorRepository.findById(matriculaUUID)
                .orElseThrow(() -> new RuntimeException("Colaborador não encontrado"));

        var avaliacaoComportamento = colaborador.getAvaliacaoComportamento();

        if (avaliacaoComportamento == null) {
            throw new RuntimeException("Avaliação comportamental não foi realizada.");
        }

        int somaAvaliacaoComportamento =
                avaliacaoComportamento.getNotaAvaliacaoComportamental() +
                avaliacaoComportamento.getNotaAprendizado() +
                avaliacaoComportamento.getNotaTomadaDecisao() +
                avaliacaoComportamento.getNotaAutonomia();

        float mediaComportamental = somaAvaliacaoComportamento / 4.0f;

        List<Entrega> entregas = colaborador.getEntregas();

        // O usuário só pode pedir o cálculo da performance final depois
        // que ele já tiver cadastrado pelo menos 2 entregas.
        if (entregas.size() < 2) {
            throw new RuntimeException("Colaborador deve ter no minimo 2 entregas cadastradas.");
        }

        int somaEntregas = 0;

        for (Entrega entrega : entregas) {
            somaEntregas += entrega.getNota();
        }

        double mediaEntregas = (double) somaEntregas / entregas.size();

        double notaFinal = mediaEntregas + mediaComportamental;

        var mediasDTO = new MediaPerformanceDTO(mediaComportamental, mediaEntregas, notaFinal);

        return new PerformanceDTO(
                colaborador.getMatricula(),
                colaborador.getNome(),
                mediasDTO
        );
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

        List<EntregaRepostaDTO> entregasDTO = colaborador.getEntregas()
                .stream()
                .map(entrega -> new EntregaRepostaDTO(
                        entrega.getId(),
                        entrega.getDescricao(),
                        entrega.getNota()
                ))
                .collect(Collectors.toList());

        return new ColaboradorRespostaDTO(
                colaborador.getMatricula(),
                colaborador.getNome(),
                colaborador.getDataAdmissao(),
                colaborador.getCargo(),
                notas,
                entregasDTO
        );
    }
}
