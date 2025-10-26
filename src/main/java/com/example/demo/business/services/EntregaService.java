package com.example.demo.business.services;

import com.example.demo.controller.dto.CadastroEntregaDTO;
import com.example.demo.controller.dto.EntregaRepostaDTO;
import com.example.demo.infrastructure.model.Colaborador;
import com.example.demo.infrastructure.model.Entrega;
import com.example.demo.infrastructure.repository.ColaboradorRepository;
import com.example.demo.infrastructure.repository.EntregaRepository;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class EntregaService {

    private final ColaboradorRepository colaboradorRepository;

    private final EntregaRepository entregaRepository;

    private static Logger logger = LoggerFactory.getLogger(EntregaService.class);

    public EntregaService(ColaboradorRepository colaboradorRepository, EntregaRepository entregaRepository) {
        this.colaboradorRepository = colaboradorRepository;
        this.entregaRepository = entregaRepository;
    }

    public Entrega cadastrarEntregaColaborador(String matricula, CadastroEntregaDTO cadastroEntregaDTO) {
        var matriculaUUID = UUID.fromString(matricula);

        logger.debug("Iniciando a verificação se o colaborador de matricula '{}' existe", matricula);

        Colaborador colaborador = colaboradorRepository.findById(matriculaUUID)
                .orElseThrow(() -> new RuntimeException("Colaborador não encontrado com a matrícula: " + matricula));

        logger.debug("Colaborador encontrado. Verificando se ele já atingiu o máximo de entregas");

        if (colaborador.getEntregas().size() >= 4) {
            throw new RuntimeException("O colaborador já atingiu o limite de 4 entregas cadastradas");
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

        boolean colaboradorExiste = colaboradorRepository.existsById(matriculaUUID);

        if (colaboradorExiste) {
            logger.debug("Colaborador encontrado. Verificando a existência da entrega id={}", id);

            var entregaOpcional = entregaRepository.findById(id);

            if (entregaOpcional.isPresent()) {
                logger.debug("Entrega [id={}] encontrada", id);

                var entrega = entregaOpcional.get();

                var entregaDTO = new EntregaRepostaDTO(
                        entrega.getId(),
                        entrega.getDescricao(),
                        entrega.getNota()
                );

                logger.info("Consulta da entrega id={} para o colaborador '{}' bem-sucedida.", id, matriculaUUID);
                return entregaDTO;
            } else {
                logger.warn("Entrega id={} não encontrada", id);
                throw new RuntimeException("A entrega consultada não existe");
            }
        } else {
            logger.warn("Colaborador de matrícula '{}' não encontrado", matricula);
            throw new RuntimeException("O colaborador consultado não existe");
        }
    }

    public List<EntregaRepostaDTO> listarEntregasPorColaborador(String matricula) {
        var matriculaUUID = UUID.fromString(matricula);

        logger.debug("Iniciando a verificação se o colaborador de matricula '{}' existe", matricula);

        var colaborador = colaboradorRepository.findById(matriculaUUID)
                .orElseThrow(() -> new RuntimeException("Colaborador não encontrado"));

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

    public void deletarEntregaColaborador(String matricula, Long id) {
        var matriculaUUID = UUID.fromString(matricula);

        logger.debug("Iniciando a verificação se o colaborador de matricula '{}' existe", matricula);

        boolean colaboradorExiste = colaboradorRepository.existsById(matriculaUUID);

        if (colaboradorExiste) {
            logger.debug("Colaborador encontrado. Verificando a existência da entrega id={}", id);

            boolean entregaExiste = entregaRepository.existsById(id);

            if (entregaExiste) {
                entregaRepository.deleteById(id);
                logger.info("Entrega encontrada e deletada.");
            } else {
                logger.warn("Entrega id={} não encontrada", id);
            }
        } else {
            logger.warn("Colaborador de matrícula '{}' não encontrado", matricula);
        }
    }
}
