package com.example.demo.controller;

import com.example.demo.business.services.AvaliacaoComportamentoService;
import com.example.demo.controller.dto.AtualizaAvaliacaoComportamentoDTO;
import com.example.demo.controller.dto.AvaliacaoComportamentoRespostaDTO;
import com.example.demo.controller.dto.CadastroAvaliacaoComportamentoDTO;
import com.example.demo.controller.dto.DetalhesAvaliacaoComportamentoDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AvaliacaoComportamentoController.class)
class AvaliacaoComportamentoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AvaliacaoComportamentoService avaliacaoService;

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    class cadastrarAvaliacaoComportamental {

        @Test
        @DisplayName("Deve cadastrar avaliação e retornar 201 com Location")
        void deveCadastrarERetornar201() throws Exception {
            // Arrange
            UUID matricula = UUID.randomUUID();
            Long idGerado = 123L;

            // Configura o mock do service para retornar o ID criado
            when(avaliacaoService.cadastrarAvaliacaoComportamental(any(), any(CadastroAvaliacaoComportamentoDTO.class)))
                    .thenReturn(idGerado);

            // Cria o DTO simulando o input do usuário
            var dto = new CadastroAvaliacaoComportamentoDTO(5.0, 4.0, 3.0, 5.0);

            // Captura dos argumentos enviados ao service
            ArgumentCaptor<String> matriculaCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<CadastroAvaliacaoComportamentoDTO> dtoCaptor = ArgumentCaptor.forClass(CadastroAvaliacaoComportamentoDTO.class);

            // Act
            // Executa POST /api/colaborador/{matricula}/avaliacao
            mockMvc.perform(
                            post("/api/colaborador/{matricula}/avaliacao", matricula)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(dto))
                    )
                    // Assert
                    // Verifica 201 e header Location no padrão da controller
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", "/api/colaborador/" + matricula + "/avaliacao/" + idGerado));

            // Verifica a chamada ao service e os argumentos passados
            verify(avaliacaoService, times(1))
                    .cadastrarAvaliacaoComportamental(matriculaCaptor.capture(), dtoCaptor.capture());
            org.junit.jupiter.api.Assertions.assertEquals(matricula.toString(), matriculaCaptor.getValue());
            org.junit.jupiter.api.Assertions.assertEquals(5.0, dtoCaptor.getValue().notaAvaliacaoComportamental());
            org.junit.jupiter.api.Assertions.assertEquals(4.0, dtoCaptor.getValue().notaAprendizado());
            org.junit.jupiter.api.Assertions.assertEquals(3.0, dtoCaptor.getValue().notaTomadaDecisao());
            org.junit.jupiter.api.Assertions.assertEquals(5.0, dtoCaptor.getValue().notaAutonomia());
        }
    }

    @Nested
    class consultaAvaliacaoPorMatricula {

        @Test
        @DisplayName("Deve retornar 200 e JSON com notas e média")
        void deveRetornar200ComJson() throws Exception {
            // Arrange
            UUID matricula = UUID.randomUUID();

            // Monta DTO de resposta esperado pela controller
            var detalhes = new DetalhesAvaliacaoComportamentoDTO(
                    5.0, 4.0, 3.0, 5.0, new BigDecimal("4.25")
            );
            var resposta = new AvaliacaoComportamentoRespostaDTO(detalhes);

            // Configura o mock do service para retornar o DTO
            when(avaliacaoService.consultaAvaliacaoPorMatricula(matricula.toString()))
                    .thenReturn(resposta);

            // Act
            // Executa GET /api/colaborador/{matricula}/avaliacao
            mockMvc.perform(get("/api/colaborador/{matricula}/avaliacao", matricula))
                    // Assert
                    // Verifica 200 e os campos do JSON aninhados
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.avaliacaoComportamento.notaAvaliacaoComportamental", is(5.0)))
                    .andExpect(jsonPath("$.avaliacaoComportamento.notaAprendizado", is(4.0)))
                    .andExpect(jsonPath("$.avaliacaoComportamento.notaTomadaDecisao", is(3.0)))
                    .andExpect(jsonPath("$.avaliacaoComportamento.notaAutonomia", is(5.0)))
                    .andExpect(jsonPath("$.avaliacaoComportamento.mediaNotas", is(4.25)));

            // Verifica chamada ao service
            verify(avaliacaoService, times(1)).consultaAvaliacaoPorMatricula(matricula.toString());
        }
    }

    @Nested
    class deletarAvaliacoesPorMatricula {

        @Test
        @DisplayName("Deve deletar avaliações e retornar 204")
        void deveDeletarCom204() throws Exception {
            // Arrange
            UUID matricula = UUID.randomUUID();

            // Configura void method no mock
            doNothing().when(avaliacaoService).deletarAvaliacoesPorMatricula(matricula.toString());

            // Act
            // Executa DELETE /api/colaborador/{matricula}/avaliacao
            mockMvc.perform(delete("/api/colaborador/{matricula}/avaliacao", matricula))
                    // Assert
                    // Verifica 204 No Content
                    .andExpect(status().isNoContent());

            // Verifica chamada ao service
            verify(avaliacaoService, times(1)).deletarAvaliacoesPorMatricula(matricula.toString());
        }
    }

    @Nested
    class atualizaAvaliacaoPorMarticula {

        @Test
        @DisplayName("Deve atualizar notas e retornar 204")
        void deveAtualizarCom204() throws Exception {
            // Arrange
            UUID matricula = UUID.randomUUID();

            // DTO com atualização (pode ser parcial ou total)
            var dto = new AtualizaAvaliacaoComportamentoDTO(5.0, 4.0, 3.5, 4.5);

            // Captura dos argumentos enviados ao service
            ArgumentCaptor<String> matriculaCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<AtualizaAvaliacaoComportamentoDTO> dtoCaptor = ArgumentCaptor.forClass(AtualizaAvaliacaoComportamentoDTO.class);

            doNothing().when(avaliacaoService).atualizaAvaliacaoPorMarticula(anyString(), any(AtualizaAvaliacaoComportamentoDTO.class));

            // Act
            // Executa PUT /api/colaborador/{matricula}/avaliacao
            mockMvc.perform(
                            put("/api/colaborador/{matricula}/avaliacao", matricula)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(dto))
                    )
                    // Assert
                    // Verifica 204 No Content
                    .andExpect(status().isNoContent());

            // Verifica args repassados ao service
            verify(avaliacaoService, times(1))
                    .atualizaAvaliacaoPorMarticula(matriculaCaptor.capture(), dtoCaptor.capture());
            org.junit.jupiter.api.Assertions.assertEquals(matricula.toString(), matriculaCaptor.getValue());
            org.junit.jupiter.api.Assertions.assertEquals(5.0, dtoCaptor.getValue().notaAvaliacaoComportamental());
            org.junit.jupiter.api.Assertions.assertEquals(4.0, dtoCaptor.getValue().notaAprendizado());
            org.junit.jupiter.api.Assertions.assertEquals(3.5, dtoCaptor.getValue().notaTomadaDecisao());
            org.junit.jupiter.api.Assertions.assertEquals(4.5, dtoCaptor.getValue().notaAutonomia());
        }
    }
}