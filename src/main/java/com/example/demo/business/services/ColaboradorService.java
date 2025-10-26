package com.example.demo.business.services;

import com.example.demo.controller.dto.*;
import com.example.demo.infrastructure.exceptions.NegocioException;
import com.example.demo.infrastructure.exceptions.ResourceNotFoundException;
import com.example.demo.infrastructure.model.AvaliacaoComportamento;
import com.example.demo.infrastructure.model.Colaborador;
import com.example.demo.infrastructure.model.Entrega;
import com.example.demo.infrastructure.repository.ColaboradorRepository;
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
    public UUID cadastrarColaborador(CadastroColaboradorDTO cadastroColaboradorDTO) {

        // DTO -> Entity
        var colaborador = new Colaborador();
        colaborador.setNome(cadastroColaboradorDTO.nome());
        colaborador.setDataAdmissao(cadastroColaboradorDTO.dataAdmissao());
        colaborador.setCargo(cadastroColaboradorDTO.cargo());

        var colaboradorSalvo = colaboradorRepository.save(colaborador);

        return colaboradorSalvo.getMatricula();
    }

    @Transactional(readOnly = true)
    public Optional<ColaboradorRespostaDTO> consultarColaboradorPorMatricula(String matricula) {
        return colaboradorRepository
                .findById(UUID.fromString(matricula))
                .map(this::formatarJsonDTO);
    }

    @Transactional(readOnly = true)
    public List<ColaboradorRespostaDTO> listarColaboradores() {
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
    public void atualizaColaboradorPorMatricula(String matricula, AtualizaColaboradorDTO atualizaColaboradorDTO) {
        var matriculaUUID = UUID.fromString(matricula);

        logger.debug("Iniciando a atualização do colaborador de matrícula '{}'", matricula);

        var colaborador = colaboradorRepository.findById(matriculaUUID)
                .orElseThrow(() -> new ResourceNotFoundException("Colaborador não encontrado"));

        logger.debug("Colaborador encontrado. Iniciando atualização das notas");

        if (atualizaColaboradorDTO.nome() != null) {
            colaborador.setNome(atualizaColaboradorDTO.nome());
            logger.debug("Nome atualizado com sucesso");
        }

        if (atualizaColaboradorDTO.cargo() != null) {
            colaborador.setCargo(atualizaColaboradorDTO.cargo());
            logger.debug("Cargo atualizado com sucesso");
        }

        // Realiza a operação de update daquele colaborador específico
        colaboradorRepository.save(colaborador);
        logger.info("Atualização do colaborador finalizada com sucesso");
    }

    public PerformanceDTO calcularPerformanceFinal(String matricula) {
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
        List<Entrega> entregas = colaborador.getEntregas();

        logger.debug("Lista de entregas encontrada.");

        // O usuário só pode pedir o cálculo da performance final depois
        // que ele já tiver cadastrado pelo menos 2 entregas.
        if (entregas.size() < 2) {
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

        for (Entrega entrega : entregas) {
            somaEntregas = somaEntregas.add(BigDecimal.valueOf(entrega.getNota()));
        }

        BigDecimal mediaEntregas = somaEntregas.divide(new BigDecimal(entregas.size()), 2, RoundingMode.HALF_UP);

        logger.debug("Média de notas das entregas calculada com sucesso");

        // Sem peso em cada média por regra de negócio
        BigDecimal notaFinal = mediaEntregas.add(mediaComportamental);

        var mediasDTO = new MediaPerformanceDTO(mediaComportamental, mediaEntregas, notaFinal);

        logger.info("Nota final de performance calculada com sucesso");

        return new PerformanceDTO(
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
    private ColaboradorRespostaDTO formatarJsonDTO(Colaborador colaborador) {
        logger.debug("Iniciando a montagem do JSON de resposta para a Controller");

        DetalhesAvaliacaoComportamentoDTO notas = null;

        logger.debug("Buscando avaliações do colaborador");
        AvaliacaoComportamento avaliacao = colaborador.getAvaliacaoComportamento();

        logger.debug("Avaliações encontradas com sucesso");

        if (avaliacao != null) {
            BigDecimal soma = BigDecimal.valueOf(avaliacao.getNotaAvaliacaoComportamental())
                    .add(BigDecimal.valueOf(avaliacao.getNotaAprendizado()))
                    .add(BigDecimal.valueOf(avaliacao.getNotaTomadaDecisao()))
                    .add(BigDecimal.valueOf(avaliacao.getNotaAutonomia()));

            BigDecimal media = soma.divide(new BigDecimal("4"), 2, RoundingMode.HALF_UP);

            logger.debug("Média das avaliações calculada com sucesso");

            notas = new DetalhesAvaliacaoComportamentoDTO(
                    avaliacao.getNotaAvaliacaoComportamental(),
                    avaliacao.getNotaAprendizado(),
                    avaliacao.getNotaTomadaDecisao(),
                    avaliacao.getNotaAutonomia(),
                    media
            );
            logger.debug("DTO de resposta da avaliação comportamental montado com sucesso");
        }

        logger.debug("Buscando entregas do colaborador");
        List<EntregaRepostaDTO> entregasDTO = colaborador.getEntregas()
                .stream()
                .map(entrega -> new EntregaRepostaDTO(
                        entrega.getId(),
                        entrega.getDescricao(),
                        entrega.getNota()
                ))
                .collect(Collectors.toList());

        logger.debug("Entregas encontradas com sucesso");

        logger.info("Avaliações e entregas encontradas com sucesso. Formatando DTO de resposta do colaborador");
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
