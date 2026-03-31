package com.fiap.agendamento_servico.domain.entities;

import com.fiap.agendamento_servico.domain.enums.StatusAgendamento;
import com.fiap.agendamento_servico.domain.exceptions.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Agendamento")
class AgendamentoTest {

    private static final UUID ID = UUID.randomUUID();
    private static final UUID CLIENTE_ID = UUID.randomUUID();
    private static final UUID PROFISSIONAL_ID = UUID.randomUUID();
    private static final UUID SERVICO_ID = UUID.randomUUID();
    private static final LocalDateTime INICIO = LocalDateTime.of(2026, 3, 25, 10, 0);
    private static final LocalDateTime FIM = LocalDateTime.of(2026, 3, 25, 10, 30);

    @Nested
    @DisplayName("Construtor")
    class Construtor {
        @Test
        @DisplayName("deve lançar exceção quando id é nulo")
        void deveLancarExcecaoQuandoIdENulo() {
            assertThrows(IllegalArgumentException.class, () -> new Agendamento(null, CLIENTE_ID, PROFISSIONAL_ID, SERVICO_ID, INICIO, FIM, StatusAgendamento.PENDENTE));
        }

        @Test
        @DisplayName("deve lançar exceção quando clienteId é nulo")
        void deveLancarExcecaoQuandoClienteIdENulo() {
            assertThrows(IllegalArgumentException.class, () -> new Agendamento(ID, null, PROFISSIONAL_ID, SERVICO_ID, INICIO, FIM, StatusAgendamento.PENDENTE));
        }

        @Test
        @DisplayName("deve lançar exceção quando profissionalId é nulo")
        void deveLancarExcecaoQuandoProfissionalIdENulo() {
            assertThrows(IllegalArgumentException.class, () -> new Agendamento(ID, CLIENTE_ID, null, SERVICO_ID, INICIO, FIM, StatusAgendamento.PENDENTE));
        }

        @Test
        @DisplayName("deve lançar exceção quando servicoId é nulo")
        void deveLancarExcecaoQuandoServicoIdENulo() {
            assertThrows(IllegalArgumentException.class, () -> new Agendamento(ID, CLIENTE_ID, PROFISSIONAL_ID, null, INICIO, FIM, StatusAgendamento.PENDENTE));
        }

        @Test
        @DisplayName("deve lançar exceção quando dataHoraInicio é nula")
        void deveLancarExcecaoQuandoDataHoraInicioENula() {
            assertThrows(IllegalArgumentException.class, () -> new Agendamento(ID, CLIENTE_ID, PROFISSIONAL_ID, SERVICO_ID, null, FIM, StatusAgendamento.PENDENTE));
        }

        @Test
        @DisplayName("deve lançar exceção quando dataHoraFim é nula")
        void deveLancarExcecaoQuandoDataHoraFimENula() {
            assertThrows(IllegalArgumentException.class, () -> new Agendamento(ID, CLIENTE_ID, PROFISSIONAL_ID, SERVICO_ID, INICIO, null, StatusAgendamento.PENDENTE));
        }

        @Test
        @DisplayName("deve lançar exceção quando inicio não é anterior ao fim")
        void deveLancarExcecaoQuandoInicioNaoENteriorAoFim() {
            assertThrows(IllegalArgumentException.class, () -> new Agendamento(ID, CLIENTE_ID, PROFISSIONAL_ID, SERVICO_ID, FIM, INICIO, StatusAgendamento.PENDENTE));
        }

        @Test
        @DisplayName("deve lançar exceção quando status é nulo")
        void deveLancarExcecaoQuandoStatusENulo() {
            assertThrows(IllegalArgumentException.class, () -> new Agendamento(ID, CLIENTE_ID, PROFISSIONAL_ID, SERVICO_ID, INICIO, FIM, null));
        }
    }

    @Nested
    @DisplayName("criar()")
    class Criar {
        @Test
        @DisplayName("deve criar agendamento com status PENDENTE e fim calculado")
        void deveCriarAgendamentoComStatusPendenteEFimCalculado() {
            Agendamento ag = Agendamento.criar(CLIENTE_ID, PROFISSIONAL_ID, SERVICO_ID, INICIO, 30);

            assertNotNull(ag.id());
            assertEquals(CLIENTE_ID, ag.clienteId());
            assertEquals(PROFISSIONAL_ID, ag.profissionalId());
            assertEquals(SERVICO_ID, ag.servicoId());
            assertEquals(INICIO, ag.dataHoraInicio());
            assertEquals(INICIO.plusMinutes(30), ag.dataHoraFim());
            assertEquals(StatusAgendamento.PENDENTE, ag.status());
        }

        @Test
        @DisplayName("deve lançar exceção quando duração é zero")
        void deveLancarExcecaoQuandoDuracaoEZero() {
            assertThrows(IllegalArgumentException.class, () -> Agendamento.criar(CLIENTE_ID, PROFISSIONAL_ID, SERVICO_ID, INICIO, 0));
        }

        @Test
        @DisplayName("deve lançar exceção quando duração é negativa")
        void deveLancarExcecaoQuandoDuracaoENegativa() {
            assertThrows(IllegalArgumentException.class,() -> Agendamento.criar(CLIENTE_ID, PROFISSIONAL_ID, SERVICO_ID, INICIO, -1));
        }
    }

    @Nested
    @DisplayName("confirmar()")
    class Confirmar {
        @Test
        @DisplayName("deve retornar agendamento com status CONFIRMADO")
        void deveRetornarAgendamentoComStatusConfirmado() {
            Agendamento ag = agendamentoPendente();
            Agendamento confirmado = ag.confirmar();

            assertEquals(StatusAgendamento.CONFIRMADO, confirmado.status());
            assertEquals(ag.id(), confirmado.id());
        }

        @Test
        @DisplayName("deve lançar exceção quando CONFIRMAR um agendamento cujo status não é PENDENTE")
        void deveLancarExcecaoQuandoConfirmarUmAgendamentoCujoStatusNaoEPendente() {
            Agendamento cancelado = agendamentoPendente().cancelar();
            
            assertThrows(BusinessException.class, cancelado::confirmar);
        }
    }

    @Nested
    @DisplayName("cancelar()")
    class Cancelar {
        @Test
        @DisplayName("deve retornar agendamento com status CANCELADO ao cancelar agendamento PENDENTE")
        void deveRetornarAgendamentoComStatusCanceladoAoCancelarAgendamentoPendente() {
            assertEquals(StatusAgendamento.CANCELADO, agendamentoPendente().cancelar().status());
        }

        @Test
        @DisplayName("deve retornar agendamento com status CANCELADO ao cancelar agendamento CONFIRMADO")
        void deveRetornarAgendamentoComStatusCanceladoAoCancelarAgendamentoConfirmado() {
            assertEquals(StatusAgendamento.CANCELADO, agendamentoPendente().confirmar().cancelar().status());
        }

        @Test
        @DisplayName("deve lançar exceção quando cancelar um agendamento que já está CANCELADO")
        void deveLancarExcecaoQuandoCancelarUmAgendamentoQueJaEstaCancelado() {
            Agendamento cancelado = agendamentoPendente().cancelar();
            
            assertThrows(BusinessException.class, cancelado::cancelar);
        }

        @Test
        @DisplayName("deve lançar exceção quando cancelar um agendamento que está CONCLUIDO")
        void deveLancarExcecaoQuandoCancelarUmAgendamentoQueEstaConcluido() {
            Agendamento concluido = agendamentoPendente().confirmar().concluir();
            
            assertThrows(BusinessException.class, concluido::cancelar);
        }
    }

    @Nested
    @DisplayName("concluir()")
    class Concluir {
        @Test
        @DisplayName("deve retornar agendamento com status CONCLUIDO ao concluir agendamento CONFIRMADO")
        void deveRetornarAgendamentoComStatusConcluido() {
            Agendamento agendamentoConfirmado = agendamentoPendente().confirmar();

            Agendamento agendamentoConcluido = agendamentoConfirmado.concluir();
            
            assertEquals(StatusAgendamento.CONCLUIDO, agendamentoConcluido.status());
        }

        @Test
        @DisplayName("deve lançar exceção ao CONCLUIR agendamento com status PENDENTE")
        void deveLancarExcecaoAoConcluirAgendamentoComStatusPendente() {
            assertThrows(BusinessException.class, () -> agendamentoPendente().concluir());
        }
    }

    @Nested
    @DisplayName("reagendar()")
    class Reagendar {
        @Test
        @DisplayName("deve retornar agendamento PENDENTE ao reagendar com novo horário")
        void deveRetornarAgendamentoPendenteAoReagendarComNovoHorario() {
            LocalDateTime novoInicio = INICIO.plusDays(1);
            Agendamento reagendado = agendamentoPendente().reagendar(novoInicio, 45);

            assertEquals(novoInicio, reagendado.dataHoraInicio());
            assertEquals(novoInicio.plusMinutes(45), reagendado.dataHoraFim());
            assertEquals(StatusAgendamento.PENDENTE, reagendado.status());
        }

        @Test
        @DisplayName("deve lançar exceção ao reagendar agendamento CANCELADO")
        void deveLancarExcecaoAoReagendarAgendamentoCancelado() {
            Agendamento cancelado = agendamentoPendente().cancelar();
            
            assertThrows(BusinessException.class, () -> cancelado.reagendar(INICIO.plusDays(1), 30));
        }

        @Test
        @DisplayName("deve lançar exceção ao reagendar agendamento CONCLUIDO")
        void deveLancarExcecaoAoReagendarAgendamentoConcluido() {
            Agendamento concluido = agendamentoPendente().confirmar().concluir();
            
            assertThrows(BusinessException.class, () -> concluido.reagendar(INICIO.plusDays(1), 30));
        }

        @Test
        @DisplayName("deve lançar exceção ao reagendar quando nova data é nula")
        void deveLancarExcecaoAoReagendarQuandoNovaDataENula() {
            assertThrows(IllegalArgumentException.class, () -> agendamentoPendente().reagendar(null, 30));
        }

        @Test
        @DisplayName("deve lançar exceção ao reagendar quando duração é zero")
        void deveLancarExcecaoAoReagendarQuandoDuracaoEZero() {
            assertThrows(IllegalArgumentException.class, () -> agendamentoPendente().reagendar(INICIO.plusDays(1), 0));
        }
    }

    private Agendamento agendamentoPendente() {
        return Agendamento.criar(CLIENTE_ID, PROFISSIONAL_ID, SERVICO_ID, INICIO, 30);
    }
}
