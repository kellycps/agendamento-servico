package com.fiap.agendamento_servico.domain.services;

import com.fiap.agendamento_servico.domain.entities.Agendamento;
import com.fiap.agendamento_servico.domain.entities.Estabelecimento;
import com.fiap.agendamento_servico.domain.entities.Profissional;
import com.fiap.agendamento_servico.domain.exceptions.HorarioIndisponivelException;
import com.fiap.agendamento_servico.domain.valueobjects.Endereco;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ValidadorAgendamento")
class ValidadorAgendamentoTest {

    private ValidadorAgendamento validador;

    private static final UUID CLIENTE_ID = UUID.randomUUID();
    private static final UUID PROFISSIONAL_ID = UUID.randomUUID();
    private static final UUID SERVICO_ID = UUID.randomUUID();
    private static final UUID ESTABELECIMENTO_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        validador = new ValidadorAgendamento();
    }

    @Nested
    @DisplayName("validarSemSobreposicao()")
    class ValidarSemSobreposicao {
        @Test
        @DisplayName("não deve lançar exceção quando não há sobreposição de horários com agendamentos existentes")
        void naoDeveLancarExcecaoQuandoNaoHaSobreposicaoDeHorarios() {
            Agendamento novo = criar(10, 0, 30);
            
            assertDoesNotThrow(() -> validador.validarSemSobreposicao(novo, List.of()));
        }

        @Test
        @DisplayName("não deve lançar exceção quando lista de agendamentos é nula")
        void naoDeveLancarExcecaoQuandoListaDeAgendamentosENula() {
            Agendamento novo = criar(10, 0, 30);
            
            assertDoesNotThrow(() -> validador.validarSemSobreposicao(novo, null));
        }

        @Test
        @DisplayName("não deve lançar exceção quando horários de agendamentos não se sobrepõem")
        void naoDeveLancarExcecaoQuandoHorariosDeAgendamentosNaoSeSobrepoem() {
            Agendamento agendamentoExistente = criar(8, 0, 60);
            Agendamento novoAgendamento = criar(9, 0, 60);
            
            assertDoesNotThrow(() -> validador.validarSemSobreposicao(novoAgendamento, List.of(agendamentoExistente)));
        }

        @Test
        @DisplayName("deve lançar exceção quando há sobreposição parcial de horários")
        void deveValidarSemSobreposicaoQuandoHaSobreposicaoParcialDeHorarios() {
            Agendamento agendamentoExistente = criar(8, 0, 60);
            Agendamento novoAgendamento = criar(8, 30, 60);
            
            assertThrows(HorarioIndisponivelException.class, () -> validador.validarSemSobreposicao(novoAgendamento, List.of(agendamentoExistente)));
        }

        @Test
        @DisplayName("deve lançar exceção quando horário novo contém sobrepõe horário existente")
        void deveValidarSemSobreposicaoQuandoNovoContemExistente() {
            Agendamento agendamentoExistente = criar(9, 0, 30);
            Agendamento novoAgendamento = criar(8, 0, 120);
            
            assertThrows(HorarioIndisponivelException.class, () -> validador.validarSemSobreposicao(novoAgendamento, List.of(agendamentoExistente)));
        }

        @Test
        @DisplayName("deve ignorar agendamentos com status CANCELADO")
        void deveIgnorarAgendamentosComStatusCancelado() {
            Agendamento agendamentoExistente = criar(8, 0, 60);
            Agendamento cancelado = agendamentoExistente.cancelar();
            Agendamento novoAgendamento = criar(8, 30, 60);

            assertDoesNotThrow(() -> validador.validarSemSobreposicao(novoAgendamento, List.of(cancelado)));
        }

        @Test
        @DisplayName("deve lançar exceção quando novo agendamento é nulo")
        void novoAgendamentoNulo() {
            assertThrows(IllegalArgumentException.class, () -> validador.validarSemSobreposicao(null, List.of()));
        }
    }

    @Nested
    @DisplayName("validarHorarioTrabalho()")
    class ValidarHorarioTrabalho {
        @Test
        @DisplayName("não deve lançar exceção quando agendamento está dentro do horário de trabalho do profissional")
        void naoDeveLancarExcecaoQuandoAgendamentoEstaDentroDoHorarioDeTrabalhoDoProfissional() {
            Profissional profissional = profissionalComHorario(LocalTime.of(8, 0), LocalTime.of(18, 0));
            Agendamento agendamento = criar(10, 0, 60);

            assertDoesNotThrow(() -> validador.validarHorarioTrabalho(agendamento, profissional));
        }

        @Test
        @DisplayName("deve lançar exceção quando agendamento começa antes do horário de trabalho do profissional")
        void deveLancarExcecaoQuandoAgendamentoComecaAntesDoHorarioDeTrabalhoDoProfissional() {
            Profissional profissional = profissionalComHorario(LocalTime.of(9, 0), LocalTime.of(18, 0));
            Agendamento agendamento = criar(8, 0, 60);
            
            assertThrows(HorarioIndisponivelException.class, () -> validador.validarHorarioTrabalho(agendamento, profissional));
        }

        @Test
        @DisplayName("deve lançar exceção quando agendamento termina depois do horário de trabalho do profissional")
        void deveLancarExcecaoQuandoAgendamentoTerminaDepoisDoHorarioDeTrabalhoDoProfissional() {
            Profissional profissional = profissionalComHorario(LocalTime.of(8, 0), LocalTime.of(17, 0));
            Agendamento agendamento = criar(16, 30, 60);
            
            assertThrows(HorarioIndisponivelException.class, () -> validador.validarHorarioTrabalho(agendamento, profissional));
        }

        @Test
        @DisplayName("não deve lançar exceção quando profissional não tem horário configurado")
        void deveNaoLancarExcecaoQuandoProfissionalNaoTemHorarioConfigurado() {
            Profissional profissional = new Profissional(UUID.randomUUID(), "Nome Profissional", ESTABELECIMENTO_ID, List.of(), List.of(), null, null);
            Agendamento agendamento = criar(6, 0, 60);
            
            assertDoesNotThrow(() -> validador.validarHorarioTrabalho(agendamento, profissional));
        }
    }

    private Agendamento criar(int hora, int minuto, int duracaoMinutos) {
        LocalDateTime inicio = LocalDateTime.of(2026, 3, 25, hora, minuto);
        return Agendamento.criar(CLIENTE_ID, PROFISSIONAL_ID, SERVICO_ID, inicio, duracaoMinutos);
    }

    private Estabelecimento estabelecimentoComIntervalo(LocalTime inicio, LocalTime fim, int intervalo) {
        return new Estabelecimento(
                ESTABELECIMENTO_ID, "Salão Teste",
                new Endereco("Rua A", "1", null, "Centro", "01001-000", "São Paulo"),
                inicio, fim, intervalo, List.of());
    }

    @Nested
    @DisplayName("validarAlinhamentoComGrade()")
    class ValidarAlinhamentoComGrade {

        @Test
        @DisplayName("não deve lançar exceção quando horário está dentro da grade")
        void naoDeveLancarExcecaoParaHorarioEstaDentroGrade() {
            Agendamento ag = criar(10, 0, 30);
            Estabelecimento estab = estabelecimentoComIntervalo(LocalTime.of(8, 0), LocalTime.of(20, 0), 30);

            assertDoesNotThrow(() -> validador.validarAlinhamentoComGrade(ag, estab));
        }

        @Test
        @DisplayName("deve lançar exceção quando minutos não são múltiplo do intervalo")
        void deveLancarExcecaoParaMinutosNaoMultiplos() {
            Agendamento ag = criar(10, 15, 30);
            Estabelecimento estab = estabelecimentoComIntervalo(LocalTime.of(8, 0), LocalTime.of(20, 0), 30);

            assertThrows(HorarioIndisponivelException.class, () -> validador.validarAlinhamentoComGrade(ag, estab));
        }

        @Test
        @DisplayName("deve lançar exceção quando horário é anterior à abertura do estabelecimento")
        void deveLancarExcecaoParaHorarioAntesAberturaDoEstabelecimento() {
            Agendamento ag = criar(7, 0, 30);
            Estabelecimento estab = estabelecimentoComIntervalo(LocalTime.of(8, 0), LocalTime.of(20, 0), 30);

            assertThrows(HorarioIndisponivelException.class, () -> validador.validarAlinhamentoComGrade(ag, estab));
        }

        @Test
        @DisplayName("deve lançar exceção quando horário é após o fechamento do estabelecimento")
        void deveLancarExcecaoParaHorarioAposFechamentoDoEstabelecimento() {
            Agendamento ag = criar(20, 0, 30);
            Estabelecimento estab = estabelecimentoComIntervalo(LocalTime.of(8, 0), LocalTime.of(20, 0), 30);

            assertThrows(HorarioIndisponivelException.class, () -> validador.validarAlinhamentoComGrade(ag, estab));
        }

        @Test
        @DisplayName("não deve lançar exceção para grade de 15 minutos alinhada")
        void naoDeveLancarExcecaoParaGrade15MinAlinhada() {
            Agendamento ag = criar(9, 45, 15);
            Estabelecimento estab = estabelecimentoComIntervalo(LocalTime.of(8, 0), LocalTime.of(18, 0), 15);

            assertDoesNotThrow(() -> validador.validarAlinhamentoComGrade(ag, estab));
        }
    }

    private Profissional profissionalComHorario(LocalTime inicio, LocalTime fim) {
        return new Profissional(
                UUID.randomUUID(),
                "Prof Teste",
                ESTABELECIMENTO_ID,
                List.of(),
                List.of(),
                inicio,
                fim);
    }
}
