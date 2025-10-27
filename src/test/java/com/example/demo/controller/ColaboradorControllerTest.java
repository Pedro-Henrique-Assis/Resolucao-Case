package com.example.demo.controller;

import com.example.demo.business.services.ColaboradorService;
import com.example.demo.controller.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ColaboradorController.class)
class ColaboradorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ColaboradorService colaboradorService;

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    class cadastrarColaborador {

        @Test
        @DisplayName("Deve criar colaborador e retornar 201 com Location")
        void deveCriarColaborador() throws Exception {
            // Arrange
            UUID matriculaGerada = UUID.randomUUID();

            // Configura o Mock para retornar a matrícula criada pelo service
            when(colaboradorService.cadastrarColaborador(any(CadastroColaboradorDTO.class)))
                    .thenReturn(matriculaGerada);

            // Cria o DTO simulando o input do usuário
            var dto = new CadastroColaboradorDTO("Alice", LocalDate.of(2024,1,1), "Engenheira");

            // Captura do DTO enviado ao service
            ArgumentCaptor<CadastroColaboradorDTO> dtoCaptor = ArgumentCaptor.forClass(CadastroColaboradorDTO.class);

            // Act
            // Executa o POST /api/colaborador com o JSON do DTO
            mockMvc.perform(
                            post("/api/colaborador")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(dto))
                    )
                    // Assert
                    // Verifica status 201 e header Location correto
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", "/api/colaborador/" + matriculaGerada));

            // Verifica se o service foi chamado com o DTO correto
            verify(colaboradorService, times(1)).cadastrarColaborador(dtoCaptor.capture());
            CadastroColaboradorDTO capturado = dtoCaptor.getValue();
            org.junit.jupiter.api.Assertions.assertEquals("Alice", capturado.nome());
            org.junit.jupiter.api.Assertions.assertEquals(LocalDate.of(2024,1,1), capturado.dataAdmissao());
            org.junit.jupiter.api.Assertions.assertEquals("Engenheira", capturado.cargo());
        }
    }

    @Nested
    class consultarColaboradorPorMatricula {

        @Test
        @DisplayName("Deve retornar 200 e o DTO quando encontrado")
        void deveRetornar200QuandoEncontrado() throws Exception {
            // Arrange
            UUID matricula = UUID.randomUUID();

            // Monta um DTO de resposta simples (sem avaliação e sem entregas)
            var dto = new ColaboradorRespostaDTO(
                    matricula,
                    "Bob",
                    LocalDate.of(2024,2,2),
                    "Analista",
                    null,
                    List.of()
            );

            // Configura o Mock para retornar Optional.of(dto)
            when(colaboradorService.consultarColaboradorPorMatricula(matricula.toString()))
                    .thenReturn(Optional.of(dto));

            // Act
            // Executa GET /api/colaborador/{matricula}
            mockMvc.perform(get("/api/colaborador/{matricula}", matricula))
                    // Assert
                    // Verifica 200 e campos do JSON
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.matricula").value(matricula.toString()))
                    .andExpect(jsonPath("$.nome").value("Bob"))
                    .andExpect(jsonPath("$.cargo").value("Analista"))
                    .andExpect(jsonPath("$.entregas", hasSize(0)));

            // Verifica chamada ao service
            verify(colaboradorService, times(1)).consultarColaboradorPorMatricula(matricula.toString());
        }

        @Test
        @DisplayName("Deve retornar 404 quando não encontrado")
        void deveRetornar404QuandoNaoEncontrado() throws Exception {
            // Arrange
            UUID matricula = UUID.randomUUID();

            // Configura o Mock para retornar Optional.empty()
            when(colaboradorService.consultarColaboradorPorMatricula(matricula.toString()))
                    .thenReturn(Optional.empty());

            // Act + Assert
            mockMvc.perform(get("/api/colaborador/{matricula}", matricula))
                    .andExpect(status().isNotFound());

            verify(colaboradorService, times(1)).consultarColaboradorPorMatricula(matricula.toString());
        }
    }

    @Nested
    class listarColaboradores {

        @Test
        @DisplayName("Deve listar colaboradores e retornar 200")
        void deveListarCom200() throws Exception {
            // Arrange
            var c1 = new ColaboradorRespostaDTO(UUID.randomUUID(),"Ana", LocalDate.of(2024,1,1), "Dev", null, List.of());
            var c2 = new ColaboradorRespostaDTO(UUID.randomUUID(),"Caio", LocalDate.of(2024,1,2), "QA", null, List.of());

            // Configura o Mock para retornar a lista
            when(colaboradorService.listarColaboradores()).thenReturn(List.of(c1, c2));

            // Act + Assert
            mockMvc.perform(get("/api/colaborador"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].nome").value("Ana"))
                    .andExpect(jsonPath("$[1].nome").value("Caio"));

            verify(colaboradorService, times(1)).listarColaboradores();
        }
    }

    @Nested
    class calcularPerformanceFinal {

        @Test
        @DisplayName("Deve calcular performance e retornar 200 com JSON")
        void deveRetornar200ComPerformance() throws Exception {
            // Arrange
            UUID matricula = UUID.randomUUID();

            // Monta DTO de performance com médias
            var medias = new MediaPerformanceDTO(
                    new BigDecimal("8.25"),
                    new BigDecimal("9.00"),
                    new BigDecimal("17.25")
            );
            var perf = new PerformanceDTO(matricula, "Alice", medias);

            // Configura o Mock para retornar o DTO
            when(colaboradorService.calcularPerformanceFinal(matricula.toString())).thenReturn(perf);

            // Act + Assert
            mockMvc.perform(get("/api/colaborador/{matricula}/performance", matricula))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.matricula").value(matricula.toString()))
                    .andExpect(jsonPath("$.nome").value("Alice"))
                    .andExpect(jsonPath("$.performance.mediaComportamental").value(8.25))
                    .andExpect(jsonPath("$.performance.mediaEntregas").value(9.00))
                    .andExpect(jsonPath("$.performance.notaFinal").value(17.25));

            verify(colaboradorService, times(1)).calcularPerformanceFinal(matricula.toString());
        }
    }

    @Nested
    class deletarColaboradorPorMatricula {

        @Test
        @DisplayName("Deve deletar e retornar 204")
        void deveDeletarCom204() throws Exception {
            // Arrange
            UUID matricula = UUID.randomUUID();

            // Configura o Mock do service para ação void (nenhuma exceção)
            doNothing().when(colaboradorService).deletarColaboradorPorMatricula(matricula.toString());

            // Act + Assert
            mockMvc.perform(delete("/api/colaborador/{matricula}", matricula))
                    .andExpect(status().isNoContent());

            verify(colaboradorService, times(1)).deletarColaboradorPorMatricula(matricula.toString());
        }
    }

    @Nested
    class atualizarColaboradorPorMatricula {

        @Test
        @DisplayName("Deve atualizar e retornar 204")
        void deveAtualizarCom204() throws Exception {
            // Arrange
            UUID matricula = UUID.randomUUID();

            // Cria o DTO simulando o input do usuário
            var dto = new AtualizaColaboradorDTO("Novo Nome", "Novo Cargo");

            // Captura dos argumentos enviados ao service
            ArgumentCaptor<String> matriculaCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<AtualizaColaboradorDTO> dtoCaptor = ArgumentCaptor.forClass(AtualizaColaboradorDTO.class);

            doNothing().when(colaboradorService).atualizaColaboradorPorMatricula(anyString(), any(AtualizaColaboradorDTO.class));

            // Act
            // Executa PATCH /api/colaborador/{matricula} com JSON
            mockMvc.perform(
                            patch("/api/colaborador/{matricula}", matricula)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(dto))
                    )
                    // Assert
                    // Verifica 204 No Content
                    .andExpect(status().isNoContent());

            // Verifica que o service foi chamado com os parâmetros corretos
            verify(colaboradorService, times(1))
                    .atualizaColaboradorPorMatricula(matriculaCaptor.capture(), dtoCaptor.capture());

            org.junit.jupiter.api.Assertions.assertEquals(matricula.toString(), matriculaCaptor.getValue());
            org.junit.jupiter.api.Assertions.assertEquals("Novo Nome", dtoCaptor.getValue().nome());
            org.junit.jupiter.api.Assertions.assertEquals("Novo Cargo", dtoCaptor.getValue().cargo());
        }
    }
}