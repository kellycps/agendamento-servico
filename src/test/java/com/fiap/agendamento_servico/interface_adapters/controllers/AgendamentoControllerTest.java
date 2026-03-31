package com.fiap.agendamento_servico.interface_adapters.controllers;

import com.fiap.agendamento_servico.application.dto.DetalhesAgendamentoDTO;
import com.fiap.agendamento_servico.application.dto.GradeEstabelecimentoDTO;
import com.fiap.agendamento_servico.application.dto.GradeHorariosDTO;
import com.fiap.agendamento_servico.application.ports.in.AgendarServicoUseCasePort;
import com.fiap.agendamento_servico.application.ports.in.ConsultarDisponibilidadeUseCasePort;
import com.fiap.agendamento_servico.application.ports.in.ConsultarGradeEstabelecimentoUseCasePort;
import com.fiap.agendamento_servico.domain.enums.StatusAgendamento;
import com.fiap.agendamento_servico.domain.exceptions.EntidadeNaoEncontradaException;
import com.fiap.agendamento_servico.domain.exceptions.HorarioIndisponivelException;
import com.fiap.agendamento_servico.infrastructure.config.ConfiguracaoJson;
import com.fiap.agendamento_servico.interface_adapters.presenters.AgendamentoPresenter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AgendamentoController.class)
@Import({AgendamentoPresenter.class, ConfiguracaoJson.class})
@DisplayName("AgendamentoController")
class AgendamentoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ConsultarDisponibilidadeUseCasePort consultarDisponibilidadeUseCasePort;

    @MockitoBean
    private ConsultarGradeEstabelecimentoUseCasePort consultarGradeEstabelecimentoUseCasePort;

    @MockitoBean
    private AgendarServicoUseCasePort agendarServicoUseCasePort;

    private static final UUID PROFISSIONAL_ID = UUID.randomUUID();
    private static final UUID ESTABELECIMENTO_ID = UUID.randomUUID();
    private static final UUID CLIENTE_ID = UUID.randomUUID();
    private static final UUID SERVICO_ID = UUID.randomUUID();
    private static final LocalDate DATA = LocalDate.of(2026, 3, 25);
    private static final LocalDateTime DATA_HORA = LocalDateTime.of(2026, 3, 25, 10, 0);

    @BeforeEach
    void resetMocks() {
        Mockito.reset(consultarDisponibilidadeUseCasePort,
                consultarGradeEstabelecimentoUseCasePort,
                agendarServicoUseCasePort);
    }

    @Nested
    @DisplayName("GET /disponibilidade")
    class ConsultarDisponibilidade {
        @Test
        @DisplayName("deve retornar 200 com a grade de horários do profissional")
        void deveRetornar200ComGradeDeHorariosDoProfissional() throws Exception {
            GradeHorariosDTO gradeDTO = new GradeHorariosDTO(DATA, "Dr. Teste", List.of());
            
            when(consultarDisponibilidadeUseCasePort.executar(eq(PROFISSIONAL_ID), eq(DATA))).thenReturn(gradeDTO);

            mockMvc.perform(get("/agendamento/disponibilidade")
                            .param("profissionalId", PROFISSIONAL_ID.toString())
                            .param("data", "2026-03-25"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.sucesso").value(true))
                    .andExpect(jsonPath("$.dados.profissional").value("Dr. Teste"))
                    .andExpect(jsonPath("$.dados.data").value("25/03/2026"));
        }
    }

    @Nested
    @DisplayName("GET /grade/estabelecimento")
    class GradeEstabelecimento {

        @Test
        @DisplayName("deve retornar 200 com a grade completa do estabelecimento")
        void deveRetornar200ComGradeCompletaDoEstabelecimento() throws Exception {
            GradeEstabelecimentoDTO gradeDTO = new GradeEstabelecimentoDTO(DATA, List.of());
            
            when(consultarGradeEstabelecimentoUseCasePort.consultarGradeCompleta(eq(ESTABELECIMENTO_ID), eq(DATA))).thenReturn(gradeDTO);

            mockMvc.perform(get("/agendamento/grade/estabelecimento")
                            .param("estabelecimentoId", ESTABELECIMENTO_ID.toString())
                            .param("data", "2026-03-25"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.sucesso").value(true))
                    .andExpect(jsonPath("$.dados.data").value("25/03/2026"));
        }
    }

    @Nested
    @DisplayName("GET /grade/disponivel")
    class GradeDisponivel {
        @Test
        @DisplayName("deve retornar 200 com os horários disponíveis do estabelecimento")
        void deveRetornar200ComHorariosDisponiveisDoEstabelecimento() throws Exception {
            GradeEstabelecimentoDTO gradeDTO = new GradeEstabelecimentoDTO(DATA, List.of());
            
            when(consultarGradeEstabelecimentoUseCasePort.consultarGradeDisponivel(eq(ESTABELECIMENTO_ID), eq(DATA))).thenReturn(gradeDTO);

            mockMvc.perform(get("/agendamento/grade/disponivel")
                            .param("estabelecimentoId", ESTABELECIMENTO_ID.toString())
                            .param("data", "2026-03-25"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.sucesso").value(true))
                    .andExpect(jsonPath("$.dados.data").value("25/03/2026"));
        }
    }

    @Nested
    @DisplayName("GET /grade/servico")
    class GradePorServico {
        @Test
        @DisplayName("deve retornar 200 com a grade filtrada por serviço")
        void deveRetornar200ComGradeFiltradaPorServico() throws Exception {
            GradeEstabelecimentoDTO gradeDTO = new GradeEstabelecimentoDTO(DATA, List.of());
            when(consultarGradeEstabelecimentoUseCasePort.consultarGradePorServico(
                    eq(ESTABELECIMENTO_ID), eq(DATA), eq(SERVICO_ID)))
                    .thenReturn(gradeDTO);

            mockMvc.perform(get("/agendamento/grade/servico")
                            .param("estabelecimentoId", ESTABELECIMENTO_ID.toString())
                            .param("data", "2026-03-25")
                            .param("servicoId", SERVICO_ID.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.sucesso").value(true))
                    .andExpect(jsonPath("$.dados.data").value("25/03/2026"));
        }
    }

    @Nested
    @DisplayName("POST /reservar")
    class Reservar {

        @Test
        @DisplayName("deve retornar 201 com agendamento PENDENTE quando reserva é criada")
        void deveRetornar201ComAgendamentoPendenteQuandoReservaEhCriada() throws Exception {
            DetalhesAgendamentoDTO dto = new DetalhesAgendamentoDTO(
                    UUID.randomUUID(), "Cliente Teste", "Dr. Teste", "Corte de Cabelo",
                    DATA_HORA, DATA_HORA.plusMinutes(30), StatusAgendamento.PENDENTE);
            
            when(agendarServicoUseCasePort.executar(any())).thenReturn(dto);

            String body = """
                    {
                      "clienteId": "%s",
                      "profissionalId": "%s",
                      "servicoId": "%s",
                      "dataHoraInicio": "2026-03-25T10:00:00"
                    }
                    """.formatted(CLIENTE_ID, PROFISSIONAL_ID, SERVICO_ID);

            mockMvc.perform(post("/agendamento/reservar")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.sucesso").value(true))
                    .andExpect(jsonPath("$.dados.status").value("PENDENTE"))
                    .andExpect(jsonPath("$.dados.profissional").value("Dr. Teste"));
        }

        @Test
        @DisplayName("deve retornar 404 quando profissional, cliente ou serviço não existe")
        void deveRetornar404QuandoProfissionalClienteOuServicoNaoExiste() throws Exception {
            when(agendarServicoUseCasePort.executar(any())).thenThrow(EntidadeNaoEncontradaException.para("Profissional", PROFISSIONAL_ID));

            String body = """
                    {
                      "clienteId": "%s",
                      "profissionalId": "%s",
                      "servicoId": "%s",
                      "dataHoraInicio": "2026-03-25T10:00:00"
                    }
                    """.formatted(CLIENTE_ID, PROFISSIONAL_ID, SERVICO_ID);

            mockMvc.perform(post("/agendamento/reservar")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.sucesso").value(false));
        }

        @Test
        @DisplayName("deve retornar 422 quando o horário solicitado não está disponível")
        void deveRetornar422QuandoHorarioSolicitadoNaoEstaDisponivel() throws Exception {
            when(agendarServicoUseCasePort.executar(any())).thenThrow(new HorarioIndisponivelException("Horário já está ocupado"));

            String body = """
                    {
                      "clienteId": "%s",
                      "profissionalId": "%s",
                      "servicoId": "%s",
                      "dataHoraInicio": "2026-03-25T10:00:00"
                    }
                    """.formatted(CLIENTE_ID, PROFISSIONAL_ID, SERVICO_ID);

            mockMvc.perform(post("/agendamento/reservar")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isUnprocessableContent())
                    .andExpect(jsonPath("$.sucesso").value(false))
                    .andExpect(jsonPath("$.mensagem").value("Horário já está ocupado"));
        }
    }
}
