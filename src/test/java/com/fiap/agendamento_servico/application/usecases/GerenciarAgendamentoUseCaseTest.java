package com.fiap.agendamento_servico.application.usecases;

import com.fiap.agendamento_servico.application.dto.ReagendarAgendamentoDTO;
import com.fiap.agendamento_servico.application.ports.out.AgendamentoRepositorioPort;
import com.fiap.agendamento_servico.application.ports.out.ClienteRepositorioPort;
import com.fiap.agendamento_servico.application.ports.out.EstabelecimentoRepositorioPort;
import com.fiap.agendamento_servico.application.ports.out.NotificacaoPort;
import com.fiap.agendamento_servico.application.ports.out.ProfissionalRepositorioPort;
import com.fiap.agendamento_servico.application.ports.out.ServicoRepositorioPort;
import com.fiap.agendamento_servico.domain.entities.Agendamento;
import com.fiap.agendamento_servico.domain.entities.Cliente;
import com.fiap.agendamento_servico.domain.entities.Estabelecimento;
import com.fiap.agendamento_servico.domain.entities.Profissional;
import com.fiap.agendamento_servico.domain.valueobjects.Endereco;
import com.fiap.agendamento_servico.domain.entities.Servico;
import com.fiap.agendamento_servico.domain.exceptions.BusinessException;
import com.fiap.agendamento_servico.domain.exceptions.EntidadeNaoEncontradaException;
import com.fiap.agendamento_servico.domain.exceptions.HorarioIndisponivelException;
import com.fiap.agendamento_servico.domain.services.ValidadorAgendamento;
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
@DisplayName("GerenciarAgendamentoUseCase")
class GerenciarAgendamentoUseCaseTest {

    @Mock 
    private AgendamentoRepositorioPort agendamentoRepositorioPort;
    
    @Mock 
    private ServicoRepositorioPort servicoRepositorioPort;
    
    @Mock 
    private ProfissionalRepositorioPort profissionalRepositorioPort;
    
    @Mock 
    private ClienteRepositorioPort clienteRepositorioPort;
    
    @Mock 
    private EstabelecimentoRepositorioPort estabelecimentoRepositorioPort;
    
    @Mock 
    private NotificacaoPort notificacaoPort;

    private GerenciarAgendamentoUseCase useCase;

    private static final UUID AGENDAMENTO_ID = UUID.randomUUID();
    private static final UUID CLIENTE_ID = UUID.randomUUID();
    private static final UUID PROFISSIONAL_ID = UUID.randomUUID();
    private static final UUID SERVICO_ID = UUID.randomUUID();
    private static final UUID ESTABELECIMENTO_ID = UUID.randomUUID();
    private static final LocalDateTime INICIO = LocalDateTime.of(2026, 3, 25, 10, 0);

    private Agendamento agendamentoPendente;
    private Profissional profissional;
    private Cliente cliente;
    private Servico servico;
    private Estabelecimento estabelecimento;

    @BeforeEach
    void setUp() {
        useCase = new GerenciarAgendamentoUseCase(
                agendamentoRepositorioPort,
                servicoRepositorioPort,
                profissionalRepositorioPort,
                clienteRepositorioPort,
                estabelecimentoRepositorioPort,
                notificacaoPort,
                new ValidadorAgendamento()
        );

        agendamentoPendente = Agendamento.criar(CLIENTE_ID, PROFISSIONAL_ID, SERVICO_ID, INICIO, 30);
        
        profissional = new Profissional(PROFISSIONAL_ID, "Ana Silva", ESTABELECIMENTO_ID, List.of(SERVICO_ID), List.of(), LocalTime.of(8, 0), LocalTime.of(18, 0));
        cliente = new Cliente(CLIENTE_ID, "Maria Pereira", "(11)99999-0000", "maria@email.com");
        servico = new Servico(SERVICO_ID, "Corte", "Corte de cabelo", 50.0, 30, ESTABELECIMENTO_ID);
        estabelecimento = estabelecimentoComNome("Salão Teste");
    }

    @Nested
    @DisplayName("confirmar()")
    class Confirmar {
        @Test
        @DisplayName("deve salvar agendamento CONFIRMADO e enviar notificação")
        void deveSalvarAgendamentoConfirmadoEEnviarNotificacao() {
            when(agendamentoRepositorioPort.buscarPorId(AGENDAMENTO_ID)).thenReturn(Optional.of(agendamentoPendente));
            when(agendamentoRepositorioPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));
            when(profissionalRepositorioPort.buscarPorId(PROFISSIONAL_ID)).thenReturn(Optional.of(profissional));
            when(clienteRepositorioPort.buscarPorId(CLIENTE_ID)).thenReturn(Optional.of(cliente));
            when(servicoRepositorioPort.buscarPorId(SERVICO_ID)).thenReturn(Optional.of(servico));
            when(estabelecimentoRepositorioPort.buscarPorId(ESTABELECIMENTO_ID)).thenReturn(Optional.of(estabelecimento));

            assertDoesNotThrow(() -> useCase.confirmar(AGENDAMENTO_ID));

            verify(agendamentoRepositorioPort).salvar(argThat(a ->a.status().name().equals("CONFIRMADO")));
            verify(notificacaoPort).enviarConfirmacaoAgendamento(
                    any(), eq(profissional), eq("Maria Pereira"), eq("maria@email.com"),
                    eq("Corte"), eq("Salão Teste"));
        }

        @Test
        @DisplayName("deve lançar exceção quando agendamento não existe")
        void deveLancarExcecaoQuandoAgendamentoNaoExiste() {
            when(agendamentoRepositorioPort.buscarPorId(AGENDAMENTO_ID)).thenReturn(Optional.empty());
            
            assertThrows(EntidadeNaoEncontradaException.class, () -> useCase.confirmar(AGENDAMENTO_ID));
            
            verify(notificacaoPort, never()).enviarConfirmacaoAgendamento(any(), any(), any(), any(), any(), any());
        }

        @Test
        @DisplayName("deve lançar exceção quando agendamento não está PENDENTE")
        void deveLancarExcecaoQuandoAgendamentoNaoEstaPendente() {
            Agendamento cancelado = agendamentoPendente.cancelar();
            
            when(agendamentoRepositorioPort.buscarPorId(AGENDAMENTO_ID)).thenReturn(Optional.of(cancelado));
            
            assertThrows(BusinessException.class, () -> useCase.confirmar(AGENDAMENTO_ID));
        }

        @Test
        @DisplayName("deve lançar exceção quando id é nulo")
        void deveLancarExcecaoQuandoIdEhNulo() {
            assertThrows(IllegalArgumentException.class, () -> useCase.confirmar(null));
        }
    }

    @Nested
    @DisplayName("cancelar()")
    class Cancelar {
        @Test
        @DisplayName("deve salvar agendamento CANCELADO")
        void deveSalvarAgendamentoCancelado() {
            when(agendamentoRepositorioPort.buscarPorId(AGENDAMENTO_ID)).thenReturn(Optional.of(agendamentoPendente));
            when(agendamentoRepositorioPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

            assertDoesNotThrow(() -> useCase.cancelar(AGENDAMENTO_ID));

            verify(agendamentoRepositorioPort).salvar(argThat(a ->a.status().name().equals("CANCELADO")));
        }

        @Test
        @DisplayName("deve lançar exceção quando agendamento não existe")
        void deveLancarExcecaoQuandoAgendamentoNaoExiste() {
            when(agendamentoRepositorioPort.buscarPorId(AGENDAMENTO_ID)).thenReturn(Optional.empty());
            
            assertThrows(EntidadeNaoEncontradaException.class, () -> useCase.cancelar(AGENDAMENTO_ID));
        }
    }

    @Nested
    @DisplayName("concluir()")
    class Concluir {
        @Test
        @DisplayName("deve salvar agendamento CONCLUIDO")
        void deveSalvarAgendamentoConcluido() {
            Agendamento confirmado = agendamentoPendente.confirmar();
            
            when(agendamentoRepositorioPort.buscarPorId(AGENDAMENTO_ID)).thenReturn(Optional.of(confirmado));
            when(agendamentoRepositorioPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

            assertDoesNotThrow(() -> useCase.concluir(AGENDAMENTO_ID));
            verify(agendamentoRepositorioPort).salvar(argThat(a -> a.status().name().equals("CONCLUIDO")));
        }

        @Test
        @DisplayName("deve lançar exceção quando tenta concluir agendamento PENDENTE")
        void deveLancarExcecaoQuandoTentaConcluirAgendamentoPendente() {
            when(agendamentoRepositorioPort.buscarPorId(AGENDAMENTO_ID))
                    .thenReturn(Optional.of(agendamentoPendente));
            assertThrows(BusinessException.class, () -> useCase.concluir(AGENDAMENTO_ID));
        }
    }

    @Nested
    @DisplayName("reagendar()")
    class Reagendar {
        @Test
        @DisplayName("deve salvar agendamento reagendado sem sobreposição")
        void deveSalvarAgendamentoReagendadoSemSobreposicao() {
            LocalDateTime novoInicio = INICIO.plusDays(1);
            
            when(agendamentoRepositorioPort.buscarPorId(AGENDAMENTO_ID)).thenReturn(Optional.of(agendamentoPendente));
            when(servicoRepositorioPort.buscarPorId(SERVICO_ID)).thenReturn(Optional.of(servico));
            when(agendamentoRepositorioPort.buscarPorProfissionalEData(any(), any())).thenReturn(List.of());
            when(agendamentoRepositorioPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

            ReagendarAgendamentoDTO dto = new ReagendarAgendamentoDTO(AGENDAMENTO_ID, novoInicio);
            assertDoesNotThrow(() -> useCase.reagendar(dto));
            verify(agendamentoRepositorioPort).salvar(any());
        }

        @Test
        @DisplayName("deve lançar exceção quando DTO é nulo")
        void deveLancarExcecaoQuandoDtoENulo() {
            assertThrows(IllegalArgumentException.class, () -> useCase.reagendar(null));
        }

        @Test
        @DisplayName("deve lançar exceção quando há sobreposição no novo horário")
        void deveLancarExcecaoQuandoHaSobreposicaoNoNovoHorario() {
            LocalDateTime novoInicio = INICIO.plusDays(1);
            Agendamento conflito = Agendamento.criar(UUID.randomUUID(), PROFISSIONAL_ID, SERVICO_ID, novoInicio.minusMinutes(15), 60);

            when(agendamentoRepositorioPort.buscarPorId(AGENDAMENTO_ID)).thenReturn(Optional.of(agendamentoPendente));
            when(servicoRepositorioPort.buscarPorId(SERVICO_ID)).thenReturn(Optional.of(servico));
            when(agendamentoRepositorioPort.buscarPorProfissionalEData(any(), any())).thenReturn(List.of(conflito));

            ReagendarAgendamentoDTO dto = new ReagendarAgendamentoDTO(AGENDAMENTO_ID, novoInicio);
            assertThrows(HorarioIndisponivelException.class, () -> useCase.reagendar(dto));
        }
    }

    @Nested
    @DisplayName("deletar()")
    class Deletar {
        @Test
        @DisplayName("deve deletar agendamento com status PENDENTE")
        void deveDeletarAgendamentoComStatusPendente() {
            when(agendamentoRepositorioPort.buscarPorId(AGENDAMENTO_ID)).thenReturn(Optional.of(agendamentoPendente));
            doNothing().when(agendamentoRepositorioPort).deletar(AGENDAMENTO_ID);

            assertDoesNotThrow(() -> useCase.deletar(AGENDAMENTO_ID));
            verify(agendamentoRepositorioPort).deletar(AGENDAMENTO_ID);
        }

        @Test
        @DisplayName("deve deletar agendamento com status CANCELADO")
        void deveDeletarAgendamentoComStatusCancelado() {
            Agendamento cancelado = agendamentoPendente.cancelar();
            
            when(agendamentoRepositorioPort.buscarPorId(AGENDAMENTO_ID)).thenReturn(Optional.of(cancelado));
            doNothing().when(agendamentoRepositorioPort).deletar(AGENDAMENTO_ID);

            assertDoesNotThrow(() -> useCase.deletar(AGENDAMENTO_ID));
            verify(agendamentoRepositorioPort).deletar(AGENDAMENTO_ID);
        }

        @Test
        @DisplayName("deve lançar exceção ao tentar deletar agendamento CONFIRMADO")
        void deveLancarExcecaoQuandoTentaDeletarAgendamentoConfirmado() {
            Agendamento confirmado = agendamentoPendente.confirmar();
           
            when(agendamentoRepositorioPort.buscarPorId(AGENDAMENTO_ID)).thenReturn(Optional.of(confirmado));

            assertThrows(BusinessException.class, () -> useCase.deletar(AGENDAMENTO_ID));
            verify(agendamentoRepositorioPort, never()).deletar(any());
        }

        @Test
        @DisplayName("deve lançar exceção ao tentar deletar agendamento CONCLUIDO")
        void deveLancarExcecaoQuandoTentaDeletarAgendamentoConcluido() {
            Agendamento concluido = agendamentoPendente.confirmar().concluir();
            
            when(agendamentoRepositorioPort.buscarPorId(AGENDAMENTO_ID)).thenReturn(Optional.of(concluido));

            assertThrows(BusinessException.class, () -> useCase.deletar(AGENDAMENTO_ID));
            verify(agendamentoRepositorioPort, never()).deletar(any());
        }
    }

    private Estabelecimento estabelecimentoComNome(String nome) {
        Endereco endereco = new Endereco("Rua A", "100", null, "Centro", "01001-000", "São Paulo");
        
        return new Estabelecimento(ESTABELECIMENTO_ID, nome, endereco,LocalTime.of(8, 0), LocalTime.of(20, 0), 30, List.of());
    }
}
