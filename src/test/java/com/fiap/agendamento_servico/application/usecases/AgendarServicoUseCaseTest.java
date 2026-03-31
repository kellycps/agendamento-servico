package com.fiap.agendamento_servico.application.usecases;

import com.fiap.agendamento_servico.application.dto.DetalhesAgendamentoDTO;
import com.fiap.agendamento_servico.application.dto.NovoAgendamentoDTO;
import com.fiap.agendamento_servico.application.ports.out.AgendamentoRepositorioPort;
import com.fiap.agendamento_servico.application.ports.out.ClienteRepositorioPort;
import com.fiap.agendamento_servico.application.ports.out.EstabelecimentoRepositorioPort;
import com.fiap.agendamento_servico.application.ports.out.ProfissionalRepositorioPort;
import com.fiap.agendamento_servico.application.ports.out.ServicoRepositorioPort;
import com.fiap.agendamento_servico.domain.entities.Agendamento;
import com.fiap.agendamento_servico.domain.entities.Cliente;
import com.fiap.agendamento_servico.domain.entities.Estabelecimento;
import com.fiap.agendamento_servico.domain.entities.Profissional;
import com.fiap.agendamento_servico.domain.entities.Servico;
import com.fiap.agendamento_servico.domain.exceptions.EntidadeNaoEncontradaException;
import com.fiap.agendamento_servico.domain.exceptions.HorarioIndisponivelException;
import com.fiap.agendamento_servico.domain.services.ValidadorAgendamento;
import com.fiap.agendamento_servico.domain.valueobjects.Endereco;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AgendarServicoUseCase")
class AgendarServicoUseCaseTest {

        @Mock 
        private ProfissionalRepositorioPort profissionalRepositorioPort;
        
        @Mock 
        private ServicoRepositorioPort      servicoRepositorioPort;
        
        @Mock 
        private AgendamentoRepositorioPort  agendamentoRepositorioPort;
        
        @Mock 
        private ClienteRepositorioPort      clienteRepositorioPort;

        @Mock
        private EstabelecimentoRepositorioPort estabelecimentoRepositorioPort;

        private ValidadorAgendamento validadorAgendamento;
        private AgendarServicoUseCase useCase;

        private static final UUID CLIENTE_ID      = UUID.randomUUID();
        private static final UUID PROFISSIONAL_ID = UUID.randomUUID();
        private static final UUID SERVICO_ID      = UUID.randomUUID();
        private static final UUID ESTABELECIMENTO_ID = UUID.randomUUID();
        private static final LocalDateTime DATA_HORA = LocalDateTime.of(2026, 3, 25, 10, 0);

        private static final Cliente CLIENTE = new Cliente(CLIENTE_ID, "Cliente Teste", "11999999999", "teste@email.com");

        private Profissional profissional;
        private Servico servico;
        private Estabelecimento estabelecimento;

        @BeforeEach
        void setUp() {
                validadorAgendamento = new ValidadorAgendamento();

                useCase = new AgendarServicoUseCase(
                        profissionalRepositorioPort,
                        servicoRepositorioPort,
                        agendamentoRepositorioPort,
                        clienteRepositorioPort,
                        estabelecimentoRepositorioPort,
                        validadorAgendamento);

                profissional = new Profissional(
                        PROFISSIONAL_ID,
                        "Ana Silva",
                        ESTABELECIMENTO_ID,
                        List.of(SERVICO_ID),
                        List.of(),
                        LocalTime.of(8, 0),
                        LocalTime.of(18, 0));

                servico = new Servico(SERVICO_ID, "Corte", "Corte de cabelo", 50.0, 30, ESTABELECIMENTO_ID);

                estabelecimento = new Estabelecimento(
                        ESTABELECIMENTO_ID, "Salão Teste",
                        new Endereco("Rua A", "1", null, "Centro", "01001-000", "São Paulo"),
                        LocalTime.of(8, 0), LocalTime.of(20, 0), 30, List.of());
        }

        @Nested
        @DisplayName("executar()")
        class Executar {
                @Test
                @DisplayName("deve criar agendamento e retornar DTO com status PENDENTE")
                void deveCriarAgendamentoERetornarDTOComStatusPendente() {
                        NovoAgendamentoDTO dto = new NovoAgendamentoDTO(CLIENTE_ID, PROFISSIONAL_ID, SERVICO_ID, DATA_HORA);

                        when(clienteRepositorioPort.buscarPorId(CLIENTE_ID)).thenReturn(Optional.of(CLIENTE));
                        when(profissionalRepositorioPort.buscarPorId(PROFISSIONAL_ID)).thenReturn(Optional.of(profissional));
                        when(servicoRepositorioPort.buscarPorId(SERVICO_ID)).thenReturn(Optional.of(servico));
                        when(agendamentoRepositorioPort.buscarPorProfissionalEData(any(), any())).thenReturn(List.of());
                        when(agendamentoRepositorioPort.salvar(any(Agendamento.class))).thenAnswer(inv -> inv.getArgument(0));
                        when(estabelecimentoRepositorioPort.buscarPorId(ESTABELECIMENTO_ID)).thenReturn(Optional.of(estabelecimento));

                        DetalhesAgendamentoDTO resultado = useCase.executar(dto);

                        assertNotNull(resultado);
                        assertEquals(DATA_HORA, resultado.dataHoraInicio());
                        assertEquals("Corte", resultado.servicoNome());
                        verify(agendamentoRepositorioPort).salvar(any(Agendamento.class));
                }

                @Test
                @DisplayName("deve lançar exceção quando profissional não é encontrado")
                void deveLancarExcecaoQuandoProfissionalNaoEncontrado() {
                        NovoAgendamentoDTO dto = new NovoAgendamentoDTO(CLIENTE_ID, PROFISSIONAL_ID, SERVICO_ID, DATA_HORA);

                        when(clienteRepositorioPort.buscarPorId(CLIENTE_ID)).thenReturn(Optional.of(CLIENTE));
                        when(profissionalRepositorioPort.buscarPorId(PROFISSIONAL_ID)).thenReturn(Optional.empty());

                        assertThrows(EntidadeNaoEncontradaException.class, () -> useCase.executar(dto));
                        verify(agendamentoRepositorioPort, never()).salvar(any());
                }

                @Test
                @DisplayName("deve lançar exceção quando serviço não é encontrado")
                void deveLancarExcecaoQuandoServicoNaoEncontrado() {
                        NovoAgendamentoDTO dto = new NovoAgendamentoDTO(CLIENTE_ID, PROFISSIONAL_ID, SERVICO_ID, DATA_HORA);

                        when(clienteRepositorioPort.buscarPorId(CLIENTE_ID)).thenReturn(Optional.of(CLIENTE));
                        when(profissionalRepositorioPort.buscarPorId(PROFISSIONAL_ID)).thenReturn(Optional.of(profissional));
                        when(servicoRepositorioPort.buscarPorId(SERVICO_ID)).thenReturn(Optional.empty());

                        assertThrows(EntidadeNaoEncontradaException.class, () -> useCase.executar(dto));
                        verify(agendamentoRepositorioPort, never()).salvar(any());
                }

                @Test
                @DisplayName("deve lançar exceção quando há sobreposição de horário")
                void deveLancarExcecaoQuandoHaSobreposicaoDeHorario() {
                        NovoAgendamentoDTO dto = new NovoAgendamentoDTO(CLIENTE_ID, PROFISSIONAL_ID, SERVICO_ID, DATA_HORA);

                        Agendamento existente = Agendamento.criar(CLIENTE_ID, PROFISSIONAL_ID, SERVICO_ID, DATA_HORA.minusMinutes(15), 60);

                        when(clienteRepositorioPort.buscarPorId(CLIENTE_ID)).thenReturn(Optional.of(CLIENTE));
                        when(profissionalRepositorioPort.buscarPorId(PROFISSIONAL_ID)).thenReturn(Optional.of(profissional));
                        when(servicoRepositorioPort.buscarPorId(SERVICO_ID)).thenReturn(Optional.of(servico));
                        when(estabelecimentoRepositorioPort.buscarPorId(ESTABELECIMENTO_ID)).thenReturn(Optional.of(estabelecimento));
                        when(agendamentoRepositorioPort.buscarPorProfissionalEData(any(), any())).thenReturn(List.of(existente));

                        assertThrows(HorarioIndisponivelException.class, () -> useCase.executar(dto));
                        verify(agendamentoRepositorioPort, never()).salvar(any());
                }

                @Test
                @DisplayName("deve lançar exceção quando DTO é nulo")
                void deveLancarExcecaoQuandoDtoNulo() {
                        assertThrows(IllegalArgumentException.class, () -> useCase.executar(null));
                }

                @Test
                @DisplayName("deve lançar exceção quando clienteId no DTO é nulo")
                void deveLancarExcecaoQuandoClienteIdNulo() {
                        NovoAgendamentoDTO dto = new NovoAgendamentoDTO(null, PROFISSIONAL_ID, SERVICO_ID, DATA_HORA);
                        
                        assertThrows(IllegalArgumentException.class, () -> useCase.executar(dto));
                }

                @Test
                @DisplayName("deve lançar exceção quando horário não está alinhado à grade do estabelecimento")
                void deveLancarExcecaoQuandoHorarioNaoAlinhado() {
                        LocalDateTime foraDaGrade = LocalDateTime.of(2026, 3, 25, 10, 15);
                        NovoAgendamentoDTO dto = new NovoAgendamentoDTO(CLIENTE_ID, PROFISSIONAL_ID, SERVICO_ID, foraDaGrade);

                        when(clienteRepositorioPort.buscarPorId(CLIENTE_ID)).thenReturn(Optional.of(CLIENTE));
                        when(profissionalRepositorioPort.buscarPorId(PROFISSIONAL_ID)).thenReturn(Optional.of(profissional));
                        when(servicoRepositorioPort.buscarPorId(SERVICO_ID)).thenReturn(Optional.of(servico));
                        when(estabelecimentoRepositorioPort.buscarPorId(ESTABELECIMENTO_ID)).thenReturn(Optional.of(estabelecimento));

                        assertThrows(HorarioIndisponivelException.class, () -> useCase.executar(dto));
                        verify(agendamentoRepositorioPort, never()).salvar(any());
                }

                @Test
                @DisplayName("deve lançar exceção quando horário está fora do funcionamento do estabelecimento")
                void deveLancarExcecaoQuandoHorarioForaDoFuncionamento() {
                        Profissional profissionalExtendido = new Profissional(
                                PROFISSIONAL_ID, "Ana Silva", ESTABELECIMENTO_ID,
                                List.of(SERVICO_ID), List.of(),
                                LocalTime.of(8, 0), LocalTime.of(22, 0));

                        LocalDateTime aposFechar = LocalDateTime.of(2026, 3, 25, 21, 0);
                        NovoAgendamentoDTO dto = new NovoAgendamentoDTO(CLIENTE_ID, PROFISSIONAL_ID, SERVICO_ID, aposFechar);

                        when(clienteRepositorioPort.buscarPorId(CLIENTE_ID)).thenReturn(Optional.of(CLIENTE));
                        when(profissionalRepositorioPort.buscarPorId(PROFISSIONAL_ID)).thenReturn(Optional.of(profissionalExtendido));
                        when(servicoRepositorioPort.buscarPorId(SERVICO_ID)).thenReturn(Optional.of(servico));
                        when(estabelecimentoRepositorioPort.buscarPorId(ESTABELECIMENTO_ID)).thenReturn(Optional.of(estabelecimento));

                        assertThrows(HorarioIndisponivelException.class, () -> useCase.executar(dto));
                        verify(agendamentoRepositorioPort, never()).salvar(any());
                }

                @Test
                @DisplayName("deve lançar exceção quando horário está fora da jornada do profissional")
                        void deveLancarExcecaoQuandoHorarioForaDaJornada() {
                        LocalDateTime foraDoHorario = LocalDateTime.of(2026, 3, 25, 7, 0);
                        NovoAgendamentoDTO dto = new NovoAgendamentoDTO(CLIENTE_ID, PROFISSIONAL_ID, SERVICO_ID, foraDoHorario);

                        when(clienteRepositorioPort.buscarPorId(CLIENTE_ID)).thenReturn(Optional.of(CLIENTE));
                        when(profissionalRepositorioPort.buscarPorId(PROFISSIONAL_ID)).thenReturn(Optional.of(profissional));
                        when(servicoRepositorioPort.buscarPorId(SERVICO_ID)).thenReturn(Optional.of(servico));

                        assertThrows(HorarioIndisponivelException.class, () -> useCase.executar(dto));
                }
    }
}
