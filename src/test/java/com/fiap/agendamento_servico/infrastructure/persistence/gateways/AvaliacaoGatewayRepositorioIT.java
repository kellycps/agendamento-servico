package com.fiap.agendamento_servico.infrastructure.persistence.gateways;

import com.fiap.agendamento_servico.domain.entities.Agendamento;
import com.fiap.agendamento_servico.domain.entities.Avaliacao;
import com.fiap.agendamento_servico.infrastructure.persistence.entities.EnderecoEmbeddable;
import com.fiap.agendamento_servico.infrastructure.persistence.entities.EstabelecimentoEntity;
import com.fiap.agendamento_servico.infrastructure.persistence.entities.ProfissionalEntity;
import com.fiap.agendamento_servico.infrastructure.persistence.entities.ServicoEntity;
import com.fiap.agendamento_servico.infrastructure.persistence.mappers.PersistenciaMapper;
import com.fiap.agendamento_servico.infrastructure.persistence.repositories.JpaAgendamentoRepository;
import com.fiap.agendamento_servico.infrastructure.persistence.repositories.JpaAvaliacaoRepository;
import com.fiap.agendamento_servico.infrastructure.persistence.repositories.JpaEstabelecimentoRepository;
import com.fiap.agendamento_servico.infrastructure.persistence.repositories.JpaProfissionalRepository;
import com.fiap.agendamento_servico.infrastructure.persistence.repositories.JpaServicoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(PersistenciaMapper.class)
@DisplayName("Repositório: AvaliacaoGateway")
class AvaliacaoGatewayRepositorioIT {

    @Autowired 
    private JpaAvaliacaoRepository avaliacaoRepository;
    
    @Autowired 
    private JpaAgendamentoRepository agendamentoRepository;
    
    @Autowired 
    private JpaServicoRepository servicoRepository;
    
    @Autowired 
    private JpaEstabelecimentoRepository estabelecimentoRepository;
    
    @Autowired 
    private JpaProfissionalRepository profissionalRepository;
    
    @Autowired 
    private PersistenciaMapper persistenciaMapper;

    private AvaliacaoGateway gateway;
    private AgendamentoGateway agendamentoGateway;

    private UUID estabelecimentoId;
    private UUID profissionalId;
    private UUID servicoId;
    private UUID clienteId;

    @BeforeEach
    void setUp() {
        gateway = new AvaliacaoGateway(avaliacaoRepository, agendamentoRepository, servicoRepository, persistenciaMapper);
        agendamentoGateway = new AgendamentoGateway(agendamentoRepository, persistenciaMapper);

        estabelecimentoId = UUID.randomUUID();
        profissionalId = UUID.randomUUID();
        servicoId = UUID.randomUUID();
        clienteId = UUID.randomUUID();

        EstabelecimentoEntity estab = new EstabelecimentoEntity();
        estab.setId(estabelecimentoId);
        estab.setNome("Salão Teste");
        estab.setEndereco(new EnderecoEmbeddable("Av Ana Costa", "100", null, "Gonzaga", "01001-000", "Santos"));
        estab.setHoraInicioFuncionamento(LocalTime.of(8, 0));
        estab.setHoraFimFuncionamento(LocalTime.of(20, 0));
        estab.setIntervaloMinutosPadrao(30);
        estabelecimentoRepository.save(estab);

        ServicoEntity servico = new ServicoEntity();
        servico.setId(servicoId);
        servico.setNome("Corte");
        servico.setDescricao("Corte de cabelo");
        servico.setPreco(50.0);
        servico.setDuracaoMinutos(30);
        servico.setEstabelecimento(estab);
        servicoRepository.save(servico);

        ProfissionalEntity profissional = new ProfissionalEntity();
        profissional.setId(profissionalId);
        profissional.setNome("Ana");
        profissional.setEmail("ana@teste.com");
        profissional.setEstabelecimento(estab);
        profissional.setHoraInicioTrabalho(LocalTime.of(8, 0));
        profissional.setHoraFimTrabalho(LocalTime.of(18, 0));
        profissional.setServicosIds(List.of(servicoId));
        profissionalRepository.save(profissional);
    }

    private Agendamento criarAgendamento(LocalDateTime inicio) {
        Agendamento ag = Agendamento.criar(clienteId, profissionalId, servicoId, inicio, 30);
        return agendamentoGateway.salvar(ag);
    }

    @Nested
    @DisplayName("existePorAgendamentoId()")
    class ExistePorAgendamentoId {
        @Test
        @DisplayName("deve retornar false quando não existe avaliação para o agendamento")
        void deveRetornarFalseQuandoNaoExisteAvaliacaoParaOAgendamento() {
            UUID agId = criarAgendamento(LocalDateTime.of(2026, 10, 15, 10, 0)).id();

            assertThat(gateway.existePorAgendamentoId(agId)).isFalse();
        }

        @Test
        @DisplayName("deve retornar true após avaliação ser salva")
        void deveRetornarTrueAposSalvarAvaliacao() {
            UUID agId = criarAgendamento(LocalDateTime.of(2026, 10, 15, 11, 0)).id();

            gateway.salvar(Avaliacao.criar(agId, 5, "Excelente!"));

            assertThat(gateway.existePorAgendamentoId(agId)).isTrue();
        }
    }

    @Nested
    @DisplayName("salvar() e listarPorProfissional()")
    class SalvarEListarPorProfissional {
        @Test
        @DisplayName("deve persistir avaliação e listá-la por profissional")
        void devePersistirAvaliacaoEListarPorProfissional() {
            UUID agId = criarAgendamento(LocalDateTime.of(2026, 10, 15, 12, 0)).id();

            Avaliacao avaliacao = gateway.salvar(Avaliacao.criar(agId, 4, "Muito bom!"));

            assertThat(avaliacao.id()).isNotNull();
            assertThat(avaliacao.nota()).isEqualTo(4);

            List<Avaliacao> lista = gateway.listarPorProfissional(profissionalId);

            assertThat(lista).hasSize(1);
            assertThat(lista.get(0).nota()).isEqualTo(4);
        }

        @Test
        @DisplayName("deve retornar lista vazia para profissional sem avaliações")
        void deveRetornarListaVaziaParaProfissionalSemAvaliacoes() {
            assertThat(gateway.listarPorProfissional(UUID.randomUUID())).isEmpty();
        }
    }

    @Nested
    @DisplayName("listarPorEstabelecimento()")
    class ListarPorEstabelecimento {
        @Test
        @DisplayName("deve retornar avaliações do estabelecimento")
        void deveRetornarAvaliacoesDoEstabelecimento() {
            UUID ag1 = criarAgendamento(LocalDateTime.of(2026, 10, 15, 13, 0)).id();
            UUID ag2 = criarAgendamento(LocalDateTime.of(2026, 10, 15, 14, 0)).id();

            gateway.salvar(Avaliacao.criar(ag1, 5, "Ótimo!"));
            gateway.salvar(Avaliacao.criar(ag2, 3, "Regular."));

            List<Avaliacao> lista = gateway.listarPorEstabelecimento(estabelecimentoId);

            assertThat(lista).hasSize(2);
            assertThat(lista).extracting(Avaliacao::nota).containsExactlyInAnyOrder(5, 3);
        }

        @Test
        @DisplayName("deve retornar lista vazia para estabelecimento sem serviços avaliados")
        void deveRetornarListaVaziaParaEstabelecimentoSemServicosAvaliados() {
            assertThat(gateway.listarPorEstabelecimento(UUID.randomUUID())).isEmpty();
        }
    }

    @Nested
    @DisplayName("deletarPorAgendamentoId()")
    class DeletarPorAgendamentoId {
        @Test
        @DisplayName("deve deletar avaliação pelo agendamento associado")
        void deveDeletarAvaliacaoPeloAgendamentoAssociado() {
            UUID agId = criarAgendamento(LocalDateTime.of(2026, 10, 15, 15, 0)).id();
            gateway.salvar(Avaliacao.criar(agId, 5, "Perfeito!"));

            assertThat(gateway.existePorAgendamentoId(agId)).isTrue();

            gateway.deletarPorAgendamentoId(agId);

            assertThat(gateway.existePorAgendamentoId(agId)).isFalse();
        }
    }
}
