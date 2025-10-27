package com.example.demo.business.services;

import com.example.demo.controller.dto.AtualizaAvaliacaoComportamentoDTO;
import com.example.demo.controller.dto.AvaliacaoComportamentoRespostaDTO;
import com.example.demo.controller.dto.CadastroAvaliacaoComportamentoDTO;
import com.example.demo.infrastructure.exceptions.NegocioException;
import com.example.demo.infrastructure.exceptions.ResourceNotFoundException;
import com.example.demo.infrastructure.model.AvaliacaoComportamento;
import com.example.demo.infrastructure.model.Colaborador;
import com.example.demo.infrastructure.repository.AvaliacaoComportamentoRepository;
import com.example.demo.infrastructure.repository.ColaboradorRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AvaliacaoComportamentoServiceTest {

    @Mock
    private AvaliacaoComportamentoRepository avaliacaoComportamentoRepository;

    @Mock
    private ColaboradorRepository colaboradorRepository;

    @InjectMocks
    private AvaliacaoComportamentoService avaliacaoService;

    @Nested
    class cadastrarAvaliacaoComportamental {

        @Test
        @DisplayName("Deve cadastrar avaliação para colaborador existente e retornar o ID")
        void deveCadastrarAvaliacaoParaColaboradorExistente() {
            // Arrange
            UUID matricula = UUID.randomUUID();

            // Simula um colaborador encontrado e vindo do banco de dados
            var colaborador = new Colaborador(matricula, "Alice", LocalDate.of(2024,1,1), "Engenheira");

            // Configura o Mock para retornar um colaborador (ou vazio) ao executar o método findById()
            when(colaboradorRepository.findById(matricula)).thenReturn(Optional.of(colaborador));

            // Cria o DTO simulando o input do usuário
            var dto = new CadastroAvaliacaoComportamentoDTO(5.0, 4.0, 3.0, 5.0);

            // Verifica qual objeto foi passado para o método save() do repositório de avaliações
            ArgumentCaptor<AvaliacaoComportamento> avaliacaoPassadaSave = ArgumentCaptor.forClass(AvaliacaoComportamento.class);

            // Configura o Mock para retornar a avaliação com ID após salvar
            var salvo = new AvaliacaoComportamento(10L, 5.0, 4.0, 3.0, 5.0);
            salvo.setColaborador(colaborador);
            when(avaliacaoComportamentoRepository.save(any(AvaliacaoComportamento.class))).thenReturn(salvo);

            // Act
            // Executa o método cadastrarAvaliacaoComportamental() para, de fato, testá-lo
            Long idRetornado = avaliacaoService.cadastrarAvaliacaoComportamental(matricula.toString(), dto);

            // Assert
            // Verifica se o método findById() do objeto colaboradorRepository foi chamado exatamente uma vez
            verify(colaboradorRepository).findById(matricula);

            // Verifica se o método save() do objeto avaliacaoComportamentoRepository foi chamado exatamente uma vez
            verify(avaliacaoComportamentoRepository).save(avaliacaoPassadaSave.capture());

            // Verifica se o objeto avaliação salvo recebeu as notas corretas e a associação com o colaborador
            AvaliacaoComportamento capturado = avaliacaoPassadaSave.getValue();
            assertEquals(5.0, capturado.getNotaAvaliacaoComportamental());
            assertEquals(4.0, capturado.getNotaAprendizado());
            assertEquals(3.0, capturado.getNotaTomadaDecisao());
            assertEquals(5.0, capturado.getNotaAutonomia());
            assertEquals(colaborador, capturado.getColaborador());

            // Verifica se o ID retornado é o mesmo gerado pelo save()
            assertEquals(10L, idRetornado);
        }

        @Test
        @DisplayName("Deve lançar ResourceNotFoundException quando colaborador não existir")
        void deveLancarResourceNotFoundQuandoColaboradorNaoExistir() {
            // Arrange
            UUID matricula = UUID.randomUUID();

            // Configura o Mock para retornar vazio ao executar o método findById()
            when(colaboradorRepository.findById(matricula)).thenReturn(Optional.empty());

            // Cria o DTO simulando o input do usuário
            var dto = new CadastroAvaliacaoComportamentoDTO(5.0, 4.0, 3.0, 5.0);

            // Act + Assert
            // Tenta cadastrar avaliação para matrícula inexistente e deve retornar ResourceNotFoundException
            assertThrows(
                    ResourceNotFoundException.class,
                    () -> avaliacaoService.cadastrarAvaliacaoComportamental(matricula.toString(), dto)
            );

            // Verifica que o save() de avaliações não foi chamado
            verify(avaliacaoComportamentoRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar IllegalArgumentException quando UUID for inválido")
        void deveLancarIllegalArgumentExceptionQuandoUuidInvalido() {
            // Arrange
            String uuidInvalido = "nao-e-uuid";
            var dto = new CadastroAvaliacaoComportamentoDTO(5.0, 4.0, 3.0, 5.0);

            // Act + Assert
            // Tenta cadastrar com UUID inválido e deve lançar IllegalArgumentException
            assertThrows(
                    IllegalArgumentException.class,
                    () -> avaliacaoService.cadastrarAvaliacaoComportamental(uuidInvalido, dto)
            );

            // Verifica que nenhum repositório foi acessado
            verify(colaboradorRepository, never()).findById(any());
            verify(avaliacaoComportamentoRepository, never()).save(any());
        }
    }

    @Nested
    class consultaAvaliacaoPorMatricula {

        @Test
        @DisplayName("Deve retornar DTO de resposta com as notas e média calculada")
        void deveRetornarAvaliacaoComMediaCalculada() {
            // Arrange
            UUID matricula = UUID.randomUUID();

            // Simula um colaborador encontrado e vindo do banco de dados
            var colaborador = new Colaborador(matricula, "Bob", LocalDate.of(2024,2,2), "Analista");

            // Cria uma avaliação existente associada ao colaborador
            var avaliacao = new AvaliacaoComportamento(100L, 5.0, 4.0, 3.0, 5.0);
            avaliacao.setColaborador(colaborador);
            colaborador.setAvaliacaoComportamento(avaliacao);

            // Configura o Mock para retornar o colaborador (com avaliação)
            when(colaboradorRepository.findById(matricula)).thenReturn(Optional.of(colaborador));

            // Calcula a média esperada
            var mediaEsperada = BigDecimal.valueOf(5.0)
                    .add(BigDecimal.valueOf(4.0))
                    .add(BigDecimal.valueOf(3.0))
                    .add(BigDecimal.valueOf(5.0))
                    .divide(new BigDecimal("4"), 2, RoundingMode.HALF_UP);

            // Act
            // Executa o método consultaAvaliacaoPorMatricula() para, de fato, testá-lo
            AvaliacaoComportamentoRespostaDTO resposta = avaliacaoService.consultaAvaliacaoPorMatricula(matricula.toString());

            // Assert
            // Verifica se o método findById() foi chamado exatamente uma vez
            verify(colaboradorRepository).findById(matricula);

            // Verifica se as notas e a média retornadas estão corretas
            assertEquals(5.0, resposta.avaliacaoComportamento().notaAvaliacaoComportamental());
            assertEquals(4.0, resposta.avaliacaoComportamento().notaAprendizado());
            assertEquals(3.0, resposta.avaliacaoComportamento().notaTomadaDecisao());
            assertEquals(5.0, resposta.avaliacaoComportamento().notaAutonomia());
            assertEquals(mediaEsperada, resposta.avaliacaoComportamento().mediaNotas());
        }

        @Test
        @DisplayName("Deve lançar ResourceNotFoundException quando colaborador não existir")
        void deveLancarResourceNotFoundQuandoColaboradorNaoExiste() {
            // Arrange
            UUID matricula = UUID.randomUUID();

            // Configura o Mock para retornar vazio ao executar o método findById()
            when(colaboradorRepository.findById(matricula)).thenReturn(Optional.empty());

            // Act + Assert
            // Tenta consultar avaliação de matrícula inexistente e deve lançar ResourceNotFoundException
            assertThrows(
                    ResourceNotFoundException.class,
                    () -> avaliacaoService.consultaAvaliacaoPorMatricula(matricula.toString())
            );
        }

        @Test
        @DisplayName("Deve lançar ResourceNotFoundException quando colaborador não possuir avaliação")
        void deveLancarResourceNotFoundQuandoSemAvaliacao() {
            // Arrange
            UUID matricula = UUID.randomUUID();

            // Simula um colaborador encontrado sem avaliação
            var colaborador = new Colaborador(matricula, "Carol", LocalDate.of(2024,3,3), "Dev");
            colaborador.setAvaliacaoComportamento(null);

            // Configura o Mock para retornar o colaborador (sem avaliação)
            when(colaboradorRepository.findById(matricula)).thenReturn(Optional.of(colaborador));

            // Act + Assert
            // Tenta consultar quando não há avaliação e deve lançar ResourceNotFoundException
            assertThrows(
                    ResourceNotFoundException.class,
                    () -> avaliacaoService.consultaAvaliacaoPorMatricula(matricula.toString())
            );
        }

        @Test
        @DisplayName("Deve lançar IllegalArgumentException quando UUID for inválido")
        void deveLancarIllegalArgumentExceptionQuandoUuidInvalido() {
            // Arrange
            String uuidInvalido = "uuid-invalido";

            // Act + Assert
            // Tenta consultar com UUID inválido e deve lançar IllegalArgumentException
            assertThrows(
                    IllegalArgumentException.class,
                    () -> avaliacaoService.consultaAvaliacaoPorMatricula(uuidInvalido)
            );

            // Verifica que o repositório não foi chamado
            verify(colaboradorRepository, never()).findById(any());
        }
    }

    @Nested
    class atualizaAvaliacaoPorMarticula {

        @Test
        @DisplayName("Deve atualizar todas as notas quando todas forem informadas")
        void deveAtualizarTodasAsNotas() {
            // Arrange
            UUID matricula = UUID.randomUUID();

            // Simula um colaborador encontrado e vindo do banco de dados
            var colaborador = new Colaborador(matricula, "Dan", LocalDate.of(2024,4,4), "Arquiteto");

            // Cria uma avaliação existente associada ao colaborador
            var avaliacao = new AvaliacaoComportamento(200L, 2.0, 2.0, 2.0, 2.0);
            avaliacao.setColaborador(colaborador);
            colaborador.setAvaliacaoComportamento(avaliacao);

            // Configura o Mock para retornar um colaborador (ou vazio) ao executar o método findById()
            when(colaboradorRepository.findById(matricula)).thenReturn(Optional.of(colaborador));

            // Cria o DTO simulando o input do usuário
            var dto = new AtualizaAvaliacaoComportamentoDTO(5.0, 4.0, 3.0, 5.0);

            // Verifica qual objeto foi passado para o método save()
            ArgumentCaptor<AvaliacaoComportamento> avaliacaoPassadaSave = ArgumentCaptor.forClass(AvaliacaoComportamento.class);

            // Act
            // Executa o método atualizaAvaliacaoPorMarticula() para, de fato, testá-lo
            avaliacaoService.atualizaAvaliacaoPorMarticula(matricula.toString(), dto);

            // Assert
            // Verifica se o método findById() do objeto colaboradorRepository foi chamado exatamente uma vez
            verify(colaboradorRepository).findById(matricula);

            // Verifica se o método save() do objeto avaliacaoComportamentoRepository foi chamado exatamente uma vez
            verify(avaliacaoComportamentoRepository).save(avaliacaoPassadaSave.capture());

            // Verifica se a avaliação teve todas as notas atualizadas
            AvaliacaoComportamento salvo = avaliacaoPassadaSave.getValue();
            assertEquals(5.0, salvo.getNotaAvaliacaoComportamental());
            assertEquals(4.0, salvo.getNotaAprendizado());
            assertEquals(3.0, salvo.getNotaTomadaDecisao());
            assertEquals(5.0, salvo.getNotaAutonomia());
        }

        @Test
        @DisplayName("Deve atualizar parcialmente quando apenas algumas notas forem informadas")
        void deveAtualizarParcialmenteAsNotas() {
            // Arrange
            UUID matricula = UUID.randomUUID();

            // Simula um colaborador encontrado e vindo do banco de dados
            var colaborador = new Colaborador(matricula, "Eva", LocalDate.of(2024,5,5), "Analista Sr.");

            // Cria uma avaliação existente associada ao colaborador
            var avaliacao = new AvaliacaoComportamento(300L, 2.0, 3.0, 4.0, 1.0);
            avaliacao.setColaborador(colaborador);
            colaborador.setAvaliacaoComportamento(avaliacao);

            // Configura o Mock para retornar um colaborador (ou vazio) ao executar o método findById()
            when(colaboradorRepository.findById(matricula)).thenReturn(Optional.of(colaborador));

            // Cria o DTO simulando o input do usuário (atualiza apenas duas notas)
            var dto = new AtualizaAvaliacaoComportamentoDTO(null, 5.0, null, 4.5);

            // Verifica qual objeto foi passado para o método save()
            ArgumentCaptor<AvaliacaoComportamento> avaliacaoPassadaSave = ArgumentCaptor.forClass(AvaliacaoComportamento.class);

            // Act
            // Executa o método atualizaAvaliacaoPorMarticula() para, de fato, testá-lo
            avaliacaoService.atualizaAvaliacaoPorMarticula(matricula.toString(), dto);

            // Assert
            // Verifica se o método findById() do objeto colaboradorRepository foi chamado exatamente uma vez
            verify(colaboradorRepository).findById(matricula);

            // Verifica se o método save() do objeto avaliacaoComportamentoRepository foi chamado exatamente uma vez
            verify(avaliacaoComportamentoRepository).save(avaliacaoPassadaSave.capture());

            // Verifica se apenas as notas informadas foram atualizadas
            AvaliacaoComportamento salvo = avaliacaoPassadaSave.getValue();
            assertEquals(2.0, salvo.getNotaAvaliacaoComportamental()); // preservada
            assertEquals(5.0, salvo.getNotaAprendizado());             // atualizada
            assertEquals(4.0, salvo.getNotaTomadaDecisao());           // preservada
            assertEquals(4.5, salvo.getNotaAutonomia());               // atualizada
        }

        @Test
        @DisplayName("Deve lançar NegocioException quando colaborador não possuir avaliação cadastrada")
        void deveLancarNegocioExceptionQuandoSemAvaliacaoParaAtualizar() {
            // Arrange
            UUID matricula = UUID.randomUUID();

            // Simula um colaborador encontrado e vindo do banco de dados sem avaliação
            var colaborador = new Colaborador(matricula, "Fred", LocalDate.of(2024,6,6), "Designer");
            colaborador.setAvaliacaoComportamento(null);

            // Configura o Mock para retornar o colaborador (sem avaliação)
            when(colaboradorRepository.findById(matricula)).thenReturn(Optional.of(colaborador));

            // Cria o DTO simulando o input do usuário
            var dto = new AtualizaAvaliacaoComportamentoDTO(5.0, 4.0, 3.0, 5.0);

            // Act + Assert
            // Tenta atualizar sem avaliação e deve retornar NegocioException
            NegocioException ex = assertThrows(
                    NegocioException.class,
                    () -> avaliacaoService.atualizaAvaliacaoPorMarticula(matricula.toString(), dto)
            );

            // Verifica a mensagem da exceção
            assertEquals("O colaborador " + matricula + " não possui uma avaliação comportamental para atualizar", ex.getMessage());

            // Verifica que o save() de avaliações não foi chamado
            verify(avaliacaoComportamentoRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar ResourceNotFoundException quando colaborador não existir")
        void deveLancarResourceNotFoundQuandoColaboradorNaoExistir() {
            // Arrange
            UUID matricula = UUID.randomUUID();

            // Configura o Mock para retornar vazio ao executar o método findById()
            when(colaboradorRepository.findById(matricula)).thenReturn(Optional.empty());

            // Cria o DTO simulando o input do usuário
            var dto = new AtualizaAvaliacaoComportamentoDTO(5.0, 4.0, 3.0, 5.0);

            // Act + Assert
            // Tenta atualizar notas para matrícula inexistente e deve lançar ResourceNotFoundException
            assertThrows(
                    ResourceNotFoundException.class,
                    () -> avaliacaoService.atualizaAvaliacaoPorMarticula(matricula.toString(), dto)
            );

            // Verifica que o save() não foi chamado
            verify(avaliacaoComportamentoRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar IllegalArgumentException quando UUID for inválido")
        void deveLancarIllegalArgumentExceptionQuandoUuidInvalido() {
            // Arrange
            String uuidInvalido = "invalid-uuid";
            var dto = new AtualizaAvaliacaoComportamentoDTO(5.0, 4.0, 3.0, 5.0);

            // Act + Assert
            // Tenta atualizar notas com UUID inválido e deve lançar IllegalArgumentException
            assertThrows(
                    IllegalArgumentException.class,
                    () -> avaliacaoService.atualizaAvaliacaoPorMarticula(uuidInvalido, dto)
            );

            // Verifica que nenhum repositório foi acessado
            verify(colaboradorRepository, never()).findById(any());
            verify(avaliacaoComportamentoRepository, never()).save(any());
        }
    }

    @Nested
    class deletarAvaliacoesPorMatricula {

        @Test
        @DisplayName("Deve remover a avaliação quando existir e salvar o colaborador (orphanRemoval)")
        void deveRemoverAvaliacaoQuandoExistir() {
            // Arrange
            UUID matricula = UUID.randomUUID();

            // Simula um colaborador encontrado e vindo do banco de dados com avaliação
            var colaborador = new Colaborador(matricula, "Gina", LocalDate.of(2024,7,7), "PO");
            var avaliacao = new AvaliacaoComportamento(500L, 5.0, 5.0, 5.0, 5.0);
            avaliacao.setColaborador(colaborador);
            colaborador.setAvaliacaoComportamento(avaliacao);

            // Configura o Mock para retornar um colaborador (ou vazio) ao executar o método findById()
            when(colaboradorRepository.findById(matricula)).thenReturn(Optional.of(colaborador));

            // Verifica qual objeto foi passado para o método save() do colaboradorRepository
            ArgumentCaptor<Colaborador> colaboradorPassadoSave = ArgumentCaptor.forClass(Colaborador.class);

            // Act
            // Executa o método deletarAvaliacoesPorMatricula() para, de fato, testá-lo
            avaliacaoService.deletarAvaliacoesPorMatricula(matricula.toString());

            // Assert
            // Verifica se o método findById() foi chamado exatamente uma vez
            verify(colaboradorRepository).findById(matricula);

            // Verifica se o método save() foi chamado para persistir a remoção da avaliação
            verify(colaboradorRepository).save(colaboradorPassadoSave.capture());

            // Verifica se a avaliação foi desvinculada (null), acionando orphanRemoval
            Colaborador salvo = colaboradorPassadoSave.getValue();
            assertNull(salvo.getAvaliacaoComportamento());
        }

        @Test
        @DisplayName("Não deve salvar quando colaborador não possui avaliação")
        void naoDeveSalvarQuandoSemAvaliacao() {
            // Arrange
            UUID matricula = UUID.randomUUID();

            // Simula um colaborador encontrado sem avaliação
            var colaborador = new Colaborador(matricula, "Heitor", LocalDate.of(2024,8,8), "DevOps");
            colaborador.setAvaliacaoComportamento(null);

            // Configura o Mock para retornar o colaborador
            when(colaboradorRepository.findById(matricula)).thenReturn(Optional.of(colaborador));

            // Act
            // Executa o método deletarAvaliacoesPorMatricula() para, de fato, testá-lo
            avaliacaoService.deletarAvaliacoesPorMatricula(matricula.toString());

            // Assert
            // Verifica se o método findById() foi chamado exatamente uma vez
            verify(colaboradorRepository).findById(matricula);

            // Verifica que save() não foi chamado, pois não havia avaliação
            verify(colaboradorRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar ResourceNotFoundException quando colaborador não existir")
        void deveLancarResourceNotFoundQuandoColaboradorNaoExistir() {
            // Arrange
            UUID matricula = UUID.randomUUID();

            // Configura o Mock para retornar vazio ao executar o método findById()
            when(colaboradorRepository.findById(matricula)).thenReturn(Optional.empty());

            // Act + Assert
            // Tenta deletar avaliação de matrícula inexistente e deve lançar ResourceNotFoundException
            assertThrows(
                    ResourceNotFoundException.class,
                    () -> avaliacaoService.deletarAvaliacoesPorMatricula(matricula.toString())
            );

            // Verifica que save() não foi chamado
            verify(colaboradorRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar IllegalArgumentException quando UUID for inválido")
        void deveLancarIllegalArgumentExceptionQuandoUuidInvalido() {
            // Arrange
            String uuidInvalido = "uuid-invalido";

            // Act + Assert
            // Tenta deletar avaliação com UUID inválido e deve lançar IllegalArgumentException
            assertThrows(
                    IllegalArgumentException.class,
                    () -> avaliacaoService.deletarAvaliacoesPorMatricula(uuidInvalido)
            );

            // Verifica que nenhum repositório foi acessado
            verify(colaboradorRepository, never()).findById(any());
            verify(colaboradorRepository, never()).save(any());
        }
    }
}