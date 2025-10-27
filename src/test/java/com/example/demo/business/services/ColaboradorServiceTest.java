package com.example.demo.business.services;

import com.example.demo.controller.dto.CadastroColaboradorDTO;
import com.example.demo.controller.dto.ColaboradorRespostaDTO;
import com.example.demo.infrastructure.model.Colaborador;
import com.example.demo.infrastructure.repository.ColaboradorRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ColaboradorServiceTest {

    //Arrange

    //Act

    //Assert

    @Mock
    private ColaboradorRepository colaboradorRepository;

    @InjectMocks
    private ColaboradorService colaboradorService;

    @Nested
    class cadastrarColaborador {

        @Test
        @DisplayName("Deve cadastrar um colaborador com sucesso")
        void deveCadastrarUmColaboradorComSucesso() {

            // Arrange
            var colaboradorDTO = new CadastroColaboradorDTO(
                    "Colaborador teste",
                    LocalDate.now(),
                    "Engenheiro"
            );

            // Objeto que deve ser salvo pelo método
            var colaboradorParaSalvar = new Colaborador();
            colaboradorParaSalvar.setNome(colaboradorDTO.nome());
            colaboradorParaSalvar.setDataAdmissao(colaboradorDTO.dataAdmissao());
            colaboradorParaSalvar.setCargo(colaboradorDTO.cargo());

            // Objeto que deve ser retornado pela service após salvar
            var matriculaEsperada = UUID.randomUUID();
            var colaboradorSalvo = new Colaborador();
            colaboradorSalvo.setMatricula(matriculaEsperada);
            colaboradorSalvo.setNome(colaboradorDTO.nome());
            colaboradorSalvo.setDataAdmissao(colaboradorDTO.dataAdmissao());
            colaboradorSalvo.setCargo(colaboradorDTO.cargo());

            // Configura o Mock para retornar o objeto 'colaboradorSalvo' quando o método save() for chamado
            // com qualquer objeto do tipo Colaborador
            when(colaboradorRepository.save(any(Colaborador.class))).thenReturn(colaboradorSalvo);

            // Verifica qual objeto foi passado para o método save()
            ArgumentCaptor<Colaborador> colaboradorPassadoSave = ArgumentCaptor.forClass(Colaborador.class);

            //Act
            // Chama o método save() para, de fato, testá-lo
            UUID matriculaRetornada = colaboradorService.cadastrarColaborador(colaboradorDTO);

            //Assert
            // Verifica se o método save() só foi chamado 1 vez
            verify(colaboradorRepository, times(1)).save(colaboradorPassadoSave.capture());

            // Captura o objeto Colaborador passado para o método save()
            Colaborador colaboradorPassadoParaSave = colaboradorPassadoSave.getValue();

            // Verifica se o colaborador passado para o save() tem os dados corretos do DTO
            assertNotNull(colaboradorPassadoParaSave, "O colaborador passado para save() não deve ser nulo");
            assertEquals(colaboradorDTO.nome(), colaboradorPassadoParaSave.getNome(), "O nome deve ser igual ao nome do DTO");
            assertEquals(colaboradorDTO.dataAdmissao(), colaboradorPassadoParaSave.getDataAdmissao(), "A data de admissão deve ser igual à do DTO");
            assertEquals(colaboradorDTO.cargo(), colaboradorPassadoParaSave.getCargo(), "O cargo deve ser igual ao do DTO");
            assertNull(colaboradorPassadoParaSave.getMatricula(), "A matrícula deve ser nula antes de salvar, pois é gerada pelo banco");

            // Verifica se a matrícula retornada pela service é a mesma simulada pelo repositório
            assertNotNull(matriculaRetornada, "A matrícula retornada não deve ser nula");
            assertEquals(matriculaEsperada, matriculaRetornada, "A matrícula retornada deve ser a mesma gerada pelo save");
        }

        @Test
        @DisplayName("Deve chamar o método save do repositório ao cadastrar")
        void deveCadastrarColaboradorUtilizandoSave() {

            //Arrange
            var colaboradorDTO = new CadastroColaboradorDTO(
                    "Colaborador teste 2",
                    LocalDate.now(),
                    "Analista"
            );

            // Simula o retorno do repositório
            var matriculaEsperada = UUID.randomUUID();
            var colaboradorSalvo = new Colaborador();
            colaboradorSalvo.setMatricula(matriculaEsperada);

            // Configura o Mock para retornar o objeto 'colaboradorSalvo' quando o método save() for chamado
            // com qualquer objeto do tipo Colaborador
            when(colaboradorRepository.save(any(Colaborador.class))).thenReturn(colaboradorSalvo);

            //Act
            // Chama o método cadastrarColaborador() para, de fato, testá-lo
            UUID matriculaRetornada = colaboradorService.cadastrarColaborador(colaboradorDTO);

            //Arrange
            // Verifica se o método save() só foi chamado 1 vez
            verify(colaboradorRepository, times(1)).save(any(Colaborador.class));

            assertEquals(matriculaEsperada, matriculaRetornada, "A matrícula retornada deve ser a mesma gerada pelo repositório");
        }
    }

    @Nested
    class consultarColaboradorPorMatricula {

        @Test
        @DisplayName("Deve retornar ColaboradorRespostaDTO quando a matrícula existir")
        void deveRetornarColaboradorRespostaDTOQuandoMatriculaExistir() {

            //Arrange
            UUID matriculaExistente = UUID.randomUUID();
            String matricula = matriculaExistente.toString();

            // Simulador um colaborador encontrado e vindo do banco de dados
            var colaboradorEncontrado = new Colaborador();
            colaboradorEncontrado.setMatricula(matriculaExistente);
            colaboradorEncontrado.setNome("Colaborador Encontrado");
            colaboradorEncontrado.setDataAdmissao(LocalDate.now());
            colaboradorEncontrado.setCargo("Engenheiro");

            // Configura o Mock para retornar um Optional com o colaborador encontrado (da base de dados, simuladamente)
            // sempre que o método findById() foi chamado
            when(colaboradorRepository.findById(matriculaExistente)).thenReturn(Optional.of(colaboradorEncontrado));

            //Act
            // Chama o método consultarColaboradorPorMatricula() para, de fato, testá-lo
            Optional<ColaboradorRespostaDTO> resultado = colaboradorService.consultarColaboradorPorMatricula(matricula);

            //Assert
            // Verifica se o método findById() só foi chamado 1 vez
            verify(colaboradorRepository, times(1)).findById(matriculaExistente);

            // Verifica se o resultado não está vazio
            assertTrue(resultado.isPresent(), "O resultado não deve ser vazio quando o colaborador é encontrado.");

            // Extrai o objeto ColaboradorRespostaDTO do resultado retornado
            ColaboradorRespostaDTO colaboradorRespostaDTO = resultado.get();

            // Verifica se os dados no DTO correspondem aos dados do colaborador encontrado
            assertEquals(matriculaExistente, colaboradorRespostaDTO.matricula(), "A matrícula no DTO deve ser a mesma do colaborador mockado.");
            assertEquals(colaboradorEncontrado.getNome(), colaboradorRespostaDTO.nome(), "O nome no DTO deve ser o mesmo do colaborador mockado.");
            assertEquals(colaboradorEncontrado.getDataAdmissao(), colaboradorRespostaDTO.dataAdmissao(), "A data de admissão no DTO deve ser a mesma.");
            assertEquals(colaboradorEncontrado.getCargo(), colaboradorRespostaDTO.cargo(), "O cargo no DTO deve ser o mesmo.");
        }
    }
}