package com.fiap.agendamento_servico.interface_adapters.controllers;

import com.fiap.agendamento_servico.application.dto.DetalhesAgendamentoDTO;
import com.fiap.agendamento_servico.application.ports.in.GerenciarAgendamentoUseCasePort;
import com.fiap.agendamento_servico.domain.enums.StatusAgendamento;
import com.fiap.agendamento_servico.domain.exceptions.BusinessException;
import com.fiap.agendamento_servico.domain.exceptions.EntidadeNaoEncontradaException;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GerenciamentoAgendamentoController.class)
@Import({AgendamentoPresenter.class, ConfiguracaoJson.class})
@DisplayName("GerenciamentoAgendamentoController")
class GerenciamentoAgendamentoControllerTest {

    @Autowired 
    private MockMvc mockMvc;

    @MockitoBean private GerenciarAgendamentoUseCasePort gerenciarAgendamentoUseCasePort;

    private static final UUID AGENDAMENTO_ID  = UUID.randomUUID();
    private static final UUID CLIENTE_ID = UUID.randomUUID();
    private static final LocalDateTime DATA_HORA = LocalDateTime.of(2026, 3, 25, 10, 0);

    @BeforeEach
    void resetMocks() {
        Mockito.reset(gerenciarAgendamentoUseCasePort);
    }

    @Nested
    @DisplayName("PATCH /confirmar/{id}")
    class Confirmar {
        @Test
        @DisplayName("deve retornar 200 com status CONFIRMADO")
        void deveRetornar200ComStatusConfirmado() throws Exception {
            doNothing().when(gerenciarAgendamentoUseCasePort).confirmar(AGENDAMENTO_ID);

            mockMvc.perform(patch("/gerenciamento/agendamento/confirmar/{id}", AGENDAMENTO_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.sucesso").value(true))
                    .andExpect(jsonPath("$.dados.status").value("CONFIRMADO"));
        }

        @Test
        @DisplayName("deve retornar 404 quando agendamento não existe")
        void deveRetornar404QuandoAgendamentoNaoExiste() throws Exception {
            doThrow(EntidadeNaoEncontradaException.para("Agendamento", AGENDAMENTO_ID)).when(gerenciarAgendamentoUseCasePort).confirmar(AGENDAMENTO_ID);

            mockMvc.perform(patch("/gerenciamento/agendamento/confirmar/{id}", AGENDAMENTO_ID))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.sucesso").value(false));
        }

        @Test
        @DisplayName("deve retornar 400 quando regra de negócio viola status")
        void deveRetornar400QuandoRegraDeNegocioViolaStatus() throws Exception {
            doThrow(new BusinessException("Agendamento não está pendente")).when(gerenciarAgendamentoUseCasePort).confirmar(AGENDAMENTO_ID);

            mockMvc.perform(patch("/gerenciamento/agendamento/confirmar/{id}", AGENDAMENTO_ID))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.sucesso").value(false));
        }
    }

    @Nested
    @DisplayName("PATCH /cancelar/{id}")
    class Cancelar {
        @Test
        @DisplayName("deve retornar 200 com status CANCELADO")
        void deveRetornar200ComStatusCancelado() throws Exception {
            doNothing().when(gerenciarAgendamentoUseCasePort).cancelar(AGENDAMENTO_ID);

            mockMvc.perform(patch("/gerenciamento/agendamento/cancelar/{id}", AGENDAMENTO_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.dados.status").value("CANCELADO"));
        }

        @Test
        @DisplayName("deve retornar 404 quando agendamento não existe")
        void deveRetornar404QuandoAgendamentoNaoExiste() throws Exception {
            doThrow(EntidadeNaoEncontradaException.para("Agendamento", AGENDAMENTO_ID)).when(gerenciarAgendamentoUseCasePort).cancelar(AGENDAMENTO_ID);

            mockMvc.perform(patch("/gerenciamento/agendamento/cancelar/{id}", AGENDAMENTO_ID)).andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PATCH /concluir/{id}")
    class Concluir {
        @Test
        @DisplayName("deve retornar 200 com status CONCLUIDO")
        void deveRetornar200ComStatusConcluido() throws Exception {
            doNothing().when(gerenciarAgendamentoUseCasePort).concluir(AGENDAMENTO_ID);

            mockMvc.perform(patch("/gerenciamento/agendamento/concluir/{id}", AGENDAMENTO_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.dados.status").value("CONCLUIDO"));
        }
    }

    @Nested
    @DisplayName("PATCH /reagendar/{id}")
    class Reagendar {
        @Test
        @DisplayName("deve retornar 200 com novaDataHoraInicio")
        void deveRetornar200ComNovaDataHoraInicio() throws Exception {
            doNothing().when(gerenciarAgendamentoUseCasePort).reagendar(any());

            mockMvc.perform(patch("/gerenciamento/agendamento/reagendar/{id}", AGENDAMENTO_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"novaDataHoraInicio\":\"2026-03-26T10:00:00\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.sucesso").value(true));
        }
    }

    @Nested
    @DisplayName("GET /cliente/{clienteId}")
    class ListarPorCliente {
        @Test
        @DisplayName("deve retornar 200 com lista de agendamentos")
        void deveRetornar200ComListaDeAgendamentos() throws Exception {
            DetalhesAgendamentoDTO agDto = new DetalhesAgendamentoDTO(
                    AGENDAMENTO_ID, "Ana Lima", "Dr. Teste", "Corte",
                    DATA_HORA, DATA_HORA.plusMinutes(30), StatusAgendamento.CONFIRMADO);

            when(gerenciarAgendamentoUseCasePort.listarPorCliente(CLIENTE_ID)).thenReturn(List.of(agDto));

            mockMvc.perform(get("/gerenciamento/agendamento/cliente/{clienteId}", CLIENTE_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.dados[0].servico").value("Corte"));
        }

        @Test
        @DisplayName("deve retornar 404 quando cliente não encontrado")
        void deveRetornar404QuandoClienteNaoEncontrado() throws Exception {
            when(gerenciarAgendamentoUseCasePort.listarPorCliente(CLIENTE_ID)).thenThrow(EntidadeNaoEncontradaException.para("Cliente", CLIENTE_ID));

            mockMvc.perform(get("/gerenciamento/agendamento/cliente/{clienteId}", CLIENTE_ID)).andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /deletar/{id}")
    class Deletar {
        @Test
        @DisplayName("deve retornar 204 ao deletar agendamento")
        void deveRetornar204AoDeletarAgendamento() throws Exception {
            doNothing().when(gerenciarAgendamentoUseCasePort).deletar(AGENDAMENTO_ID);

            mockMvc.perform(delete("/gerenciamento/agendamento/deletar/{id}", AGENDAMENTO_ID)).andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("deve retornar 400 ao tentar deletar agendamento CONCLUIDO")
        void deveRetornar400AoTentarDeletarAgendamentoConcluido() throws Exception {
            doThrow(new BusinessException("Agendamentos concluídos não podem ser deletados.")).when(gerenciarAgendamentoUseCasePort).deletar(AGENDAMENTO_ID);

            mockMvc.perform(delete("/gerenciamento/agendamento/deletar/{id}", AGENDAMENTO_ID)).andExpect(status().isBadRequest());
        }
    }
}
