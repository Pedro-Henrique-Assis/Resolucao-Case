package com.example.demo.colaborador.entrega.service;

import com.example.demo.colaborador.entrega.service.EntregaService;
import com.example.demo.colaborador.entrega.resource.json.EntregaAtualizaRequest;
import com.example.demo.colaborador.entrega.resource.json.EntregaCadastroRequest;
import com.example.demo.colaborador.entrega.resource.json.EntregaResponse;
import com.example.demo.base.exception.NegocioException;
import com.example.demo.base.exception.ResourceNotFoundException;
import com.example.demo.colaborador.model.ColaboradorEntity;
import com.example.demo.colaborador.entrega.model.EntregaEntity;
import com.example.demo.colaborador.repository.ColaboradorRepository;
import com.example.demo.colaborador.entrega.repository.EntregaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EntregaServiceTest {

    @Mock
    private ColaboradorRepository colaboradorRepository;

    @Mock
    private EntregaRepository entregaRepository;

    @InjectMocks
    private EntregaService entregaService;

    @Nested
    class cadastrarEntregaColaboradorEntityEntity {

        @Test
        @DisplayName("Deve cadastrar entrega quando colaborador existir e tiver menos de 4 entregas")
        void deveCadastrarEntregaComSucesso() {
            // Arrange
            UUID matricula = UUID.randomUUID();

            // Simula um colaborador encontrado e vindo do banco de dados
            var colaborador = new ColaboradorEntity(matricula, "Alice", LocalDate.of(2024,1,1), "Engenheira");

            // Lista de entregas atual (< 4) para permitir novo cadastro
            colaborador.setEntregas(new ArrayList<>(List.of(new EntregaEntity(), new EntregaEntity(), new EntregaEntity())));

            // Configura o Mock para retornar um colaborador (ou vazio) ao executar o método findById()
            when(colaboradorRepository.findById(matricula)).thenReturn(Optional.of(colaborador));

            // Cria o DTO simulando o input do usuário
            var dto = new EntregaCadastroRequest("Relatório Q1", 4.5);

            // Verifica qual objeto foi passado para o método save() do repositório de entregas
            ArgumentCaptor<EntregaEntity> entregaPassadaSave = ArgumentCaptor.forClass(EntregaEntity.class);

            // Configura o Mock para retornar a entrega salva com ID
            var salvo = new EntregaEntity();
            salvo.setId(10L);
            salvo.setDescricao("Relatório Q1");
            salvo.setNota(4.5);
            salvo.setColaborador(colaborador);
            when(entregaRepository.save(any(EntregaEntity.class))).thenReturn(salvo);

            // Act
            // Executa o método cadastrarEntregaColaborador() para, de fato, testá-lo
            EntregaEntity retorno = entregaService.cadastrarEntregaColaborador(matricula.toString(), dto);

            // Assert
            // Verifica se o método findById() foi chamado exatamente uma vez
            verify(colaboradorRepository).findById(matricula);

            // Verifica se o método save() foi chamado exatamente uma vez e captura o argumento
            verify(entregaRepository).save(entregaPassadaSave.capture());

            // Verifica se a entrega montada possui os dados corretos e associação com o colaborador
            EntregaEntity capturada = entregaPassadaSave.getValue();
            assertEquals("Relatório Q1", capturada.getDescricao());
            assertEquals(4.5, capturada.getNota());
            assertEquals(colaborador, capturada.getColaborador());

            // Verifica retorno
            assertEquals(10L, retorno.getId());
            assertEquals("Relatório Q1", retorno.getDescricao());
            assertEquals(4.5, retorno.getNota());
        }

        @Test
        @DisplayName("Deve lançar ResourceNotFoundException quando colaborador não existir")
        void deveLancarResourceNotFoundQuandoColaboradorNaoExistir() {
            // Arrange
            UUID matricula = UUID.randomUUID();

            // Configura o Mock para retornar vazio ao executar o método findById()
            when(colaboradorRepository.findById(matricula)).thenReturn(Optional.empty());

            // Cria o DTO simulando o input do usuário
            var dto = new EntregaCadastroRequest("Doc", 3.0);

            // Act + Assert
            // Tenta cadastrar para matrícula inexistente e deve lançar ResourceNotFoundException
            assertThrows(
                    ResourceNotFoundException.class,
                    () -> entregaService.cadastrarEntregaColaborador(matricula.toString(), dto)
            );

            // Verifica que save() não foi chamado
            verify(entregaRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar NegocioException quando colaborador já tiver 4 entregas")
        void deveLancarNegocioQuandoLimiteUltrapassado() {
            // Arrange
            UUID matricula = UUID.randomUUID();

            // Simula colaborador com 4 entregas (limite atingido)
            var colaborador = new ColaboradorEntity(matricula, "Bob", LocalDate.of(2024,2,2), "Analista");
            colaborador.setEntregas(new ArrayList<>(List.of(new EntregaEntity(), new EntregaEntity(), new EntregaEntity(), new EntregaEntity())));

            when(colaboradorRepository.findById(matricula)).thenReturn(Optional.of(colaborador));

            var dto = new EntregaCadastroRequest("Nova", 4.0);

            // Act + Assert
            // Tenta cadastrar a 5ª entrega e deve lançar NegocioException
            NegocioException ex = assertThrows(
                    NegocioException.class,
                    () -> entregaService.cadastrarEntregaColaborador(matricula.toString(), dto)
            );
            assertEquals("O colaborador já atingiu o limite de 4 entregas cadastradas", ex.getMessage());

            // Verifica que save() não foi chamado
            verify(entregaRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar IllegalArgumentException quando UUID for inválido")
        void deveLancarIllegalArgumentExceptionQuandoUuidInvalido() {
            // Arrange
            String uuidInvalido = "uuid-invalido";
            var dto = new EntregaCadastroRequest("Qualquer", 3.5);

            // Act + Assert
            // Tenta cadastrar com UUID inválido e deve lançar IllegalArgumentException
            assertThrows(
                    IllegalArgumentException.class,
                    () -> entregaService.cadastrarEntregaColaborador(uuidInvalido, dto)
            );

            // Verifica que repositórios não foram acessados
            verify(colaboradorRepository, never()).findById(any());
            verify(entregaRepository, never()).save(any());
        }
    }

    @Nested
    class consultarEntregaPorIdEntity {

        @Test
        @DisplayName("Deve retornar DTO da entrega quando colaborador e entrega existirem e pertencerem")
        void deveConsultarEntregaComSucesso() {
            // Arrange
            UUID matricula = UUID.randomUUID();
            Long entregaId = 50L;

            // Simula um colaborador encontrado e vindo do banco de dados
            var colaborador = new ColaboradorEntity(matricula, "Carol", LocalDate.of(2024,3,3), "Dev");

            // Simula entrega que pertence ao colaborador
            var entrega = new EntregaEntity();
            entrega.setId(entregaId);
            entrega.setDescricao("Entrega 50");
            entrega.setNota(5.0);
            entrega.setColaborador(colaborador);

            when(colaboradorRepository.findById(matricula)).thenReturn(Optional.of(colaborador));
            when(entregaRepository.findById(entregaId)).thenReturn(Optional.of(entrega));

            // Act
            // Executa o método consultarEntregaPorId() para, de fato, testá-lo
            EntregaResponse dto = entregaService.consultarEntregaPorId(matricula.toString(), entregaId);

            // Assert
            // Verifica interações
            verify(colaboradorRepository).findById(matricula);
            verify(entregaRepository).findById(entregaId);

            // Verifica retorno
            assertEquals(entregaId, dto.id());
            assertEquals("Entrega 50", dto.descricao());
            assertEquals(5.0, dto.nota());
        }

        @Test
        @DisplayName("Deve lançar ResourceNotFoundException quando colaborador não existir")
        void deveLancarResourceNotFoundQuandoColaboradorNaoExistir() {
            // Arrange
            UUID matricula = UUID.randomUUID();
            when(colaboradorRepository.findById(matricula)).thenReturn(Optional.empty());

            // Act + Assert
            assertThrows(
                    ResourceNotFoundException.class,
                    () -> entregaService.consultarEntregaPorId(matricula.toString(), 1L)
            );

            verify(entregaRepository, never()).findById(anyLong());
        }

        @Test
        @DisplayName("Deve lançar ResourceNotFoundException quando entrega não existir")
        void deveLancarResourceNotFoundQuandoEntregaNaoExistir() {
            // Arrange
            UUID matricula = UUID.randomUUID();
            Long entregaId = 99L;
            var colaborador = new ColaboradorEntity(matricula, "Dan", LocalDate.of(2024,4,4), "Arq");
            when(colaboradorRepository.findById(matricula)).thenReturn(Optional.of(colaborador));
            when(entregaRepository.findById(entregaId)).thenReturn(Optional.empty());

            // Act + Assert
            assertThrows(
                    ResourceNotFoundException.class,
                    () -> entregaService.consultarEntregaPorId(matricula.toString(), entregaId)
            );
        }

        @Test
        @DisplayName("Deve lançar NegocioException quando entrega não pertencer ao colaborador")
        void deveLancarNegocioQuandoEntregaNaoPertencer() {
            // Arrange
            UUID matricula = UUID.randomUUID();
            UUID outro = UUID.randomUUID();
            Long entregaId = 77L;

            var colaborador = new ColaboradorEntity(matricula, "Eve", LocalDate.of(2024,5,5), "QA");
            var outroColab = new ColaboradorEntity(outro, "X", LocalDate.of(2024,1,1), "Y");

            var entrega = new EntregaEntity();
            entrega.setId(entregaId);
            entrega.setDescricao("Entrega 77");
            entrega.setNota(4.0);
            entrega.setColaborador(outroColab); // pertence a outro colaborador

            when(colaboradorRepository.findById(matricula)).thenReturn(Optional.of(colaborador));
            when(entregaRepository.findById(entregaId)).thenReturn(Optional.of(entrega));

            // Act + Assert
            NegocioException ex = assertThrows(
                    NegocioException.class,
                    () -> entregaService.consultarEntregaPorId(matricula.toString(), entregaId)
            );
            assertTrue(ex.getMessage().contains("Acesso negado"));

            verify(entregaRepository).findById(entregaId);
        }

        @Test
        @DisplayName("Deve lançar IllegalArgumentException quando UUID for inválido")
        void deveLancarIllegalArgumentQuandoUuidInvalido() {
            // Arrange
            String uuidInvalido = "invalid";

            // Act + Assert
            assertThrows(
                    IllegalArgumentException.class,
                    () -> entregaService.consultarEntregaPorId(uuidInvalido, 1L)
            );

            verify(colaboradorRepository, never()).findById(any());
            verify(entregaRepository, never()).findById(anyLong());
        }
    }

    @Nested
    class listarEntregasPorColaboradorEntity {

        @Test
        @DisplayName("Deve listar entregas do colaborador mapeadas para DTO")
        void deveListarEntregas() {
            // Arrange
            UUID matricula = UUID.randomUUID();
            var colaborador = new ColaboradorEntity(matricula, "Fay", LocalDate.of(2024,6,6), "PM");

            var e1 = new EntregaEntity(); e1.setId(1L); e1.setDescricao("E1"); e1.setNota(3.0); e1.setColaborador(colaborador);
            var e2 = new EntregaEntity(); e2.setId(2L); e2.setDescricao("E2"); e2.setNota(4.0); e2.setColaborador(colaborador);
            colaborador.setEntregas(List.of(e1, e2));

            when(colaboradorRepository.findById(matricula)).thenReturn(Optional.of(colaborador));

            // Act
            // Executa o método listarEntregasPorColaborador() para, de fato, testá-lo
            List<EntregaResponse> lista = entregaService.listarEntregasPorColaborador(matricula.toString());

            // Assert
            verify(colaboradorRepository).findById(matricula);
            assertEquals(2, lista.size());
            assertEquals(1L, lista.getFirst().id());
            assertEquals("E1", lista.get(0).descricao());
            assertEquals(3.0, lista.get(0).nota());
            assertEquals(2L, lista.get(1).id());
            assertEquals("E2", lista.get(1).descricao());
            assertEquals(4.0, lista.get(1).nota());
        }

        @Test
        @DisplayName("Deve lançar ResourceNotFoundException quando colaborador não existir")
        void deveLancarResourceNotFoundQuandoColaboradorNaoExistir() {
            // Arrange
            UUID matricula = UUID.randomUUID();
            when(colaboradorRepository.findById(matricula)).thenReturn(Optional.empty());

            // Act + Assert
            assertThrows(
                    ResourceNotFoundException.class,
                    () -> entregaService.listarEntregasPorColaborador(matricula.toString())
            );
        }

        @Test
        @DisplayName("Deve lançar IllegalArgumentException quando UUID for inválido")
        void deveLancarIllegalArgumentExceptionQuandoUuidInvalido() {
            // Arrange
            String uuidInvalido = "oops";

            // Act + Assert
            assertThrows(
                    IllegalArgumentException.class,
                    () -> entregaService.listarEntregasPorColaborador(uuidInvalido)
            );

            verify(colaboradorRepository, never()).findById(any());
        }
    }

    @Nested
    class deletarEntregaColaboradorEntityEntity {

        @Test
        @DisplayName("Deve deletar entrega quando pertencer ao colaborador")
        void deveDeletarEntregaComSucesso() {
            // Arrange
            UUID matricula = UUID.randomUUID();
            Long entregaId = 33L;

            // Simula colaborador e entrega pertencente a ele
            var colaborador = new ColaboradorEntity(matricula, "Gus", LocalDate.of(2024,7,7), "Dev Sr.");
            var entrega = new EntregaEntity();
            entrega.setId(entregaId);
            entrega.setColaborador(colaborador);

            when(colaboradorRepository.findById(matricula)).thenReturn(Optional.of(colaborador));
            when(entregaRepository.findById(entregaId)).thenReturn(Optional.of(entrega));

            // Act
            // Executa o método deletarEntregaColaborador() para, de fato, testá-lo
            entregaService.deletarEntregaColaborador(matricula.toString(), entregaId);

            // Assert
            verify(colaboradorRepository).findById(matricula);
            verify(entregaRepository).findById(entregaId);
            verify(entregaRepository).deleteById(entregaId);
        }

        @Test
        @DisplayName("Deve lançar ResourceNotFoundException quando colaborador não existir")
        void deveLancarResourceNotFoundQuandoColaboradorNaoExistir() {
            // Arrange
            UUID matricula = UUID.randomUUID();
            when(colaboradorRepository.findById(matricula)).thenReturn(Optional.empty());

            // Act + Assert
            assertThrows(
                    ResourceNotFoundException.class,
                    () -> entregaService.deletarEntregaColaborador(matricula.toString(), 1L)
            );

            verify(entregaRepository, never()).deleteById(anyLong());
        }

        @Test
        @DisplayName("Deve lançar ResourceNotFoundException quando entrega não existir")
        void deveLancarResourceNotFoundQuandoEntregaNaoExistir() {
            // Arrange
            UUID matricula = UUID.randomUUID();
            Long entregaId = 123L;
            var colaborador = new ColaboradorEntity(matricula, "Hil", LocalDate.of(2024,8,8), "PO");
            when(colaboradorRepository.findById(matricula)).thenReturn(Optional.of(colaborador));
            when(entregaRepository.findById(entregaId)).thenReturn(Optional.empty());

            // Act + Assert
            assertThrows(
                    ResourceNotFoundException.class,
                    () -> entregaService.deletarEntregaColaborador(matricula.toString(), entregaId)
            );

            verify(entregaRepository, never()).deleteById(anyLong());
        }

        @Test
        @DisplayName("Deve lançar NegocioException quando entrega não pertencer ao colaborador")
        void deveLancarNegocioQuandoEntregaNaoPertencerAoColaborador() {
            // Arrange
            UUID matricula = UUID.randomUUID();
            UUID outro = UUID.randomUUID();
            Long entregaId = 44L;

            var colaborador = new ColaboradorEntity(matricula, "Ian", LocalDate.of(2024,9,9), "QA");
            var outroColab = new ColaboradorEntity(outro, "X", LocalDate.of(2024,1,1), "Y");

            var entrega = new EntregaEntity();
            entrega.setId(entregaId);
            entrega.setColaborador(outroColab); // entrega de outro colaborador

            when(colaboradorRepository.findById(matricula)).thenReturn(Optional.of(colaborador));
            when(entregaRepository.findById(entregaId)).thenReturn(Optional.of(entrega));

            // Act + Assert
            NegocioException ex = assertThrows(
                    NegocioException.class,
                    () -> entregaService.deletarEntregaColaborador(matricula.toString(), entregaId)
            );
            assertTrue(ex.getMessage().contains("Acesso negado"));

            verify(entregaRepository, never()).deleteById(anyLong());
        }

        @Test
        @DisplayName("Deve lançar IllegalArgumentException quando UUID for inválido")
        void deveLancarIllegalArgumentExceptionQuandoUuidInvalido() {
            // Arrange
            String uuidInvalido = "bad-uuid";

            // Act + Assert
            assertThrows(
                    IllegalArgumentException.class,
                    () -> entregaService.deletarEntregaColaborador(uuidInvalido, 1L)
            );

            verify(colaboradorRepository, never()).findById(any());
            verify(entregaRepository, never()).findById(anyLong());
        }
    }

    @Nested
    class atualizarEntregaPorIdEntity {

        @Test
        @DisplayName("Deve atualizar descrição e nota quando ambas forem informadas")
        void deveAtualizarDescricaoENota() {
            // Arrange
            UUID matricula = UUID.randomUUID();
            Long entregaId = 70L;

            // Simula colaborador e entrega pertencente
            var colaborador = new ColaboradorEntity(matricula, "Jade", LocalDate.of(2024,10,10), "Arq");
            var entrega = new EntregaEntity(); entrega.setId(entregaId); entrega.setDescricao("Antiga"); entrega.setNota(3.0); entrega.setColaborador(colaborador);

            // Configura o Mock para retornar colaborador e entrega
            when(colaboradorRepository.findById(matricula)).thenReturn(Optional.of(colaborador));
            when(entregaRepository.findById(entregaId)).thenReturn(Optional.of(entrega));

            // Cria o DTO simulando o input do usuário
            var dto = new EntregaAtualizaRequest("Nova Desc", 4.5);

            // Verifica qual objeto foi passado para o método save()
            ArgumentCaptor<EntregaEntity> entregaPassadaSave = ArgumentCaptor.forClass(EntregaEntity.class);

            // Act
            // Executa o método atualizarEntregaPorId() para, de fato, testá-lo
            entregaService.atualizarEntregaPorId(matricula.toString(), entregaId, dto);

            // Assert
            // Verifica interações
            verify(colaboradorRepository).findById(matricula);
            verify(entregaRepository).findById(entregaId);
            verify(entregaRepository).save(entregaPassadaSave.capture());

            // Verifica alterações
            EntregaEntity salvo = entregaPassadaSave.getValue();
            assertEquals("Nova Desc", salvo.getDescricao());
            assertEquals(4.5, salvo.getNota());
        }

        @Test
        @DisplayName("Deve atualizar apenas a descrição quando nota for nula")
        void deveAtualizarApenasDescricao() {
            // Arrange
            UUID matricula = UUID.randomUUID();
            Long entregaId = 71L;

            var colaborador = new ColaboradorEntity(matricula, "Kai", LocalDate.of(2024,11,11), "Eng");
            var entrega = new EntregaEntity(); entrega.setId(entregaId); entrega.setDescricao("Antiga"); entrega.setNota(2.0); entrega.setColaborador(colaborador);

            when(colaboradorRepository.findById(matricula)).thenReturn(Optional.of(colaborador));
            when(entregaRepository.findById(entregaId)).thenReturn(Optional.of(entrega));

            var dto = new EntregaAtualizaRequest("Desc Atualizada", null);

            ArgumentCaptor<EntregaEntity> entregaPassadaSave = ArgumentCaptor.forClass(EntregaEntity.class);

            // Act
            entregaService.atualizarEntregaPorId(matricula.toString(), entregaId, dto);

            // Assert
            verify(entregaRepository).save(entregaPassadaSave.capture());
            EntregaEntity salvo = entregaPassadaSave.getValue();
            assertEquals("Desc Atualizada", salvo.getDescricao());
            assertEquals(2.0, salvo.getNota()); // preservada
        }

        @Test
        @DisplayName("Deve atualizar apenas a nota quando descrição for nula")
        void deveAtualizarApenasNota() {
            // Arrange
            UUID matricula = UUID.randomUUID();
            Long entregaId = 72L;

            var colaborador = new ColaboradorEntity(matricula, "Lia", LocalDate.of(2024,12,12), "Dev");
            var entrega = new EntregaEntity(); entrega.setId(entregaId); entrega.setDescricao("Keep"); entrega.setNota(1.5); entrega.setColaborador(colaborador);

            when(colaboradorRepository.findById(matricula)).thenReturn(Optional.of(colaborador));
            when(entregaRepository.findById(entregaId)).thenReturn(Optional.of(entrega));

            var dto = new EntregaAtualizaRequest(null, 4.0);

            ArgumentCaptor<EntregaEntity> entregaPassadaSave = ArgumentCaptor.forClass(EntregaEntity.class);

            // Act
            entregaService.atualizarEntregaPorId(matricula.toString(), entregaId, dto);

            // Assert
            verify(entregaRepository).save(entregaPassadaSave.capture());
            EntregaEntity salvo = entregaPassadaSave.getValue();
            assertEquals("Keep", salvo.getDescricao()); // preservada
            assertEquals(4.0, salvo.getNota());         // atualizada
        }

        @Test
        @DisplayName("Deve lançar RuntimeException quando colaborador não existir")
        void deveLancarRuntimeQuandoColaboradorNaoExistir() {
            // Arrange
            UUID matricula = UUID.randomUUID();
            when(colaboradorRepository.findById(matricula)).thenReturn(Optional.empty());

            // Act + Assert
            RuntimeException ex = assertThrows(
                    RuntimeException.class,
                    () -> entregaService.atualizarEntregaPorId(matricula.toString(), 1L, new EntregaAtualizaRequest("x", 2.0))
            );
            assertTrue(ex.getMessage().contains("Colaborador não encontrado"));

            verify(entregaRepository, never()).findById(anyLong());
            verify(entregaRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar RuntimeException quando entrega não existir")
        void deveLancarRuntimeQuandoEntregaNaoExistir() {
            // Arrange
            UUID matricula = UUID.randomUUID();
            var colaborador = new ColaboradorEntity(matricula, "Meg", LocalDate.of(2024,10,1), "Líder");
            when(colaboradorRepository.findById(matricula)).thenReturn(Optional.of(colaborador));
            when(entregaRepository.findById(9L)).thenReturn(Optional.empty());

            // Act + Assert
            RuntimeException ex = assertThrows(
                    RuntimeException.class,
                    () -> entregaService.atualizarEntregaPorId(matricula.toString(), 9L, new EntregaAtualizaRequest("x", 2.0))
            );
            assertTrue(ex.getMessage().contains("Entrega não encontrada"));

            verify(entregaRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar RuntimeException quando entrega não pertencer ao colaborador")
        void deveLancarRuntimeQuandoEntregaDeOutroColaborador() {
            // Arrange
            UUID matricula = UUID.randomUUID();
            UUID outro = UUID.randomUUID();
            Long entregaId = 80L;

            var colaborador = new ColaboradorEntity(matricula, "Neo", LocalDate.of(2024,9,9), "SE");
            var outroColab = new ColaboradorEntity(outro, "Trin", LocalDate.of(2024,9,1), "SE");

            var entrega = new EntregaEntity(); entrega.setId(entregaId); entrega.setColaborador(outroColab);

            when(colaboradorRepository.findById(matricula)).thenReturn(Optional.of(colaborador));
            when(entregaRepository.findById(entregaId)).thenReturn(Optional.of(entrega));

            // Act + Assert
            RuntimeException ex = assertThrows(
                    RuntimeException.class,
                    () -> entregaService.atualizarEntregaPorId(matricula.toString(), entregaId, new EntregaAtualizaRequest("x", 2.0))
            );
            assertTrue(ex.getMessage().contains("Acesso negado"));

            verify(entregaRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar IllegalArgumentException quando UUID for inválido")
        void deveLancarIllegalArgumentExceptionQuandoUuidInvalido() {
            // Arrange
            String uuidInvalido = "nope";

            // Act + Assert
            assertThrows(
                    IllegalArgumentException.class,
                    () -> entregaService.atualizarEntregaPorId(uuidInvalido, 1L, new EntregaAtualizaRequest("x", 2.0))
            );

            verify(colaboradorRepository, never()).findById(any());
            verify(entregaRepository, never()).findById(anyLong());
            verify(entregaRepository, never()).save(any());
        }
    }
}