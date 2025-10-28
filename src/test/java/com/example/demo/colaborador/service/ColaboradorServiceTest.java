package com.example.demo.colaborador.service;

import com.example.demo.colaborador.model.ColaboradorEntity;
import com.example.demo.colaborador.resource.json.ColaboradorAtualizaRequest;
import com.example.demo.colaborador.resource.json.ColaboradorCadastroRequest;
import com.example.demo.colaborador.resource.json.ColaboradorResponse;
import com.example.demo.base.exception.NegocioException;
import com.example.demo.base.exception.ResourceNotFoundException;
import com.example.demo.colaborador.avaliacao.model.AvaliacaoComportamentoEntity;
import com.example.demo.colaborador.entrega.model.EntregaEntity;
import com.example.demo.colaborador.repository.ColaboradorRepository;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ColaboradorServiceTest {

    // Arrange

    // Act

    // Assert

    @Mock
    private ColaboradorRepository colaboradorRepository;

    @InjectMocks
    private ColaboradorService colaboradorService;

    @Nested
    class cadastrarColaboradorEntity {

        @Test
        @DisplayName("Deve cadastrar um colaborador com sucesso")
        void deveCadastrarUmColaboradorComSucesso() {

            // Arrange
            var colaboradorDTO = new ColaboradorCadastroRequest(
                    "Colaborador teste",
                    LocalDate.now(),
                    "Engenheiro"
            );

            // Objeto que deve ser salvo pelo método
            var colaboradorParaSalvar = new ColaboradorEntity();
            colaboradorParaSalvar.setNome(colaboradorDTO.nome());
            colaboradorParaSalvar.setDataAdmissao(colaboradorDTO.dataAdmissao());
            colaboradorParaSalvar.setCargo(colaboradorDTO.cargo());

            // Objeto que deve ser retornado pela service após salvar
            var matriculaEsperada = UUID.randomUUID();
            var colaboradorSalvo = new ColaboradorEntity();
            colaboradorSalvo.setMatricula(matriculaEsperada);
            colaboradorSalvo.setNome(colaboradorDTO.nome());
            colaboradorSalvo.setDataAdmissao(colaboradorDTO.dataAdmissao());
            colaboradorSalvo.setCargo(colaboradorDTO.cargo());

            // Configura o Mock para retornar o objeto 'colaboradorSalvo' quando o método save() for chamado
            // com qualquer objeto do tipo Colaborador
            when(colaboradorRepository.save(any(ColaboradorEntity.class))).thenReturn(colaboradorSalvo);

            // Verifica qual objeto foi passado para o método save()
            ArgumentCaptor<ColaboradorEntity> colaboradorPassadoSave = ArgumentCaptor.forClass(ColaboradorEntity.class);

            // Act
            // Chama o método save() para, de fato, testá-lo
            UUID matriculaRetornada = colaboradorService.cadastrarColaborador(colaboradorDTO);

            // Assert
            // Verifica se o método save() só foi chamado 1 vez
            verify(colaboradorRepository, times(1)).save(colaboradorPassadoSave.capture());

            // Captura o objeto Colaborador passado para o método save()
            ColaboradorEntity colaboradorEntityPassadoParaSave = colaboradorPassadoSave.getValue();

            // Verifica se o colaborador passado para o save() tem os dados corretos do DTO
            assertNotNull(colaboradorEntityPassadoParaSave, "O colaborador passado para save() não deve ser nulo");
            assertEquals(colaboradorDTO.nome(), colaboradorEntityPassadoParaSave.getNome(), "O nome deve ser igual ao nome do DTO");
            assertEquals(colaboradorDTO.dataAdmissao(), colaboradorEntityPassadoParaSave.getDataAdmissao(), "A data de admissão deve ser igual à do DTO");
            assertEquals(colaboradorDTO.cargo(), colaboradorEntityPassadoParaSave.getCargo(), "O cargo deve ser igual ao do DTO");
            assertNull(colaboradorEntityPassadoParaSave.getMatricula(), "A matrícula deve ser nula antes de salvar, pois é gerada pelo banco");

            // Verifica se a matrícula retornada pela service é a mesma simulada pelo repositório
            assertNotNull(matriculaRetornada, "A matrícula retornada não deve ser nula");
            assertEquals(matriculaEsperada, matriculaRetornada, "A matrícula retornada deve ser a mesma gerada pelo save");
        }

        @Test
        @DisplayName("Deve chamar o método save do repositório ao cadastrar")
        void deveCadastrarColaboradorUtilizandoSave() {

            // Arrange
            var colaboradorDTO = new ColaboradorCadastroRequest(
                    "Colaborador teste 2",
                    LocalDate.now(),
                    "Analista"
            );

            // Simula o retorno do repositório
            var matriculaEsperada = UUID.randomUUID();
            var colaboradorSalvo = new ColaboradorEntity();
            colaboradorSalvo.setMatricula(matriculaEsperada);

            // Configura o Mock para retornar o objeto 'colaboradorSalvo' quando o método save() for chamado
            // com qualquer objeto do tipo Colaborador
            when(colaboradorRepository.save(any(ColaboradorEntity.class))).thenReturn(colaboradorSalvo);

            // Act
            // Chama o método cadastrarColaborador() para, de fato, testá-lo
            UUID matriculaRetornada = colaboradorService.cadastrarColaborador(colaboradorDTO);

            // Arrange
            // Verifica se o método save() só foi chamado 1 vez
            verify(colaboradorRepository, times(1)).save(any(ColaboradorEntity.class));

            assertEquals(matriculaEsperada, matriculaRetornada, "A matrícula retornada deve ser a mesma gerada pelo repositório");
        }
    }

    @Nested
    class consultarColaboradorPorMatriculaEntity {

        @Test
        @DisplayName("Deve retornar ColaboradorRespostaDTO quando a matrícula existir")
        void deveRetornarColaboradorRespostaDTOQuandoMatriculaExistir() {

            // Arrange
            UUID matriculaExistente = UUID.randomUUID();
            String matricula = matriculaExistente.toString();

            // Simula um colaborador encontrado e vindo do banco de dados
            var colaboradorEncontrado = new ColaboradorEntity();
            colaboradorEncontrado.setMatricula(matriculaExistente);
            colaboradorEncontrado.setNome("Colaborador Encontrado");
            colaboradorEncontrado.setDataAdmissao(LocalDate.now());
            colaboradorEncontrado.setCargo("Engenheiro");

            // Configura o Mock para retornar um Optional com o colaborador encontrado (da base de dados, simuladamente)
            // sempre que o método findById() foi chamado
            when(colaboradorRepository.findById(matriculaExistente)).thenReturn(Optional.of(colaboradorEncontrado));

            // Act
            // Chama o método consultarColaboradorPorMatricula() para, de fato, testá-lo
            Optional<ColaboradorResponse> resultado = colaboradorService.consultarColaboradorPorMatricula(matricula);

            // Assert
            // Verifica se o método findById() só foi chamado 1 vez
            verify(colaboradorRepository, times(1)).findById(matriculaExistente);

            // Verifica se o resultado não está vazio
            assertTrue(resultado.isPresent(), "O resultado não deve ser vazio quando o colaborador é encontrado.");

            // Extrai o objeto ColaboradorRespostaDTO do resultado retornado
            ColaboradorResponse colaboradorResponse = resultado.get();

            // Verifica se os dados no DTO correspondem aos dados do colaborador encontrado
            assertEquals(matriculaExistente, colaboradorResponse.matricula(), "A matrícula no DTO deve ser a mesma do colaborador mockado.");
            assertEquals(colaboradorEncontrado.getNome(), colaboradorResponse.nome(), "O nome no DTO deve ser o mesmo do colaborador mockado.");
            assertEquals(colaboradorEncontrado.getDataAdmissao(), colaboradorResponse.dataAdmissao(), "A data de admissão no DTO deve ser a mesma.");
            assertEquals(colaboradorEncontrado.getCargo(), colaboradorResponse.cargo(), "O cargo no DTO deve ser o mesmo.");
        }

        @Test
        @DisplayName("Deve retornar DTO com Avaliação e lista de Entregas vazia quando aplicável")
        void deveConsultarColaboradorPorMatriculaQuandoTemAvaliacaoMasNaoTemEntregas() {

            // Arrange
            UUID matriculaUUID = UUID.randomUUID();
            String matricula = matriculaUUID.toString();

            // Simula um colaborador encontrado e vindo do banco de dados, com avaliacao e sem entregas
            ColaboradorEntity colaboradorEntityEncontrado = new ColaboradorEntity(matriculaUUID, "Colaborador Com Avaliacao", LocalDate.now(), "Analista");

            AvaliacaoComportamentoEntity avaliacaoEncontrada = new AvaliacaoComportamentoEntity();
            avaliacaoEncontrada.setId(1L);
            avaliacaoEncontrada.setNotaAvaliacaoComportamental(5.0);
            avaliacaoEncontrada.setNotaAprendizado(4.0);
            avaliacaoEncontrada.setNotaTomadaDecisao(3.0);
            avaliacaoEncontrada.setNotaAutonomia(5.0);

            colaboradorEntityEncontrado.setAvaliacaoComportamento(avaliacaoEncontrada);
            colaboradorEntityEncontrado.setEntregas(Collections.emptyList());

            // Configura o Mock para retornar um Optional com o colaborador encontrado (da base de dados, simuladamente)
            // sempre que o método findById() foi chamado
            when(colaboradorRepository.findById(matriculaUUID)).thenReturn(Optional.of(colaboradorEntityEncontrado));

            // Act
            // Chama o método consultarColaboradorPorMatricula() para, de fato, testá-lo
            Optional<ColaboradorResponse> resultado = colaboradorService.consultarColaboradorPorMatricula(matricula);

            // Assert
            // Verifica se o resultado não está vazio
            assertTrue(resultado.isPresent(), "Resultado não deve ser vazio.");

            // Extrai o objeto ColaboradorRespostaDTO do resultado retornado
            ColaboradorResponse dto = resultado.get();

            // Verifica se os dados no DTO correspondem aos dados do colaborador encontrado e se a avaliação não é nula
            assertEquals(matriculaUUID, dto.matricula());
            assertEquals("Colaborador Com Avaliacao", dto.nome());

            assertNotNull(dto.avaliacaoComportamento(), "Avaliação no DTO não deve ser nula.");
            assertEquals(5.0, dto.avaliacaoComportamento().notaAvaliacaoComportamental());
            assertEquals(4.0, dto.avaliacaoComportamento().notaAprendizado());
            assertEquals(3.0, dto.avaliacaoComportamento().notaTomadaDecisao());
            assertEquals(5.0, dto.avaliacaoComportamento().notaAutonomia());

            BigDecimal mediaEsperada = BigDecimal
                    .valueOf((5.0 + 4.0 + 3.0 + 5.0) / 4.0)
                    .setScale(2, RoundingMode.HALF_UP);
            assertEquals(mediaEsperada, dto.avaliacaoComportamento().mediaNotas(), "Média das notas comportamentais incorreta.");

            assertNotNull(dto.entregas(), "Lista de entregas no DTO não deve ser nula.");
            assertTrue(dto.entregas().isEmpty(), "Lista de entregas no DTO deve estar vazia.");
        }

        @Test
        @DisplayName("Deve retornar DTO com Avaliação nula e lista de Entregas preenchida quando aplicável")
        void deveConsultarColaboradorPorMatriculaQuandoTemEntregasMasNaoTemAvaliacao() {

            // Arrange
            UUID matriculaUUID = UUID.randomUUID();
            String matricula = matriculaUUID.toString();

            // Simula um colaborador encontrado e vindo do banco de dados, com entregas e sem avaliação
            var colaboradorEncontrado = new ColaboradorEntity(matriculaUUID, "Colaborador Com Entregas", LocalDate.now(), "Engenheiro");
            colaboradorEncontrado.setAvaliacaoComportamento(null); // Garantir que a avaliação é nula

            var entrega1 = new EntregaEntity();
            entrega1.setId(10L);
            entrega1.setDescricao("Entrega Dez");
            entrega1.setNota(5.0);

            var entrega2 = new EntregaEntity();
            entrega2.setId(11L);
            entrega2.setDescricao("Entrega Onze");
            entrega2.setNota(4.0);
            colaboradorEncontrado.setEntregas(List.of(entrega1, entrega2));

            // Configura o Mock para retornar um Optional com o colaborador encontrado (da base de dados, simuladamente)
            // sempre que o método findById() foi chamado
            when(colaboradorRepository.findById(matriculaUUID)).thenReturn(Optional.of(colaboradorEncontrado));

            // Act
            // Chama o método consultarColaboradorPorMatricula() para, de fato, testá-lo
            Optional<ColaboradorResponse> resultado = colaboradorService.consultarColaboradorPorMatricula(matricula);

            // Assert
            // Verifica se o resultado não está vazio
            assertTrue(resultado.isPresent(), "Resultado não deve ser vazio.");

            // Extrai o objeto ColaboradorRespostaDTO do resultado retornado
            ColaboradorResponse dto = resultado.get();

            // Verifica informações do colaborador
            assertEquals(matriculaUUID, dto.matricula());
            assertEquals("Colaborador Com Entregas", dto.nome());

            // Verifica se a avaliação, de fato, veio nula
            assertNull(dto.avaliacaoComportamento(), "Avaliação no DTO deve ser nula.");

            // Verifica se a lista de entregas veio preenchida
            assertNotNull(dto.entregas(), "Lista de entregas no DTO não deve ser nula.");
            assertEquals(2, dto.entregas().size(), "Deve haver 2 entregas no DTO.");

            // Verifica detalhes da primeira entrega no DTO
            assertEquals(10L, dto.entregas().getFirst().id());
            assertEquals("Entrega Dez", dto.entregas().get(0).descricao());
            assertEquals(5.0, dto.entregas().getFirst().nota());

            // Verifica detalhes da segunda entrega no DTO
            assertEquals(11L, dto.entregas().get(1).id());
            assertEquals("Entrega Onze", dto.entregas().get(1).descricao());
            assertEquals(4.0, dto.entregas().get(1).nota());
        }
    }

    @Nested
    class listarColaboradores {

        @Test
        @DisplayName("Deve retornar lista vazia quando repositório não possuir registros")
        void deveRetornarListaVaziaQuandoRepositorioVazio() {
            // Arrange
            // Configura o Mock para retornar uma lista vazia em caso do BD não possuir colaboradores cadastrados
            when(colaboradorRepository.findAll()).thenReturn(Collections.emptyList());

            // Act
            // Executa o método listarColaboradores() para, de fato, testá-lo
            List<ColaboradorResponse> resultado = colaboradorService.listarColaboradores();

            // Assert
            // Verifica quantas vezes o método findAll() foi executado
            verify(colaboradorRepository, times(1)).findAll();

            //Verifica se a lista é nula ou se está vazia (correto)
            assertNotNull(resultado, "A lista não deve ser nula");
            assertTrue(resultado.isEmpty(), "A lista deve estar vazia");
        }

        @Test
        @DisplayName("Deve listar corretamente colaboradores com/sem avaliação e com/sem entregas")
        void deveListarCorretamenteColaboradoresCombinacoesDeDados() {
            // Arrange
            UUID m1 = UUID.randomUUID();
            UUID m2 = UUID.randomUUID();
            UUID m3 = UUID.randomUUID();

            // Simula um colaboradores encontrados e vindos do banco de dados
            // Com avaliação e sem entregas
            ColaboradorEntity c1 = new ColaboradorEntity(m1, "Alice", LocalDate.of(2024, 1, 1), "Engenheira");
            AvaliacaoComportamentoEntity av1 = new AvaliacaoComportamentoEntity();
            av1.setNotaAvaliacaoComportamental(5.0);
            av1.setNotaAprendizado(4.0);
            av1.setNotaTomadaDecisao(3.0);
            av1.setNotaAutonomia(5.0);
            c1.setAvaliacaoComportamento(av1);
            c1.setEntregas(Collections.emptyList());

            // Sem avaliação e com entregas
            ColaboradorEntity c2 = new ColaboradorEntity(m2, "Bruno", LocalDate.of(2024, 2, 2), "Analista");
            c2.setAvaliacaoComportamento(null);
            EntregaEntity e21 = new EntregaEntity();
            e21.setId(101L);
            e21.setDescricao("Entrega 101");
            e21.setNota(8.5);
            EntregaEntity e22 = new EntregaEntity();
            e22.setId(102L);
            e22.setDescricao("Entrega 102");
            e22.setNota(7.0);
            c2.setEntregas(List.of(e21, e22));

            // Com avaliação e com entregas
            ColaboradorEntity c3 = new ColaboradorEntity(m3, "Carla", LocalDate.of(2024, 3, 3), "Dev");
            AvaliacaoComportamentoEntity av3 = new AvaliacaoComportamentoEntity();
            av3.setNotaAvaliacaoComportamental(9.0);
            av3.setNotaAprendizado(8.0);
            av3.setNotaTomadaDecisao(7.0);
            av3.setNotaAutonomia(9.0);
            c3.setAvaliacaoComportamento(av3);
            EntregaEntity e31 = new EntregaEntity();
            e31.setId(201L);
            e31.setDescricao("Entrega 201");
            e31.setNota(10.0);
            c3.setEntregas(List.of(e31));

            // Configura o Mock para retornar uma lista de colaboradores a partir do método findAll()
            when(colaboradorRepository.findAll()).thenReturn(List.of(c1, c2, c3));

            // Act
            // Executa o método listarColaboradores() para, de fato, testá-lo
            List<ColaboradorResponse> resultado = colaboradorService.listarColaboradores();

            // Assert
            // Verifica se o método findAll() só foi executado uma única vez
            verify(colaboradorRepository, times(1)).findAll();

            // Verifica se o resultado é nulo
            assertNotNull(resultado);

            // Verifica se o retorno foi de 3 colaboradores
            assertEquals(3, resultado.size(), "A quantidade de itens deve refletir o repositório");

            // Valida os dados do colaborador c1 (avaliação presente, entregas vazias)
            ColaboradorResponse r1 = resultado.getFirst();
            assertEquals(m1, r1.matricula());
            assertEquals("Alice", r1.nome());
            assertNotNull(r1.avaliacaoComportamento(), "Avaliação deve existir");
            BigDecimal mediaEsperadaC1 = BigDecimal.valueOf((5.0 + 4.0 + 3.0 + 5.0) / 4.0).setScale(2, RoundingMode.HALF_UP);
            assertEquals(mediaEsperadaC1, r1.avaliacaoComportamento().mediaNotas());
            assertNotNull(r1.entregas());
            assertTrue(r1.entregas().isEmpty(), "Entregas devem estar vazias");

            // Valida os dados do colaborador c2 (avaliação nula, entregas preenchidas)
            ColaboradorResponse r2 = resultado.get(1);
            assertEquals(m2, r2.matricula());
            assertEquals("Bruno", r2.nome());
            assertNull(r2.avaliacaoComportamento(), "Avaliação deve ser nula");
            assertNotNull(r2.entregas());
            assertEquals(2, r2.entregas().size());
            assertEquals(101L, r2.entregas().getFirst().id());
            assertEquals("Entrega 101", r2.entregas().getFirst().descricao());
            assertEquals(8.5, r2.entregas().getFirst().nota());

            // Valida os dados do colaborador c3 (avaliação e entregas presentes)
            ColaboradorResponse r3 = resultado.get(2);
            assertEquals(m3, r3.matricula());
            assertEquals("Carla", r3.nome());
            assertNotNull(r3.avaliacaoComportamento());
            BigDecimal mediaEsperadaC3 = BigDecimal.valueOf((9.0 + 8.0 + 7.0 + 9.0) / 4.0).setScale(2, RoundingMode.HALF_UP);
            assertEquals(mediaEsperadaC3, r3.avaliacaoComportamento().mediaNotas());
            assertEquals(1, r3.entregas().size());
            assertEquals(201L, r3.entregas().getFirst().id());
            assertEquals("Entrega 201", r3.entregas().getFirst().descricao());
            assertEquals(10.0, r3.entregas().getFirst().nota());
        }
    }

    @Nested
    class deletarColaboradorPorMatriculaEntity {

        @Test
        @DisplayName("Deve deletar colaborador quando matrícula existir")
        void deveDeletarQuandoMatriculaExistir() {
            // Arrange
            UUID matricula = UUID.randomUUID();

            // Simula um colaborador com matrícula vindo do banco de dados
            var colaborador = new ColaboradorEntity();
            colaborador.setMatricula(matricula);

            // Configura o Mock para retornar um colaborador (ou vazio) ao executar o método findById()
            when(colaboradorRepository.findById(matricula)).thenReturn(Optional.of(colaborador));

            // Act
            // Executa o método deletarColaboradorPorMatricula() para, de fato, testá-lo
            colaboradorService.deletarColaboradorPorMatricula(matricula.toString());

            // Assert
            // Verifica se o método findById() só foi executado uma única vez
            verify(colaboradorRepository, times(1)).findById(matricula);

            // Verifica se o método delete() só foi executado uma única vez
            verify(colaboradorRepository, times(1)).delete(colaborador);

            // Garante que não houve mais nenhuma chamada a colaboradorRepository
            verifyNoMoreInteractions(colaboradorRepository);
        }

        @Test
        @DisplayName("Deve lançar ResourceNotFoundException quando matrícula não existir")
        void deveLancarResourceNotFoundQuandoMatriculaNaoExistir() {
            // Arrange
            UUID matricula = UUID.randomUUID();

            // Configura o Mock para retornar um vazio em caso da matrícula não existir no banco de dados
            when(colaboradorRepository.findById(matricula)).thenReturn(Optional.empty());

            // Act + Assert
            // Tenta deletar um colaborador passando uma matrícula inexistente e deve retornar uma ResourceNotFoundException
            assertThrows(
                    ResourceNotFoundException.class,
                    () -> colaboradorService.deletarColaboradorPorMatricula(matricula.toString())
            );

            // Verifica se o método findById() só foi executado uma única vez
            verify(colaboradorRepository, times(1)).findById(matricula);

            // Verifica se o método delete() de fato não foi executado
            verify(colaboradorRepository, never()).delete(any());

            // Garante que não houve mais nenhuma chamada a colaboradorRepository
            verifyNoMoreInteractions(colaboradorRepository);
        }

        @Test
        @DisplayName("Deve lançar IllegalArgumentException quando UUID for inválido")
        void deveLancarIllegalArgumentExceptionQuandoUuidInvalido() {
            // Arrange
            // Simula uma matrícula gerada erroneamente
            String uuidInvalido = "nao-e-um-uuid";

            // Act + Assert
            // Tenta deletar um colaborador passando uma matrícula incorreta e deve retornar uma IllegalArgumentException
            assertThrows(
                    IllegalArgumentException.class,
                    () -> colaboradorService.deletarColaboradorPorMatricula(uuidInvalido)
            );

            // Verifica se o método findById() de fato não foi executado
            verify(colaboradorRepository, never()).findById(any());

            // Verifica se o método delete() de fato não foi executado
            verify(colaboradorRepository, never()).delete(any());

            // Garante que não houve mais nenhuma chamada a colaboradorRepository
            verifyNoMoreInteractions(colaboradorRepository);
        }
    }

    @Nested
    class atualizaColaboradorPorMatriculaEntity {

        @Test
        @DisplayName("Deve atualizar nome e cargo quando ambos forem informados")
        void deveAtualizarNomeDataECargo() {
            // Arrange
            UUID matricula = UUID.randomUUID();

            // Simula um colaborador encontrado e vindo do banco de dados
            var colaboradorExistente = new ColaboradorEntity(matricula, "Antigo Nome", LocalDate.of(2024,1,1), "Antigo Cargo");

            // Configura o Mock para retornar um colaborador (ou vazio) ao executar o método findById()
            when(colaboradorRepository.findById(matricula)).thenReturn(Optional.of(colaboradorExistente));

            // Cria o DTO simulando o input do usuário
            var novaData = LocalDate.of(2024, 5, 10);
            var dto = new ColaboradorAtualizaRequest("Novo Nome", novaData, "Novo Cargo");

            // Verifica qual objeto foi passado para o método save()
            ArgumentCaptor<ColaboradorEntity> colaboradorPassadoSave = ArgumentCaptor.forClass(ColaboradorEntity.class);

            // Act
            // Executa o método atualizaColaboradorPorMatricula() para, de fato, testá-lo
            colaboradorService.atualizaColaboradorPorMatricula(matricula.toString(), dto);

            // Assert
            // Verifica se o método findById() do objeto colaboradorRepository foi chamado exatamente uma vez
            verify(colaboradorRepository, times(1)).findById(matricula);

            // Verifica se o método save() do objeto colaboradorRepository foi chamado exatamente uma vez
            verify(colaboradorRepository, times(1)).save(colaboradorPassadoSave.capture());

            // Verifica se o objeto colaborador salvo teve o nome e cargo atualizados
            ColaboradorEntity salvo = colaboradorPassadoSave.getValue();
            assertEquals("Novo Nome", salvo.getNome());
            assertEquals(novaData, salvo.getDataAdmissao());
            assertEquals("Novo Cargo", salvo.getCargo());
        }

        @Test
        @DisplayName("Deve atualizar apenas o nome quando cargo for nulo")
        void deveAtualizarApenasNome() {
            // Arrange
            UUID matricula = UUID.randomUUID();

            // Simula um colaborador encontrado e vindo do banco de dados
            var colaboradorExistente = new ColaboradorEntity(matricula, "Nome Antigo", LocalDate.of(2024,2,2), "Cargo Mantido");

            // Configura o Mock para retornar um colaborador (ou vazio) ao executar o método findById()
            when(colaboradorRepository.findById(matricula)).thenReturn(Optional.of(colaboradorExistente));

            // Cria o DTO simulando o input do usuário
            var dto = new ColaboradorAtualizaRequest("Nome Atualizado", null, null);

            // Verifica qual objeto foi passado para o método save()
            ArgumentCaptor<ColaboradorEntity> colaboradorPassadoSave = ArgumentCaptor.forClass(ColaboradorEntity.class);

            // Act
            // Executa o método atualizaColaboradorPorMatricula() para, de fato, testá-lo
            colaboradorService.atualizaColaboradorPorMatricula(matricula.toString(), dto);

            // Assert
            // Verifica se o método findById() do objeto colaboradorRepository foi chamado exatamente uma vez
            verify(colaboradorRepository).findById(matricula);

            // Verifica se o método save() do objeto colaboradorRepository foi chamado exatamente uma vez
            verify(colaboradorRepository).save(colaboradorPassadoSave.capture());

            // Verifica se o objeto colaborador salvo só teve o nome atualizado
            ColaboradorEntity salvo = colaboradorPassadoSave.getValue();
            assertEquals("Nome Atualizado", salvo.getNome());
            assertEquals(LocalDate.of(2024,2,2), salvo.getDataAdmissao());
            assertEquals("Cargo Mantido", salvo.getCargo());
        }

        @Test
        @DisplayName("Deve atualizar apenas o cargo quando nome for nulo")
        void deveAtualizarApenasCargo() {
            // Arrange
            UUID matricula = UUID.randomUUID();

            // Simula um colaborador encontrado e vindo do banco de dados
            var existente = new ColaboradorEntity(matricula, "Nome Mantido", LocalDate.of(2024,3,3), "Cargo Antigo");

            // Configura o Mock para retornar um colaborador (ou vazio) ao executar o método findById()
            when(colaboradorRepository.findById(matricula)).thenReturn(Optional.of(existente));

            // Cria o DTO simulando o input do usuário
            var dto = new ColaboradorAtualizaRequest(null, null, "Cargo Atualizado");

            // Verifica qual objeto foi passado para o método save()
            ArgumentCaptor<ColaboradorEntity> colaboradorPassadoSave = ArgumentCaptor.forClass(ColaboradorEntity.class);

            // Act
            // Executa o método atualizaColaboradorPorMatricula() para, de fato, testá-lo
            colaboradorService.atualizaColaboradorPorMatricula(matricula.toString(), dto);

            // Assert
            // Verifica se o método findById() do objeto colaboradorRepository foi chamado exatamente uma vez
            verify(colaboradorRepository).findById(matricula);

            // Verifica se o método save() do objeto colaboradorRepository foi chamado exatamente uma vez
            verify(colaboradorRepository).save(colaboradorPassadoSave.capture());

            // Verifica se o objeto colaborador salvo só teve o cargo atualizado
            ColaboradorEntity salvo = colaboradorPassadoSave.getValue();
            assertEquals("Nome Mantido", salvo.getNome());
            assertEquals(LocalDate.of(2024,3,3), salvo.getDataAdmissao());
            assertEquals("Cargo Atualizado", salvo.getCargo());
        }

        @Test
        @DisplayName("Deve atualizar apenas a dataAdmissao quando nome e cargo forem nulos")
        void deveAtualizarApenasDataAdmissao() {
            // Arrange
            UUID matricula = UUID.randomUUID();

            // Simula um colaborador encontrado e vindo do banco de dados
            var existente = new ColaboradorEntity(matricula, "Nome Mantido", LocalDate.of(2023,1,1), "Cargo Mantido");

            // Configura o Mock para retornar um colaborador (ou vazio) ao executar o método findById()
            when(colaboradorRepository.findById(matricula)).thenReturn(Optional.of(existente));

            // Cria o DTO simulando o input do usuário
            var novaData = LocalDate.of(2024, 12, 31);
            var dto = new ColaboradorAtualizaRequest(null, novaData, null);

            // Verifica qual objeto foi passado para o método save()
            ArgumentCaptor<ColaboradorEntity> captor = ArgumentCaptor.forClass(ColaboradorEntity.class);

            // Act
            // Executa o método atualizaColaboradorPorMatricula() para, de fato, testá-lo
            colaboradorService.atualizaColaboradorPorMatricula(matricula.toString(), dto);

            // Assert
            // Verifica se o método findById() do objeto colaboradorRepository foi chamado exatamente uma vez
            verify(colaboradorRepository).findById(matricula);

            // Verifica se o método save() do objeto colaboradorRepository foi chamado exatamente uma vez
            verify(colaboradorRepository).save(captor.capture());

            // Verifica se o objeto colaborador salvo só teve a data de admissão atualizada
            ColaboradorEntity salvo = captor.getValue();
            assertEquals("Nome Mantido", salvo.getNome());
            assertEquals(novaData, salvo.getDataAdmissao());
            assertEquals("Cargo Mantido", salvo.getCargo());
        }

        @Test
        @DisplayName("Deve manter dados quando ambos campos do DTO forem nulos")
        void deveManterDadosQuandoDtoNulo() {
            // Arrange
            UUID matricula = UUID.randomUUID();

            // Simula um colaborador encontrado e vindo do banco de dados
            var existente = new ColaboradorEntity(matricula, "Nome Original", LocalDate.of(2024,4,4), "Cargo Original");

            // Configura o Mock para retornar um colaborador (ou vazio) ao executar o método findById()
            when(colaboradorRepository.findById(matricula)).thenReturn(Optional.of(existente));

            // Cria o DTO simulando o input do usuário
            var dto = new ColaboradorAtualizaRequest(null, null, null);

            // Verifica qual objeto foi passado para o método save()
            ArgumentCaptor<ColaboradorEntity> colaboradorPassadoSave = ArgumentCaptor.forClass(ColaboradorEntity.class);

            // Act
            // Executa o método atualizaColaboradorPorMatricula() para, de fato, testá-lo
            colaboradorService.atualizaColaboradorPorMatricula(matricula.toString(), dto);

            // Assert
            // Verifica se o método findById() do objeto colaboradorRepository foi chamado exatamente uma vez
            verify(colaboradorRepository).findById(matricula);

            // Verifica se o método save() do objeto colaboradorRepository foi chamado exatamente uma vez
            verify(colaboradorRepository).save(colaboradorPassadoSave.capture());

            // Verifica se o objeto colaborador salvo não teve nada atualizado
            ColaboradorEntity salvo = colaboradorPassadoSave.getValue();
            assertEquals("Nome Original", salvo.getNome());
            assertEquals(LocalDate.of(2024,4,4), salvo.getDataAdmissao());
            assertEquals("Cargo Original", salvo.getCargo());
        }

        @Test
        @DisplayName("Deve lançar ResourceNotFoundException quando matrícula não existir")
        void deveLancarResourceNotFoundQuandoMatriculaNaoExistir() {
            // Arrange
            UUID matricula = UUID.randomUUID();

            // Configura o Mock para retornar um vazio em caso da matrícula não existir no banco de dados
            when(colaboradorRepository.findById(matricula)).thenReturn(Optional.empty());

            // Cria o DTO simulando o input do usuário
            var dto = new ColaboradorAtualizaRequest("Qualquer", LocalDate.now(), "Qualquer");

            // Act + Assert
            // Tenta atualizar um colaborador passando uma matrícula inexistente e deve retornar uma ResourceNotFoundException
            assertThrows(
                    ResourceNotFoundException.class,
                    () -> colaboradorService.atualizaColaboradorPorMatricula(matricula.toString(), dto)
            );

            // Verifica se o método findById() do objeto colaboradorRepository foi chamado exatamente uma vez
            verify(colaboradorRepository, times(1)).findById(matricula);

            // Verifica se o método save() de fato não foi executado
            verify(colaboradorRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar IllegalArgumentException quando UUID for inválido")
        void deveLancarIllegalArgumentExceptionQuandoUuidInvalido() {
            // Arrange
            // Cria uma matrícula inválida
            String uuidInvalido = "invalido";

            // Cria o DTO simulando o input do usuário
            var dto = new ColaboradorAtualizaRequest("Nome", LocalDate.now(), "Cargo");

            // Act + Assert
            // Tenta atualizar um colaborador passando uma matrícula incorreta e deve retornar uma IllegalArgumentException
            assertThrows(
                    IllegalArgumentException.class,
                    () -> colaboradorService.atualizaColaboradorPorMatricula(uuidInvalido, dto)
            );

            // Verifica se o método findById() do objeto colaboradorRepository foi chamado exatamente uma vez
            verify(colaboradorRepository, never()).findById(any());

            // Verifica se o método save() de fato não foi executado
            verify(colaboradorRepository, never()).save(any());
        }
    }

    @Nested
    class calcularPerformanceFinal {

        @Test
        @DisplayName("Deve calcular performance final com avaliação e pelo menos 2 entregas")
        void deveCalcularPerformanceFinalComSucesso() {
            // Arrange
            UUID matricula = UUID.randomUUID();

            // Simula um colaborador encontrado e vindo do banco de dados
            var colaborador = new ColaboradorEntity(matricula, "Alice", LocalDate.of(2024,1,1), "Engenheira");

            // Cria avaliação comportamental (todas as notas preenchidas)
            var avaliacao = new AvaliacaoComportamentoEntity();
            avaliacao.setNotaAvaliacaoComportamental(9.0);
            avaliacao.setNotaAprendizado(8.0);
            avaliacao.setNotaTomadaDecisao(7.0);
            avaliacao.setNotaAutonomia(9.0);
            colaborador.setAvaliacaoComportamento(avaliacao);

            // Cria duas entregas (regra mínima exigida)
            var entrega1 = new EntregaEntity();
            entrega1.setId(1L);
            entrega1.setDescricao("Entrega 1");
            entrega1.setNota(10.0);

            var entrega2 = new EntregaEntity();
            entrega2.setId(2L);
            entrega2.setDescricao("Entrega 2");
            entrega2.setNota(8.0);

            colaborador.setEntregas(List.of(entrega1, entrega2));

            // Configura o Mock para retornar o colaborador ao executar o método findById()
            when(colaboradorRepository.findById(matricula)).thenReturn(Optional.of(colaborador));

            // Calcula as médias esperadas (reproduzindo a mesma lógica da service)
            var mediaComportamentalEsperada = BigDecimal.valueOf(9.0)
                    .add(BigDecimal.valueOf(8.0))
                    .add(BigDecimal.valueOf(7.0))
                    .add(BigDecimal.valueOf(9.0))
                    .divide(new BigDecimal("4"), 2, RoundingMode.HALF_UP);
            var mediaEntregasEsperada = BigDecimal.valueOf(10.0)
                    .add(BigDecimal.valueOf(8.0))
                    .divide(new BigDecimal("2"), 2, RoundingMode.HALF_UP);
            var notaFinalEsperada = mediaComportamentalEsperada.add(mediaEntregasEsperada);

            // Act
            // Executa o método calcularPerformanceFinal() para, de fato, testá-lo
            var resultado = colaboradorService.calcularPerformanceFinal(matricula.toString());

            // Assert
            // Verifica se o método findById() foi chamado exatamente uma vez
            verify(colaboradorRepository, times(1)).findById(matricula);

            // Verifica se as informações retornadas estão corretas
            assertEquals(matricula, resultado.matricula());
            assertEquals("Alice", resultado.nome());
            assertEquals(mediaComportamentalEsperada, resultado.performance().mediaComportamental());
            assertEquals(mediaEntregasEsperada, resultado.performance().mediaEntregas());
            assertEquals(notaFinalEsperada, resultado.performance().notaFinal());
        }

        @Test
        @DisplayName("Deve lançar NegocioException quando avaliação comportamental não existir")
        void deveLancarNegocioExceptionQuandoSemAvaliacao() {
            // Arrange
            UUID matricula = UUID.randomUUID();

            // Simula um colaborador encontrado e vindo do banco de dados
            var colaborador = new ColaboradorEntity(matricula, "Bob", LocalDate.of(2024,2,2), "Analista");

            // Cria duas entregas válidas
            var entrega1 = new EntregaEntity();
            entrega1.setNota(7.0);
            var entrega2 = new EntregaEntity();
            entrega2.setNota(9.0);
            colaborador.setEntregas(List.of(entrega1, entrega2));

            // Remove a avaliação para forçar a exceção
            colaborador.setAvaliacaoComportamento(null);

            // Configura o Mock para retornar o colaborador
            when(colaboradorRepository.findById(matricula)).thenReturn(Optional.of(colaborador));

            // Act + Assert
            // Tenta calcular a performance sem avaliação e deve retornar uma NegocioException
            NegocioException ex = assertThrows(
                    NegocioException.class,
                    () -> colaboradorService.calcularPerformanceFinal(matricula.toString())
            );

            // Verifica a mensagem da exceção
            assertEquals("Avaliação comportamental não foi realizada.", ex.getMessage());

            // Verifica se o método findById() foi chamado exatamente uma vez
            verify(colaboradorRepository).findById(matricula);
        }

        @Test
        @DisplayName("Deve lançar NegocioException quando houver menos de 2 entregas cadastradas")
        void deveLancarNegocioExceptionQuandoMenosDeDuasEntregas() {
            // Arrange
            UUID matricula = UUID.randomUUID();

            // Simula um colaborador com avaliação válida
            var colaborador = new ColaboradorEntity(matricula, "Carol", LocalDate.of(2024,3,3), "Desenvolvedora");

            var avaliacao = new AvaliacaoComportamentoEntity();
            avaliacao.setNotaAvaliacaoComportamental(8.0);
            avaliacao.setNotaAprendizado(8.0);
            avaliacao.setNotaTomadaDecisao(8.0);
            avaliacao.setNotaAutonomia(8.0);
            colaborador.setAvaliacaoComportamento(avaliacao);

            // Cria apenas uma entrega (regra violada)
            var entrega1 = new EntregaEntity();
            entrega1.setNota(9.0);
            colaborador.setEntregas(List.of(entrega1));

            // Configura o Mock para retornar o colaborador
            when(colaboradorRepository.findById(matricula)).thenReturn(Optional.of(colaborador));

            // Act + Assert
            // Tenta calcular a performance com menos de 2 entregas e deve retornar uma NegocioException
            NegocioException ex = assertThrows(
                    NegocioException.class,
                    () -> colaboradorService.calcularPerformanceFinal(matricula.toString())
            );

            // Verifica a mensagem da exceção
            assertEquals("Colaborador deve ter no minimo 2 entregas cadastradas.", ex.getMessage());

            // Verifica se o método findById() foi chamado exatamente uma vez
            verify(colaboradorRepository).findById(matricula);
        }

        @Test
        @DisplayName("Deve lançar ResourceNotFoundException quando matrícula não existir no banco")
        void deveLancarResourceNotFoundQuandoMatriculaNaoExistir() {
            // Arrange
            UUID matricula = UUID.randomUUID();

            // Configura o Mock para retornar vazio ao executar o método findById()
            when(colaboradorRepository.findById(matricula)).thenReturn(Optional.empty());

            // Act + Assert
            // Tenta calcular a performance de uma matrícula inexistente e deve retornar uma ResourceNotFoundException
            assertThrows(
                    ResourceNotFoundException.class,
                    () -> colaboradorService.calcularPerformanceFinal(matricula.toString())
            );

            // Verifica se o método findById() foi chamado exatamente uma vez
            verify(colaboradorRepository).findById(matricula);
        }

        @Test
        @DisplayName("Deve lançar IllegalArgumentException quando UUID for inválido")
        void deveLancarIllegalArgumentExceptionQuandoUuidInvalido() {
            // Arrange
            // Cria uma matrícula inválida
            String uuidInvalido = "uuid-invalido";

            // Act + Assert
            // Tenta calcular performance passando uma matrícula inválida e deve retornar uma IllegalArgumentException
            assertThrows(
                    IllegalArgumentException.class,
                    () -> colaboradorService.calcularPerformanceFinal(uuidInvalido)
            );

            // Verifica se o método findById() não foi chamado
            verify(colaboradorRepository, never()).findById(any());
        }
    }
}