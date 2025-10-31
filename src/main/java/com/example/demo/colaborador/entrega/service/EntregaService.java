package com.example.demo.colaborador.entrega.service;

import com.example.demo.colaborador.entrega.resource.json.EntregaAtualizaRequest;
import com.example.demo.colaborador.entrega.resource.json.EntregaCadastroRequest;
import com.example.demo.colaborador.entrega.resource.json.EntregaResponse;
import com.example.demo.base.exception.NegocioException;
import com.example.demo.base.exception.ResourceNotFoundException;
import com.example.demo.colaborador.model.ColaboradorEntity;
import com.example.demo.colaborador.entrega.model.EntregaEntity;
import com.example.demo.colaborador.repository.ColaboradorRepository;
import com.example.demo.colaborador.entrega.repository.EntregaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class EntregaService {

    private final ColaboradorRepository colaboradorRepository;

    private final EntregaRepository entregaRepository;

    private static final Logger logger = LoggerFactory.getLogger(EntregaService.class);

    public EntregaService(ColaboradorRepository colaboradorRepository, EntregaRepository entregaRepository) {
        this.colaboradorRepository = colaboradorRepository;
        this.entregaRepository = entregaRepository;
    }

    // Método que cadastra uma nova entrega para um colaborador
    // Objetivo: cadastrar a entrega, validando o limite de 4 entregas por colaborador
    // Parâmetros: matrícula (String) do colaborador e DTO (EntregaCadastroRequest) com descrição e nota
    // Resposta: A entidade (EntregaEntity) da entrega que foi salva no banco
    @Transactional
    public EntregaEntity cadastrarEntregaColaborador(String matricula, EntregaCadastroRequest entregaCadastroRequest) {
        var matriculaUUID = UUID.fromString(matricula);

        logger.debug("Iniciando a verificação se o colaborador de matricula '{}' existe", matricula);

        ColaboradorEntity colaboradorEntity = colaboradorRepository.findById(matriculaUUID)
                .orElseThrow(() -> new ResourceNotFoundException("Colaborador não encontrado"));

        logger.debug("Colaborador encontrado. Verificando se ele já atingiu o máximo de entregas");

        if (colaboradorEntity.getEntregas().size() >= 4) {
            throw new NegocioException("O colaborador já atingiu o limite de 4 entregas cadastradas");
        }

        logger.info("Colaborador não atingiu o máximo de entregas. Adicionando nova entrega.");

        var entrega = new EntregaEntity();
        entrega.setDescricao(entregaCadastroRequest.descricao());
        entrega.setNota(entregaCadastroRequest.nota());
        entrega.setColaborador(colaboradorEntity);

        return entregaRepository.save(entrega);
    }

    // Método que consulta uma entrega específica pelo seu ID
    // Objetivo: consultar uma entrega e validar se ela pertence ao colaborador informado
    // Parâmetros: matrícula (String) do colaborador e id (Long) da entrega
    // Resposta: DTO (EntregaResponse) com os dados da entrega encontrada
    @Transactional(readOnly = true)
    public EntregaResponse consultarEntregaPorId(String matricula, Long id) {
        var matriculaUUID = UUID.fromString(matricula);

        logger.debug("Iniciando a verificação se o colaborador de matricula '{}' existe", matricula);

        ColaboradorEntity colaboradorEntity = colaboradorRepository.findById(matriculaUUID)
                .orElseThrow(() -> new ResourceNotFoundException("Colaborador não encontrado"));

        logger.debug("Colaborador encontrado. Verificando a existência da entrega id={}", id);

        var entrega = entregaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("A entrega consultada não existe: " + id));

        // Verifica se a entrega pertence mesmo àquele colaborador
        if (!entrega.getColaborador().getMatricula().equals(matriculaUUID)) {
            throw new NegocioException("Acesso negado: A entrega " + id + " não pertence ao colaborador " + matricula);
        }

        logger.debug("Entrega [id={}] encontrada", id);

        var entregaDTO = new EntregaResponse(
                entrega.getId(),
                entrega.getDescricao(),
                entrega.getNota()
        );

        logger.info("Consulta da entrega id={} para o colaborador '{}' bem-sucedida.", id, matriculaUUID);
        return entregaDTO;
    }

    // Método que lista todas as entregas de um colaborador específico
    // Objetivo: listar todas as entregas vinculadas a uma matrícula
    // Parâmetros: matrícula (String) do colaborador
    // Resposta: Lista de DTOs (List<EntregaResponse>) com as entregas
    @Transactional(readOnly = true)
    public List<EntregaResponse> listarEntregasPorColaborador(String matricula) {
        var matriculaUUID = UUID.fromString(matricula);

        logger.debug("Iniciando a verificação se o colaborador de matricula '{}' existe", matricula);

        var colaborador = colaboradorRepository.findById(matriculaUUID)
                .orElseThrow(() -> new ResourceNotFoundException("Colaborador não encontrado"));

        logger.debug("Colaborador encontrado. Obtendo sua lista de entregas.");

        List<EntregaEntity> entregases = colaborador.getEntregas();

        logger.info("Lista de entregas formada com sucesso.");
        return entregases.stream()
                .map(entrega -> new EntregaResponse(
                        entrega.getId(),
                        entrega.getDescricao(),
                        entrega.getNota()
                ))
                .collect(Collectors.toList());
    }

    // Método que exclui uma entrega específica
    // Objetivo: excluir uma entrega, validando se ela pertence ao colaborador informado
    // Parâmetros: matrícula (String) do colaborador e id (Long) da entrega a ser excluída
    // Resposta: void (apenas remove a entrega do banco de dados)
    @Transactional
    public void deletarEntregaColaborador(String matricula, Long id) {
        var matriculaUUID = UUID.fromString(matricula);

        logger.debug("Iniciando a verificação se o colaborador de matricula '{}' existe", matricula);

        var colaborador = colaboradorRepository.findById(matriculaUUID)
                .orElseThrow(() -> new ResourceNotFoundException("Colaborador não encontrado"));

        logger.debug("Colaborador encontrado. Verificando a existência da entrega id={}", id);

        var entrega = entregaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("A entrega consultada não existe: " + id));

        logger.debug("Entrega encontrada. Verificando se a entrega pertence ao colaborador");

        // Verifica se a entrega pertence mesmo àquele colaborador (Inibe o risco de apagar entregas de outros colaboradores)
        if (!entrega.getColaborador().getMatricula().equals(colaborador.getMatricula())) {
            throw new NegocioException("Acesso negado: A entrega " + id + " não pertence ao colaborador " + matricula);
        }

        logger.debug("Validações finalizadas. Iniciando a exclusão da entrega 'id={}'", id);

        entregaRepository.deleteById(id);
        logger.info("Entrega deletada com sucesso.");
    }

    // Método que atualiza uma entrega (parcial ou total)
    // Objetivo: atualizar descrição e/ou nota de uma entrega, validando a posse
    // Parâmetros: matrícula (String), id (Long) da entrega e DTO (EntregaAtualizaRequest) com os dados
    // Resposta: void (apenas salva as alterações no banco de dados)
    @Transactional
    public void atualizarEntregaPorId(String matricula, Long id, EntregaAtualizaRequest entregaAtualizaRequest) {
        var matriculaUUID = UUID.fromString(matricula);

        logger.debug("Iniciando a verificação se o colaborador de matricula '{}' existe", matricula);

        ColaboradorEntity colaboradorEntity = colaboradorRepository.findById(matriculaUUID)
                .orElseThrow(() -> new RuntimeException("Colaborador não encontrado com a matrícula: " + matricula));

        logger.debug("Colaborador encontrado. Verificando a existência da entrega id={}", id);
        EntregaEntity entregaEntity = entregaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Entrega não encontrada com o ID: " + id));

        logger.debug("Entrega encontrada. Verificando se a entrega pertence ao colaborador");

        // Verifica se a entrega (id) pertence ao colaborador
        if (!entregaEntity.getColaborador().getMatricula().equals(colaboradorEntity.getMatricula())) {
            throw new RuntimeException("Acesso negado: A entrega " + id + " não pertence ao colaborador " + matricula);
        }

        logger.debug("Validações finalizadas. Iniciando o processo de atualização das informações");

        if (entregaAtualizaRequest.descricao() != null) {
            entregaEntity.setDescricao(entregaAtualizaRequest.descricao());
            logger.debug("Descrição atualizada com sucesso");
        }

        if (entregaAtualizaRequest.nota() != null) {
            entregaEntity.setNota(entregaAtualizaRequest.nota());
            logger.debug("Nota atualizada com sucesso");
        }

        entregaRepository.save(entregaEntity);
        logger.info("Colaborador atualizado com sucesso");
    }
}
