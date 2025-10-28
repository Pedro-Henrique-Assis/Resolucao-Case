package com.example.demo.colaborador.avaliacao.service;

import com.example.demo.colaborador.avaliacao.resource.json.AvaliacaoComportamentoAtualizaRequest;
import com.example.demo.colaborador.avaliacao.resource.json.AvaliacaoComportamentoCadastroRequest;
import com.example.demo.colaborador.avaliacao.resource.json.AvaliacaoComportamentoResponse;
import com.example.demo.base.exception.NegocioException;
import com.example.demo.base.exception.ResourceNotFoundException;
import com.example.demo.colaborador.avaliacao.model.AvaliacaoComportamentoEntity;
import com.example.demo.colaborador.model.ColaboradorEntity;
import com.example.demo.colaborador.avaliacao.repository.AvaliacaoComportamentoRepository;
import com.example.demo.colaborador.repository.ColaboradorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Service
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

    @Transactional
    public Long cadastrarAvaliacaoComportamental(
            String matricula,
            AvaliacaoComportamentoCadastroRequest avaliacaoComportamentoCadastroRequest) {

        logger.debug("Verificando se o colaborador de matricula '{}' existe", matricula);
        ColaboradorEntity colaboradorEntity = colaboradorRepository.findById(UUID.fromString(matricula))
                .orElseThrow(() -> new ResourceNotFoundException("Colaborador não encontrado."));

        logger.debug("Colaborador de matricula '{}' encontrado.", matricula);

        logger.debug("Atribuindo valores às notas do colaborador");
        //DTO -> Entity
        var avaliacaoComportamento = new AvaliacaoComportamentoEntity();
        avaliacaoComportamento.setNotaAvaliacaoComportamental(avaliacaoComportamentoCadastroRequest.notaAvaliacaoComportamental());
        avaliacaoComportamento.setNotaAprendizado(avaliacaoComportamentoCadastroRequest.notaAprendizado());
        avaliacaoComportamento.setNotaTomadaDecisao(avaliacaoComportamentoCadastroRequest.notaTomadaDecisao());
        avaliacaoComportamento.setNotaAutonomia(avaliacaoComportamentoCadastroRequest.notaAutonomia());
        avaliacaoComportamento.setColaborador(colaboradorEntity);

        var avaliacaoComportamentoSalva = avaliacaoComportamentoRepository.save(avaliacaoComportamento);
        logger.info("Avalização comportamental do colaborador de matricula '{}' cadastrada com sucesso", matricula);

        return avaliacaoComportamentoSalva.getId();
    }

    @Transactional(readOnly = true)
    public AvaliacaoComportamentoResponse consultaAvaliacaoPorMatricula(String matricula) {
        logger.debug("Iniciando consulta de avaliação para a matrícula '{}'", matricula);
        var matriculaUUID = UUID.fromString(matricula);

        var colaborador = colaboradorRepository.findById(matriculaUUID)
                .orElseThrow(() -> new ResourceNotFoundException("Colaborador não encontrado"));

        logger.debug("Colaborador de matricula '{}' encontrado.", matricula);

        AvaliacaoComportamentoEntity avaliacao = colaborador.getAvaliacaoComportamento();

        if (avaliacao == null) {
            throw new ResourceNotFoundException("O Colaborador não possui avaliação comportamental.");
        }

        Long avaliacaoId = avaliacao.getId();
        logger.debug("Avaliação 'id={}' encontrada. Formatando a resposta.", avaliacaoId);

        var respostaDTO = this.formataRespostaDTO(avaliacao);

        logger.info("Consulta de avaliação 'id={}' para a matricula '{}' concluída com sucesso.", avaliacaoId, matricula);
        return respostaDTO;
    }

    @Transactional
    public void atualizaAvaliacaoPorMarticula(
            String matricula,
            AvaliacaoComportamentoAtualizaRequest novasNotas) {

        var matriculaUUID = UUID.fromString(matricula);

        logger.debug("Verificando se o colaborador de matricula '{}' existe", matricula);
        var colaborador = colaboradorRepository.findById(matriculaUUID)
                .orElseThrow(() -> new ResourceNotFoundException("Colaborador não encontrado"));

        logger.debug("Colaborador encontrado. Iniciando atualização das notas");

        var notas = colaborador.getAvaliacaoComportamento();

        // Verifica se o colaborador possui notas cadastradas
        if (notas == null) {
            throw new NegocioException("O colaborador " + matricula + " não possui uma avaliação comportamental para atualizar");
        }

        logger.debug("Avaliação id={} encontrada. Iniciando atualização das notas.", notas.getId());

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

    @Transactional
    public void deletarAvaliacoesPorMatricula(String matricula) {
        var matriculaUUID = UUID.fromString(matricula);

        logger.debug("Iniciando a verificação se o colaborador de matricula '{}' existe", matricula);

        var colaborador = colaboradorRepository.findById(matriculaUUID)
                .orElseThrow(() -> new ResourceNotFoundException("Colaborador não encontrado"));

        logger.debug("Colaborador encontrado. Verificando se ele possui avaliacoes");
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
    }

    private AvaliacaoComportamentoResponse formataRespostaDTO(AvaliacaoComportamentoEntity avaliacao) {

        logger.debug("Iniciando formatação do JSON de resposta da avaliacao id={}", avaliacao.getId());

        logger.debug("Obtendo as notas da avaliacao");
        BigDecimal notaAvaliacaoComportamental = BigDecimal.valueOf(avaliacao.getNotaAvaliacaoComportamental());
        BigDecimal notaAprendizado = BigDecimal.valueOf(avaliacao.getNotaAprendizado());
        BigDecimal notaTomadaDecisao = BigDecimal.valueOf(avaliacao.getNotaTomadaDecisao());
        BigDecimal notaAutonomia = BigDecimal.valueOf(avaliacao.getNotaAutonomia());

        logger.debug("Calculando a media das notas");
        BigDecimal soma = notaAvaliacaoComportamental
                .add(notaAprendizado)
                .add(notaTomadaDecisao)
                .add(notaAutonomia);

        BigDecimal media = soma.divide(new BigDecimal("4"), 2, RoundingMode.HALF_UP);

        logger.debug("Média das notas calculada com sucesso");

        logger.info("Formatacao finalizada com sucesso!");
        return new AvaliacaoComportamentoResponse(
                avaliacao.getNotaAvaliacaoComportamental(),
                avaliacao.getNotaAprendizado(),
                avaliacao.getNotaTomadaDecisao(),
                avaliacao.getNotaAutonomia(),
                media
        );
    }
}
