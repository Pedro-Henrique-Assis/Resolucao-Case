package com.example.demo.colaborador.entrega.resource;

import com.example.demo.colaborador.entrega.service.EntregaService;
import com.example.demo.colaborador.entrega.resource.json.EntregaAtualizaRequest;
import com.example.demo.colaborador.entrega.resource.json.EntregaCadastroRequest;
import com.example.demo.colaborador.entrega.resource.json.EntregaResponse;
import com.example.demo.colaborador.entrega.model.EntregaEntity;
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

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = EntregaResource.class)
class EntregaResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EntregaService entregaService;

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    class cadastrarEntregaColaboradorEntityEntity {

        @Test
        @DisplayName("Deve cadastrar e retornar 201 com Location e body DTO")
        void deveCadastrarCom201LocationEBody() throws Exception {
            // Arrange
            UUID matricula = UUID.randomUUID();

            // Cria o DTO simulando o input do usuário
            var dto = new EntregaCadastroRequest("Relatório Q1", 4.5);

            // Simula a entidade retornada pelo service
            var salvo = new EntregaEntity();
            salvo.setId(10L);
            salvo.setDescricao("Relatório Q1");
            salvo.setNota(4.5);

            // Configura o mock do service
            when(entregaService.cadastrarEntregaColaborador(eq(matricula.toString()), any(EntregaCadastroRequest.class)))
                    .thenReturn(salvo);

            // Captura dos argumentos repassados ao service
            ArgumentCaptor<String> matriculaCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<EntregaCadastroRequest> dtoCaptor = ArgumentCaptor.forClass(EntregaCadastroRequest.class);

            // Act
            // Executa POST /api/v1/colaborador/{matricula}/entrega
            mockMvc.perform(
                            post("/api/v1/colaborador/{matricula}/entrega", matricula)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(dto))
                    )
                    // Assert
                    // Verifica 201, Location e body com o DTO montado pela controller
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", "/api/v1/colaborador/" + matricula + "/entrega/10"))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id", is(10)))
                    .andExpect(jsonPath("$.descricao", is("Relatório Q1")))
                    .andExpect(jsonPath("$.nota", is(4.5)));

            // Verifica chamada ao service e argumentos
            verify(entregaService, times(1))
                    .cadastrarEntregaColaborador(matriculaCaptor.capture(), dtoCaptor.capture());
            org.junit.jupiter.api.Assertions.assertEquals(matricula.toString(), matriculaCaptor.getValue());
            org.junit.jupiter.api.Assertions.assertEquals("Relatório Q1", dtoCaptor.getValue().descricao());
            org.junit.jupiter.api.Assertions.assertEquals(4.5, dtoCaptor.getValue().nota());
        }
    }

    @Nested
    class listarEntregasPorColaboradorEntity {

        @Test
        @DisplayName("Deve retornar 200 com lista de entregas em JSON")
        void deveListarCom200() throws Exception {
            // Arrange
            UUID matricula = UUID.randomUUID();

            // Simula lista retornada pelo service (já em DTO)
            var e1 = new EntregaResponse(1L, "Entrega 1", 3.0);
            var e2 = new EntregaResponse(2L, "Entrega 2", 4.0);

            when(entregaService.listarEntregasPorColaborador(matricula.toString()))
                    .thenReturn(List.of(e1, e2));

            // Act
            // Executa GET /api/v1/colaborador/{matricula}/entrega
            mockMvc.perform(get("/api/v1/colaborador/{matricula}/entrega", matricula))
                    // Assert
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].id", is(1)))
                    .andExpect(jsonPath("$[0].descricao", is("Entrega 1")))
                    .andExpect(jsonPath("$[0].nota", is(3.0)))
                    .andExpect(jsonPath("$[1].id", is(2)))
                    .andExpect(jsonPath("$[1].descricao", is("Entrega 2")))
                    .andExpect(jsonPath("$[1].nota", is(4.0)));

            // Verifica chamada ao service
            verify(entregaService, times(1)).listarEntregasPorColaborador(matricula.toString());
        }
    }

    @Nested
    class consultarEntregaPorIdEntity {

        @Test
        @DisplayName("Deve retornar 200 com a entrega quando pertencer ao colaborador")
        void deveConsultarCom200() throws Exception {
            // Arrange
            UUID matricula = UUID.randomUUID();
            Long id = 77L;

            var dto = new EntregaResponse(id, "Entrega 77", 5.0);

            when(entregaService.consultarEntregaPorId(matricula.toString(), id))
                    .thenReturn(dto);

            // Act
            // Executa GET /api/v1/colaborador/{matricula}/entrega/{id}
            mockMvc.perform(get("/api/v1/colaborador/{matricula}/entrega/{id}", matricula, id))
                    // Assert
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id", is(77)))
                    .andExpect(jsonPath("$.descricao", is("Entrega 77")))
                    .andExpect(jsonPath("$.nota", is(5.0)));

            verify(entregaService, times(1)).consultarEntregaPorId(matricula.toString(), id);
        }
    }

    @Nested
    class atualizarEntregaPorIdEntity {

        @Test
        @DisplayName("Deve atualizar e retornar 204")
        void deveAtualizarCom204() throws Exception {
            // Arrange
            UUID matricula = UUID.randomUUID();
            Long id = 88L;

            // DTO simulando input do usuário
            var dto = new EntregaAtualizaRequest("Nova descrição", 4.5);

            // Captura dos argumentos enviados ao service
            ArgumentCaptor<String> matriculaCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
            ArgumentCaptor<EntregaAtualizaRequest> dtoCaptor = ArgumentCaptor.forClass(EntregaAtualizaRequest.class);

            doNothing().when(entregaService).atualizarEntregaPorId(anyString(), anyLong(), any(EntregaAtualizaRequest.class));

            // Act
            // Executa PUT /api/v1/colaborador/{matricula}/entrega/{id}
            mockMvc.perform(
                            patch("/api/v1/colaborador/{matricula}/entrega/{id}", matricula, id)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(dto))
                    )
                    // Assert
                    .andExpect(status().isNoContent());

            // Verifica que o service foi chamado com os argumentos corretos
            verify(entregaService, times(1))
                    .atualizarEntregaPorId(matriculaCaptor.capture(), idCaptor.capture(), dtoCaptor.capture());
            org.junit.jupiter.api.Assertions.assertEquals(matricula.toString(), matriculaCaptor.getValue());
            org.junit.jupiter.api.Assertions.assertEquals(id, idCaptor.getValue());
            org.junit.jupiter.api.Assertions.assertEquals("Nova descrição", dtoCaptor.getValue().descricao());
            org.junit.jupiter.api.Assertions.assertEquals(4.5, dtoCaptor.getValue().nota());
        }
    }

    @Nested
    class deletarEntregaColaboradorEntityEntity {

        @Test
        @DisplayName("Deve deletar e retornar 204")
        void deveDeletarCom204() throws Exception {
            // Arrange
            UUID matricula = UUID.randomUUID();
            Long id = 99L;

            doNothing().when(entregaService).deletarEntregaColaborador(matricula.toString(), id);

            // Act
            // Executa DELETE /api/v1/colaborador/{matricula}/entrega/{id}
            mockMvc.perform(delete("/api/v1/colaborador/{matricula}/entrega/{id}", matricula, id))
                    // Assert
                    .andExpect(status().isNoContent());

            // Verifica chamada ao service
            verify(entregaService, times(1)).deletarEntregaColaborador(matricula.toString(), id);
        }
    }
}