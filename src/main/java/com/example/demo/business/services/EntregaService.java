package com.example.demo.business.services;

import com.example.demo.controller.dto.AtualizaColaboradorDTO;
import com.example.demo.controller.dto.AtualizaEntregaDTO;
import com.example.demo.controller.dto.CadastroEntregaDTO;
import com.example.demo.controller.dto.EntregaRepostaDTO;
import com.example.demo.infrastructure.exceptions.NegocioException;
import com.example.demo.infrastructure.exceptions.ResourceNotFoundException;
import com.example.demo.infrastructure.model.Colaborador;
import com.example.demo.infrastructure.model.Entrega;
import com.example.demo.infrastructure.repository.ColaboradorRepository;
import com.example.demo.infrastructure.repository.EntregaRepository;
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

    @Transactional
    public Entrega cadastrarEntregaColaborador(String matricula, CadastroEntregaDTO cadastroEntregaDTO) {
        var matriculaUUID = UUID.fromString(matricula);

        logger.debug("Iniciando a verificação se o colaborador de matricula '{}' existe", matricula);

        Colaborador colaborador = colaboradorRepository.findById(matriculaUUID)
                .orElseThrow(() -> new ResourceNotFoundException("Colaborador não encontrado"));

        logger.debug("Colaborador encontrado. Verificando se ele já atingiu o máximo de entregas");

        if (colaborador.getEntregas().size() >= 4) {
            throw new NegocioException("O colaborador já atingiu o limite de 4 entregas cadastradas");
        }

        logger.info("Colaborador não atingiu o máximo de entregas. Adicionando nova entrega.");

        var entrega = new Entrega();
        entrega.setDescricao(cadastroEntregaDTO.descricao());
        entrega.setNota(cadastroEntregaDTO.nota());
        entrega.setColaborador(colaborador);

        return entregaRepository.save(entrega);
    }

    public EntregaRepostaDTO consultarEntregaPorId(String matricula, Long id) {
        var matriculaUUID = UUID.fromString(matricula);

        logger.debug("Iniciando a verificação se o colaborador de matricula '{}' existe", matricula);

        Colaborador colaborador = colaboradorRepository.findById(matriculaUUID)
                .orElseThrow(() -> new ResourceNotFoundException("Colaborador não encontrado"));

        logger.debug("Colaborador encontrado. Verificando a existência da entrega id={}", id);

        var entrega = entregaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("A entrega consultada não existe: " + id));

        // Verifica se a entrega pertence mesmo àquele colaborador
        if (!entrega.getColaborador().getMatricula().equals(matriculaUUID)) {
            throw new NegocioException("Acesso negado: A entrega " + id + " não pertence ao colaborador " + matricula);
        }

        logger.debug("Entrega [id={}] encontrada", id);

        var entregaDTO = new EntregaRepostaDTO(
                entrega.getId(),
                entrega.getDescricao(),
                entrega.getNota()
        );

        logger.info("Consulta da entrega id={} para o colaborador '{}' bem-sucedida.", id, matriculaUUID);
        return entregaDTO;
    }

    public List<EntregaRepostaDTO> listarEntregasPorColaborador(String matricula) {
        var matriculaUUID = UUID.fromString(matricula);

        logger.debug("Iniciando a verificação se o colaborador de matricula '{}' existe", matricula);

        var colaborador = colaboradorRepository.findById(matriculaUUID)
                .orElseThrow(() -> new ResourceNotFoundException("Colaborador não encontrado"));

        logger.debug("Colaborador encontrado. Obtendo sua lista de entregas.");

        List<Entrega> entregas = colaborador.getEntregas();

        logger.info("Lista de entregas formada com sucesso.");
        return entregas.stream()
                .map(entrega -> new EntregaRepostaDTO(
                        entrega.getId(),
                        entrega.getDescricao(),
                        entrega.getNota()
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public void deletarEntregaColaborador(String matricula, Long id) {
        var matriculaUUID = UUID.fromString(matricula);

        logger.debug("Iniciando a verificação se o colaborador de matricula '{}' existe", matricula);

        var colaborador = colaboradorRepository.findById(matriculaUUID)
                .orElseThrow(() -> new ResourceNotFoundException("Colaborador não encontrado"));

        logger.debug("Colaborador encontrado. Verificando a existência da entrega id={}", id);

        var entrega = entregaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("A entrega consultada não existe: " + id));

        // Verifica se a entrega pertence mesmo àquele colaborador (Inibe o risco de apagar entregas de outros colaboradores)
        if (!entrega.getColaborador().getMatricula().equals(colaborador.getMatricula())) {
            throw new NegocioException("Acesso negado: A entrega " + id + " não pertence ao colaborador " + matricula);
        }

        entregaRepository.deleteById(id);
        logger.info("Entrega encontrada e deletada.");
    }

    @Transactional
    public void atualizarEntregaPorId(String matricula, Long id, AtualizaEntregaDTO atualizaEntregaDTO) {
        var matriculaUUID = UUID.fromString(matricula);

        Colaborador colaborador = colaboradorRepository.findById(matriculaUUID)
                .orElseThrow(() -> new RuntimeException("Colaborador não encontrado com a matrícula: " + matricula));

        Entrega entrega = entregaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Entrega não encontrada com o ID: " + id));

        // Verifica se a entrega (id) pertence ao colaborador
        if (!entrega.getColaborador().getMatricula().equals(colaborador.getMatricula())) {
            throw new RuntimeException("Acesso negado: A entrega " + id + " não pertence ao colaborador " + matricula);
        }

        if (atualizaEntregaDTO.descricao() != null) {
            entrega.setDescricao(atualizaEntregaDTO.descricao());
        }

        if (atualizaEntregaDTO.nota() != null) {
            entrega.setNota(atualizaEntregaDTO.nota());
        }

        entregaRepository.save(entrega);
    }
}
