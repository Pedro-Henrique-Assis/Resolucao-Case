package com.example.demo.colaborador.service;

import com.example.demo.colaborador.avaliacao.resource.json.AvaliacaoComportamentoResponse;
import com.example.demo.colaborador.entrega.resource.json.EntregaResponse;
import com.example.demo.colaborador.resource.json.*;
import com.example.demo.base.exception.NegocioException;
import com.example.demo.base.exception.ResourceNotFoundException;
import com.example.demo.colaborador.avaliacao.model.AvaliacaoComportamentoEntity;
import com.example.demo.colaborador.model.ColaboradorEntity;
import com.example.demo.colaborador.entrega.model.EntregaEntity;
import com.example.demo.colaborador.repository.ColaboradorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ColaboradorService {

    private final ColaboradorRepository colaboradorRepository;

    private static final Logger logger = LoggerFactory.getLogger(ColaboradorService.class);

    //Injeção de dependência da classe ColaboradorRepository
    public ColaboradorService(ColaboradorRepository colaboradorRepository) {
        this.colaboradorRepository = colaboradorRepository;
    }

    @Transactional
    public UUID cadastrarColaborador(ColaboradorCadastroRequest colaboradorCadastroRequest) {

        // DTO -> Entity
        var colaborador = new ColaboradorEntity();
        colaborador.setNome(colaboradorCadastroRequest.nome());
        colaborador.setDataAdmissao(colaboradorCadastroRequest.dataAdmissao());
        colaborador.setCargo(colaboradorCadastroRequest.cargo());

        var colaboradorSalvo = colaboradorRepository.save(colaborador);

        return colaboradorSalvo.getMatricula();
    }

    @Transactional(readOnly = true)
    public Optional<ColaboradorResponse> consultarColaboradorPorMatricula(String matricula) {
        return colaboradorRepository
                .findById(UUID.fromString(matricula))
                .map(this::formatarJsonDTO);
    }

    @Transactional(readOnly = true)
    public List<ColaboradorResponse> listarColaboradores() {
        return colaboradorRepository.findAll().stream().map(this::formatarJsonDTO).collect(Collectors.toList());
    }

    @Transactional
    public void deletarColaboradorPorMatricula(String matricula) {
        var matriculaUUID = UUID.fromString(matricula);
        logger.debug("Tentando deletar colaborador [matricula={}]", matriculaUUID);

        var colaborador = colaboradorRepository.findById(matriculaUUID)
                .orElseThrow(() -> new ResourceNotFoundException("Colaborador não encontrado"));

        colaboradorRepository.delete(colaborador);
        logger.info("Colaborador deletado com sucesso");
    }

    @Transactional
    public void atualizaColaboradorPorMatricula(String matricula, ColaboradorAtualizaRequest colaboradorAtualizaRequest) {
        var matriculaUUID = UUID.fromString(matricula);

        logger.debug("Iniciando a atualização do colaborador de matrícula '{}'", matricula);

        var colaborador = colaboradorRepository.findById(matriculaUUID)
                .orElseThrow(() -> new ResourceNotFoundException("Colaborador não encontrado"));

        logger.debug("Colaborador encontrado. Iniciando atualização das notas");

        if (colaboradorAtualizaRequest.nome() != null) {
            colaborador.setNome(colaboradorAtualizaRequest.nome());
            logger.debug("Nome atualizado com sucesso");
        }

        if (colaboradorAtualizaRequest.dataAdmissao() != null) {
            colaborador.setDataAdmissao(colaboradorAtualizaRequest.dataAdmissao());
            logger.debug("Data de admissão atualizada com sucesso");
        }

        if (colaboradorAtualizaRequest.cargo() != null) {
            colaborador.setCargo(colaboradorAtualizaRequest.cargo());
            logger.debug("Cargo atualizado com sucesso");
        }

        // Realiza a operação de update daquele colaborador específico
        colaboradorRepository.save(colaborador);
        logger.info("Atualização do colaborador finalizada com sucesso");
    }

    public ColaboradorPerformanceResponse calcularPerformanceFinal(String matricula) {
        var matriculaUUID = UUID.fromString(matricula);

        logger.debug("Iniciando o cálculo de performance final do colaborador de matrícula '{}'", matricula);

        var colaborador = colaboradorRepository.findById(matriculaUUID)
                .orElseThrow(() -> new ResourceNotFoundException("Colaborador não encontrado"));

        logger.debug("Colaborador encontrado. Buscando avaliação.");

        var avaliacaoComportamento = colaborador.getAvaliacaoComportamento();

        if (avaliacaoComportamento == null) {
            logger.warn("Avaliação comportamental não encontrada");
            throw new NegocioException("Avaliação comportamental não foi realizada.");
        }

        logger.debug("Avaliação encontrada.");


        logger.debug("Obtendo lista de entregas do colaborador");
        List<EntregaEntity> entregases = colaborador.getEntregas();

        logger.debug("Lista de entregas encontrada.");

        // O usuário só pode pedir o cálculo da performance final depois
        // que ele já tiver cadastrado pelo menos 2 entregas.
        if (entregases.size() < 2) {
            logger.warn("O colaborador possui menos de 2 entregas. O cálculo não será possível");
            throw new NegocioException("Colaborador deve ter no minimo 2 entregas cadastradas.");
        }

        BigDecimal somaAvaliacaoComportamento = BigDecimal.valueOf(avaliacaoComportamento.getNotaAvaliacaoComportamental())
                        .add(BigDecimal.valueOf(avaliacaoComportamento.getNotaAprendizado()))
                        .add(BigDecimal.valueOf(avaliacaoComportamento.getNotaTomadaDecisao()))
                        .add(BigDecimal.valueOf(avaliacaoComportamento.getNotaAutonomia()));

        // Fórmula (n1 + n2 + n3 + n4) / 4 [Regra de negócio sem peso na notas]
        BigDecimal mediaComportamental = somaAvaliacaoComportamento.divide(new BigDecimal("4"), 2, RoundingMode.HALF_UP);

        logger.debug("Média de notas comportamentais calculada com sucesso");

        BigDecimal somaEntregas = BigDecimal.ZERO;

        for (EntregaEntity entregaEntity : entregases) {
            somaEntregas = somaEntregas.add(BigDecimal.valueOf(entregaEntity.getNota()));
        }

        BigDecimal mediaEntregas = somaEntregas.divide(new BigDecimal(entregases.size()), 2, RoundingMode.HALF_UP);

        logger.debug("Média de notas das entregas calculada com sucesso");

        // Sem peso em cada média por regra de negócio
        BigDecimal notaFinal = mediaEntregas.add(mediaComportamental);

        var mediasDTO = new ColaboradorMediaPerformanceResponse(mediaComportamental, mediaEntregas, notaFinal);

        logger.info("Nota final de performance calculada com sucesso");

        return new ColaboradorPerformanceResponse(
                colaborador.getMatricula(),
                colaborador.getNome(),
                mediasDTO
        );
    }

    // Método que formata o JSON de resposta gerado pela consulta de colaboradores
    // Objetivo: não gerar um loop infinito de informações aninhadas ao retornar as avaliações e entregas
    // + tornar o JSON mais agradável e legível
    // Parametros: objeto do tipo Colaborador cadastrado no banco de dados
    // Retorno: objeto do tipo ColaboradorRespostaDTO utilizado pela ResponseEntity na classe ColaboradorController
    private ColaboradorResponse formatarJsonDTO(ColaboradorEntity colaboradorEntity) {
        logger.debug("Iniciando a montagem do JSON de resposta para a Controller");

        AvaliacaoComportamentoResponse notas = null;

        logger.debug("Buscando avaliações do colaborador");
        AvaliacaoComportamentoEntity avaliacao = colaboradorEntity.getAvaliacaoComportamento();

        logger.debug("Avaliações encontradas com sucesso");

        if (avaliacao != null) {
            BigDecimal soma = BigDecimal.valueOf(avaliacao.getNotaAvaliacaoComportamental())
                    .add(BigDecimal.valueOf(avaliacao.getNotaAprendizado()))
                    .add(BigDecimal.valueOf(avaliacao.getNotaTomadaDecisao()))
                    .add(BigDecimal.valueOf(avaliacao.getNotaAutonomia()));

            BigDecimal media = soma.divide(new BigDecimal("4"), 2, RoundingMode.HALF_UP);

            logger.debug("Média das avaliações calculada com sucesso");

            notas = new AvaliacaoComportamentoResponse(
                    avaliacao.getNotaAvaliacaoComportamental(),
                    avaliacao.getNotaAprendizado(),
                    avaliacao.getNotaTomadaDecisao(),
                    avaliacao.getNotaAutonomia(),
                    media
            );
            logger.debug("DTO de resposta da avaliação comportamental montado com sucesso");
        }

        logger.debug("Buscando entregas do colaborador");
        List<EntregaResponse> entregasDTO = colaboradorEntity.getEntregas()
                .stream()
                .map(entrega -> new EntregaResponse(
                        entrega.getId(),
                        entrega.getDescricao(),
                        entrega.getNota()
                ))
                .collect(Collectors.toList());

        logger.debug("Entregas encontradas com sucesso");

        logger.info("Avaliações e entregas encontradas com sucesso. Formatando DTO de resposta do colaborador");
        return new ColaboradorResponse(
                colaboradorEntity.getMatricula(),
                colaboradorEntity.getNome(),
                colaboradorEntity.getDataAdmissao(),
                colaboradorEntity.getCargo(),
                notas,
                entregasDTO
        );
    }
}
