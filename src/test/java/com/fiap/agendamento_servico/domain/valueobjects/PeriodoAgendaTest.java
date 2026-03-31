package com.fiap.agendamento_servico.domain.valueobjects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PeriodoAgenda")
class PeriodoAgendaTest {

    @Nested
    @DisplayName("Construtor")
    class Construtor {
        @Test
        @DisplayName("deve lançar exceção quando horaInicio é nula")
        void deveLancarExcecaoQuandoHoraInicioENula() {
            assertThrows(IllegalArgumentException.class, () -> new PeriodoAgenda(null, LocalTime.of(10, 0), true, null));
        }

        @Test
        @DisplayName("deve lançar exceção quando horaFim é nula")
        void deveLancarExcecaoQuandoHoraFimENula() {
            assertThrows(IllegalArgumentException.class, () -> new PeriodoAgenda(LocalTime.of(9, 0), null, true, null));
        }

        @Test
        @DisplayName("deve lançar exceção quando inicio não é anterior ao fim")
        void deveLancarExcecaoQuandoInicioNaoENteriorAoFim() {
            assertThrows(IllegalArgumentException.class, () -> new PeriodoAgenda(LocalTime.of(10, 0), LocalTime.of(9, 0), true, null));
        }

        @Test
        @DisplayName("deve lançar exceção quando inicio é igual ao fim")
        void deveLancarExcecaoQuandoInicioIgualAoFim() {
            assertThrows(IllegalArgumentException.class, () -> new PeriodoAgenda(LocalTime.of(9, 0), LocalTime.of(9, 0), true, null));
        }

        @Test
        @DisplayName("deve criar período válido com quaisquer status")
        void deveCriarPeriodoValidoComQuaisquerStatus() {
            assertDoesNotThrow(() -> new PeriodoAgenda(LocalTime.of(9, 0), LocalTime.of(10, 0), false, null));
        }
    }

    @Nested
    @DisplayName("contemHorario()")
    class ContemHorario {
        private final PeriodoAgenda periodo = new PeriodoAgenda(
                LocalTime.of(9, 0), LocalTime.of(11, 0), true, null);

        @Test
        @DisplayName("deve retornar true quando janela contém exatamente o período")
        void deveRetornarTrueQuandoJanelaContemExatamenteOPeriodo() {
            assertTrue(periodo.contemHorario(LocalTime.of(9, 0), LocalTime.of(11, 0)));
        }

        @Test
        @DisplayName("deve retornar true quando período está dentro da janela maior")
        void deveRetornarTrueQuandoPeriodoEstaDentroDaJanelaMaior() {
            assertTrue(periodo.contemHorario(LocalTime.of(8, 0), LocalTime.of(12, 0)));
        }

        @Test
        @DisplayName("deve retornar false quando janela começa depois do início do período")
        void deveRetornarFalseQuandoJanelaComecaDepoisDoInicioDoPeriodo() {
            assertFalse(periodo.contemHorario(LocalTime.of(9, 30), LocalTime.of(11, 0)));
        }

        @Test
        @DisplayName("deve retornar false quando janela termina antes do fim do período")
        void deveRetornarFalseQuandoJanelaTerminaAntesDoFimDoPeriodo() {
            assertFalse(periodo.contemHorario(LocalTime.of(9, 0), LocalTime.of(10, 30)));
        }

        @Test
        @DisplayName("deve lançar exceção quando inicio da janela é nulo")
        void deveLancarExcecaoQuandoInicioDaJanelaENulo() {
            assertThrows(IllegalArgumentException.class, () -> periodo.contemHorario(null, LocalTime.of(11, 0)));
        }

        @Test
        @DisplayName("deve lançar exceção quando fim da janela é nulo")
        void deveLancarExcecaoQuandoFimDaJanelaENulo() {
            assertThrows(IllegalArgumentException.class, () -> periodo.contemHorario(LocalTime.of(9, 0), null));
        }

        @Test
        @DisplayName("deve lançar exceção quando início da janela não é anterior ao fim")
        void deveLancarExcecaoQuandoInicioDaJanelaNaoENteriorAoFim() {
            assertThrows(IllegalArgumentException.class, () -> periodo.contemHorario(LocalTime.of(11, 0), LocalTime.of(9, 0)));
        }
    }
}
