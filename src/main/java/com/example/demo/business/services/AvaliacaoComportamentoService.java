package com.example.demo.business.services;

import com.example.demo.controller.dto.AtualizaAvaliacaoComportamentoDTO;
import com.example.demo.controller.dto.AvaliacaoComportamentoDTO;
import com.example.demo.controller.dto.AvaliacaoComportamentoRespostaDTO;
import com.example.demo.infrastructure.model.AvaliacaoComportamento;
import com.example.demo.infrastructure.model.Colaborador;
import com.example.demo.infrastructure.repository.AvaliacaoComportamentoRepository;
import com.example.demo.infrastructure.repository.ColaboradorRepository;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class AvaliacaoComportamentoService {

    private final AvaliacaoComportamentoRepository avaliacaoComportamentoRepository;

    private final ColaboradorRepository colaboradorRepository;

    private static final Logger logger = LoggerFactory.getLogger(AvaliacaoComportamentoService.class);

    public AvaliacaoComportamentoService(
            AvaliacaoComportamentoRepository avaliacaoComportamentoRepository,
            ColaboradorRepository colaboradorRepository) {

        this.avaliacaoComportamentoRepository = avaliacaoComportamentoRepository;
        this.colaboradorRepository = colaboradorRepository;
    }

    public Optional<Long> cadastrarAvaliacaoComportamental(
            String matricula,
            AvaliacaoComportamentoDTO avaliacaoComportamentoDTO) {

        logger.debug("Verificando se o colaborador de matricula '{}' existe", matricula);
        Optional<Colaborador> colaboradorOpcional = colaboradorRepository.findById(UUID.fromString(matricula));

        if (colaboradorOpcional.isEmpty()) {
            logger.warn("Colaborador de matricula '{}' não encontrado.", matricula);
            return Optional.empty();
        }

        var colaborador = colaboradorOpcional.get();
        logger.debug("Colaborador de matricula '{}' encontrado.", matricula);

        logger.debug("Atribuindo valores às notas do colaborador");
        //DTO -> Entity
        var avaliacaoComportamento = new AvaliacaoComportamento();
        avaliacaoComportamento.setNotaAvaliacaoComportamental(avaliacaoComportamentoDTO.notaAvaliacaoComportamental());
        avaliacaoComportamento.setNotaAprendizado(avaliacaoComportamentoDTO.notaAprendizado());
        avaliacaoComportamento.setNotaTomadaDecisao(avaliacaoComportamentoDTO.notaTomadaDecisao());
        avaliacaoComportamento.setNotaAutonomia(avaliacaoComportamentoDTO.notaAutonomia());
        avaliacaoComportamento.setColaborador(colaborador);

        var avaliacaoComportamentoSalva = avaliacaoComportamentoRepository.save(avaliacaoComportamento);
        logger.info("Avalização comportamental do colaborador de matricula '{}' cadastrada com sucesso", matricula);

        return Optional.of(avaliacaoComportamentoSalva.getId());
    }

    public Optional<AvaliacaoComportamentoRespostaDTO> consultaAvaliacaoPorMatricula(String matricula) {
        logger.debug("Iniciando consulta de avaliação para a matrícula '{}'", matricula);
        var matriculaUUID = UUID.fromString(matricula);

        var colaboradorOpcional = colaboradorRepository.findById(matriculaUUID);

        if (colaboradorOpcional.isEmpty()) {
            logger.warn("Colaborador de matricula '{}' não encontrado.", matricula);
            return Optional.empty();
        }

        Colaborador colaborador = colaboradorOpcional.get();
        logger.debug("Colaborador de matricula '{}' encontrado.", matricula);

        AvaliacaoComportamento avaliacao = colaborador.getAvaliacaoComportamento();

        if (avaliacao == null) {
            logger.info("Colaborador encontrado, mas não possui avaliação comportamental associada.");
            return Optional.empty();
        }

        Long avaliacaoId = avaliacao.getId();
        logger.debug("Avaliação 'id={}' encontrada. Formatando a resposta...", avaliacaoId);

        var respostaDTO = this.formataRespostaDTO(avaliacao);

        logger.info("Consulta de avaliação 'id={}' para a matricula '{}' concluída com sucesso.", avaliacaoId, matricula);
        return Optional.of(respostaDTO);
    }

    public void atualizaAvaliacaoPorMarticula(
            String matricula,
            AtualizaAvaliacaoComportamentoDTO novasNotas) {

        var matriculaUUID = UUID.fromString(matricula);

        logger.debug("Verificando se o colaborador de matricula '{}' existe", matricula);
        var colaboradorEntity = colaboradorRepository.findById(matriculaUUID);

        if (colaboradorEntity.isPresent()) {
            var colaborador = colaboradorEntity.get();

            logger.debug("Colaborador encontrado. Iniciando atualização das notas.");

            var notas = colaborador.getAvaliacaoComportamento();

            if (novasNotas.notaAvaliacaoComportamental() != null) {
                notas.setNotaAvaliacaoComportamental(novasNotas.notaAvaliacaoComportamental());
                logger.debug("Nota da avaliação comportamental atualizada");
            }

            if (novasNotas.notaAprendizado() != null) {
                notas.setNotaAprendizado(novasNotas.notaAprendizado());
                logger.debug("Nota da avaliação de aprendizado atualizada");
            }

            if (novasNotas.notaTomadaDecisao() != null) {
                notas.setNotaTomadaDecisao(novasNotas.notaTomadaDecisao());
                logger.debug("Nota da avaliação de tomada de decisao atualizada");
            }

            if (novasNotas.notaAutonomia() != null) {
                notas.setNotaAutonomia(novasNotas.notaAutonomia());
                logger.debug("Nota da avaliação de autonomia atualizada");
            }

            logger.info("Notas atualizadas com sucesso");
            avaliacaoComportamentoRepository.save(notas);
        }

    }

    public void deletarAvaliacoesPorMatricula(String matricula) {
        var matriculaUUID = UUID.fromString(matricula);

        logger.debug("Iniciando a verificação se o colaborador de matricula '{}' existe", matricula);

        var colaboradorOpcional = colaboradorRepository.findById(matriculaUUID);

        if (colaboradorOpcional.isPresent()) {
            logger.debug("Colaborador encontrado. Excluindo a avaliacao");

            var colaborador = colaboradorOpcional.get();

            logger.debug("Verificando se o colaborador possui avaliacoes");
            var avaliacao = colaborador.getAvaliacaoComportamento();

            if (avaliacao != null) {
                // Como o relacionamento possui orphanRemoval = true,
                // ao colocar valor null na avaliacao de comportamento, ela será automaticamente apagada
                colaborador.setAvaliacaoComportamento(null);
                colaboradorRepository.save(colaborador);
                logger.info("Avaliacao encontrada e deletada");
            } else {
                logger.warn("Avaliacao do colaborador nao encontrada");
            }
        } else {
            logger.warn("Colaborador de matrícula '{}' não encontrado", matricula);
        }
    }

    private AvaliacaoComportamentoRespostaDTO formataRespostaDTO(AvaliacaoComportamento avaliacao) {

        logger.debug("Iniciando formatação do JSON de resposta da avaliacao id={}", avaliacao.getId());

        logger.debug("Obtendo as notas da avaliacao");
        Byte notaAvaliacaoComportamental = avaliacao.getNotaAvaliacaoComportamental();
        Byte notaAprendizado = avaliacao.getNotaAprendizado();
        Byte notaTomadaDecisao = avaliacao.getNotaTomadaDecisao();
        Byte notaAutonomia = avaliacao.getNotaAutonomia();

        logger.debug("Calculando a media das notas");
        int soma = notaAvaliacaoComportamental + notaAprendizado + notaTomadaDecisao + notaAutonomia;
        float media = soma / 4.0f;

        var novasNotasDTO = new AvaliacaoComportamentoDTO(
                avaliacao.getNotaAvaliacaoComportamental(),
                avaliacao.getNotaAprendizado(),
                avaliacao.getNotaTomadaDecisao(),
                avaliacao.getNotaAutonomia(),
                media
        );

        logger.info("Formatacao finalizada com sucesso!");
        return new AvaliacaoComportamentoRespostaDTO(novasNotasDTO);
    }
}
