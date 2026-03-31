package com.fiap.agendamento_servico.application.usecases;

import com.fiap.agendamento_servico.application.dto.DetalhesAvaliacaoDTO;
import com.fiap.agendamento_servico.application.dto.NovaAvaliacaoDTO;
import com.fiap.agendamento_servico.application.ports.out.AgendamentoRepositorioPort;
import com.fiap.agendamento_servico.application.ports.out.AvaliacaoRepositorioPort;
import com.fiap.agendamento_servico.application.ports.out.EstabelecimentoRepositorioPort;
import com.fiap.agendamento_servico.application.ports.out.ProfissionalRepositorioPort;
import com.fiap.agendamento_servico.domain.entities.Agendamento;
import com.fiap.agendamento_servico.domain.entities.Avaliacao;
import com.fiap.agendamento_servico.domain.entities.Estabelecimento;
import com.fiap.agendamento_servico.domain.entities.Profissional;
import com.fiap.agendamento_servico.domain.exceptions.BusinessException;
import com.fiap.agendamento_servico.domain.exceptions.EntidadeNaoEncontradaException;
import com.fiap.agendamento_servico.domain.services.CalculadoraDeMediaAvaliacao;
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
@DisplayName("RegistrarAvaliacaoUseCase")
class RegistrarAvaliacaoUseCaseTest {

    @Mock 
    private AgendamentoRepositorioPort agendamentoRepositorioPort;
    
    @Mock 
    private AvaliacaoRepositorioPort avaliacaoRepositorioPort;
    
    @Mock 
    private ProfissionalRepositorioPort profissionalRepositorioPort;
    
    @Mock 
    private EstabelecimentoRepositorioPort estabelecimentoRepositorioPort;

    private CalculadoraDeMediaAvaliacao calculadoraDeMediaAvaliacao;
    private RegistrarAvaliacaoUseCase useCase;

    private static final UUID AGENDAMENTO_ID    = UUID.randomUUID();
    private static final UUID PROFISSIONAL_ID   = UUID.randomUUID();
    private static final UUID SERVICO_ID        = UUID.randomUUID();
    private static final UUID CLIENTE_ID        = UUID.randomUUID();
    private static final UUID ESTABELECIMENTO_ID = UUID.randomUUID();
    private static final LocalDateTime INICIO   = LocalDateTime.of(2026, 3, 25, 10, 0);

    private Agendamento agendamentoConcluido;
    private Profissional profissional;
    private Estabelecimento estabelecimento;

    @BeforeEach
    void setUp() {
        calculadoraDeMediaAvaliacao = new CalculadoraDeMediaAvaliacao();
        useCase = new RegistrarAvaliacaoUseCase(
                agendamentoRepositorioPort,
                avaliacaoRepositorioPort,
                profissionalRepositorioPort,
                estabelecimentoRepositorioPort,
                calculadoraDeMediaAvaliacao
        );

        agendamentoConcluido = Agendamento.criar(CLIENTE_ID, PROFISSIONAL_ID, SERVICO_ID, INICIO, 30)
                .confirmar()
                .concluir();

        profissional = new Profissional(PROFISSIONAL_ID, "Ana Silva",
                ESTABELECIMENTO_ID, List.of(SERVICO_ID), List.of(),
                LocalTime.of(8, 0), LocalTime.of(18, 0));

        estabelecimento = new Estabelecimento(
                ESTABELECIMENTO_ID,
                "Salão Teste",
                new Endereco("Av Ana Costa", "100", null, "Gonzaga", "01001-000", "Santos"),
                LocalTime.of(8, 0),
                LocalTime.of(20, 0),
                30,
                List.of()
        );
    }

    @Nested
    @DisplayName("executar()")
    class Executar {
        private final NovaAvaliacaoDTO dto = new NovaAvaliacaoDTO(AGENDAMENTO_ID, 5, "Excelente!");

        @Test
        @DisplayName("deve salvar avaliação e atualizar médias do profissional e estabelecimento")
        void deveSalvarAvaliacaoEAtualizarMediasDoProfissionalEEstabelecimento() {
            Avaliacao avaliacaoSalva = Avaliacao.criar(AGENDAMENTO_ID, 5, "Excelente!");
            
            when(agendamentoRepositorioPort.buscarPorId(AGENDAMENTO_ID)).thenReturn(Optional.of(agendamentoConcluido));
            when(avaliacaoRepositorioPort.existePorAgendamentoId(AGENDAMENTO_ID)).thenReturn(false);
            when(avaliacaoRepositorioPort.salvar(any())).thenReturn(avaliacaoSalva);
            when(profissionalRepositorioPort.buscarPorId(PROFISSIONAL_ID)).thenReturn(Optional.of(profissional));
            when(estabelecimentoRepositorioPort.buscarPorId(ESTABELECIMENTO_ID)).thenReturn(Optional.of(estabelecimento));
            when(avaliacaoRepositorioPort.listarPorProfissional(PROFISSIONAL_ID)).thenReturn(List.of(avaliacaoSalva));
            when(avaliacaoRepositorioPort.listarPorEstabelecimento(ESTABELECIMENTO_ID)).thenReturn(List.of(avaliacaoSalva));

            assertDoesNotThrow(() -> useCase.executar(dto));

            verify(avaliacaoRepositorioPort).salvar(any());
            verify(profissionalRepositorioPort).salvar(profissional);
            verify(estabelecimentoRepositorioPort).salvar(estabelecimento);
            assertEquals(5.0, profissional.getNotaMedia());
            assertEquals(5.0, estabelecimento.getNotaMedia());
        }

        @Test
        @DisplayName("deve lançar exceção quando agendamento não é encontrado")
        void deveLancarExcecaoQuandoAgendamentoNaoEncontrado() {
            when(agendamentoRepositorioPort.buscarPorId(AGENDAMENTO_ID)).thenReturn(Optional.empty());

            assertThrows(EntidadeNaoEncontradaException.class, () -> useCase.executar(dto));
            verify(avaliacaoRepositorioPort, never()).salvar(any());
        }

        @Test
        @DisplayName("deve lançar exceção quando agendamento está PENDENTE")
        void deveLancarExcecaoQuandoAgendamentoEstaPendente() {
            Agendamento pendente = Agendamento.criar(CLIENTE_ID, PROFISSIONAL_ID, SERVICO_ID, INICIO, 30);
            
            when(agendamentoRepositorioPort.buscarPorId(AGENDAMENTO_ID)).thenReturn(Optional.of(pendente));

            BusinessException ex = assertThrows(BusinessException.class, () -> useCase.executar(dto));
            
            assertTrue(ex.getMessage().contains("PENDENTE"));
            verify(avaliacaoRepositorioPort, never()).salvar(any());
        }

        @Test
        @DisplayName("deve lançar exceção quando agendamento está CONFIRMADO")
        void deveLancarExcecaoQuandoAgendamentoEstaConfirmado() {
            Agendamento confirmado = Agendamento.criar(CLIENTE_ID, PROFISSIONAL_ID, SERVICO_ID, INICIO, 30).confirmar();
            when(agendamentoRepositorioPort.buscarPorId(AGENDAMENTO_ID)).thenReturn(Optional.of(confirmado));

            assertThrows(BusinessException.class, () -> useCase.executar(dto));
            verify(avaliacaoRepositorioPort, never()).salvar(any());
        }

        @Test
        @DisplayName("deve lançar exceção quando agendamento já foi avaliado")
        void deveLancarExcecaoQuandoAgendamentoJaFoiAvaliado() {
            when(agendamentoRepositorioPort.buscarPorId(AGENDAMENTO_ID)).thenReturn(Optional.of(agendamentoConcluido));
            when(avaliacaoRepositorioPort.existePorAgendamentoId(AGENDAMENTO_ID)).thenReturn(true);

            BusinessException ex = assertThrows(BusinessException.class, () -> useCase.executar(dto));
            
            assertTrue(ex.getMessage().contains("já foi avaliado"));
            verify(avaliacaoRepositorioPort, never()).salvar(any());
        }

        @Test
        @DisplayName("deve lançar exceção quando profissional não é encontrado")
        void deveLancarExcecaoQuandoProfissionalNaoEncontrado() {
            when(agendamentoRepositorioPort.buscarPorId(AGENDAMENTO_ID)).thenReturn(Optional.of(agendamentoConcluido));
            when(avaliacaoRepositorioPort.existePorAgendamentoId(AGENDAMENTO_ID)).thenReturn(false);
            when(avaliacaoRepositorioPort.salvar(any())).thenReturn(Avaliacao.criar(AGENDAMENTO_ID, 5, null));
            when(profissionalRepositorioPort.buscarPorId(PROFISSIONAL_ID)).thenReturn(Optional.empty());

            assertThrows(EntidadeNaoEncontradaException.class, () -> useCase.executar(dto));
            verify(estabelecimentoRepositorioPort, never()).buscarPorId(any());
        }

        @Test
        @DisplayName("deve lançar exceção quando estabelecimento não é encontrado")
        void deveLancarExcecaoQuandoEstabelecimentoNaoEncontrado() {
            when(agendamentoRepositorioPort.buscarPorId(AGENDAMENTO_ID)).thenReturn(Optional.of(agendamentoConcluido));
            when(avaliacaoRepositorioPort.existePorAgendamentoId(AGENDAMENTO_ID)).thenReturn(false);
            when(avaliacaoRepositorioPort.salvar(any())).thenReturn(Avaliacao.criar(AGENDAMENTO_ID, 5, null));
            when(profissionalRepositorioPort.buscarPorId(PROFISSIONAL_ID)).thenReturn(Optional.of(profissional));
            when(estabelecimentoRepositorioPort.buscarPorId(ESTABELECIMENTO_ID)).thenReturn(Optional.empty());

            assertThrows(EntidadeNaoEncontradaException.class, () -> useCase.executar(dto));
            verify(profissionalRepositorioPort, never()).salvar(any());
        }
    }

    @Nested
    @DisplayName("listarPorEstabelecimento()")
    class ListarPorEstabelecimento {

        @Test
        @DisplayName("deve retornar lista de avaliações com nome do profissional")
        void deveRetornarListaDeAvaliacoesComNomeDosProfissionais() {
            Avaliacao avaliacao = Avaliacao.criar(AGENDAMENTO_ID, 4, "Bom atendimento");
            
            when(estabelecimentoRepositorioPort.buscarPorId(ESTABELECIMENTO_ID)).thenReturn(Optional.of(estabelecimento));
            when(avaliacaoRepositorioPort.listarPorEstabelecimento(ESTABELECIMENTO_ID)).thenReturn(List.of(avaliacao));
            when(agendamentoRepositorioPort.buscarPorId(avaliacao.agendamentoId())).thenReturn(Optional.of(agendamentoConcluido));
            when(profissionalRepositorioPort.buscarPorId(PROFISSIONAL_ID)).thenReturn(Optional.of(profissional));

            List<DetalhesAvaliacaoDTO> resultado = useCase.listarPorEstabelecimento(ESTABELECIMENTO_ID);

            assertEquals(1, resultado.size());
            assertEquals(4, resultado.get(0).nota());
            assertEquals("Ana Silva", resultado.get(0).nomeProfissional());
        }

        @Test
        @DisplayName("deve retornar lista vazia quando não há avaliações")
        void deveRetornarListaVaziaQuandoNaoHaAvaliacoes() {
            when(estabelecimentoRepositorioPort.buscarPorId(ESTABELECIMENTO_ID)).thenReturn(Optional.of(estabelecimento));
            when(avaliacaoRepositorioPort.listarPorEstabelecimento(ESTABELECIMENTO_ID)).thenReturn(List.of());

            List<DetalhesAvaliacaoDTO> resultado = useCase.listarPorEstabelecimento(ESTABELECIMENTO_ID);

            assertTrue(resultado.isEmpty());
        }

        @Test
        @DisplayName("deve lançar exceção quando estabelecimento não é encontrado")
        void deveLancarExcecaoQuandoEstabelecimentoNaoEncontrado() {
            when(estabelecimentoRepositorioPort.buscarPorId(ESTABELECIMENTO_ID)).thenReturn(Optional.empty());

            assertThrows(EntidadeNaoEncontradaException.class, () -> useCase.listarPorEstabelecimento(ESTABELECIMENTO_ID));
        }
    }
}
