package com.fiap.agendamento_servico.infrastructure.persistence.gateways;

import com.fiap.agendamento_servico.domain.entities.Agendamento;
import com.fiap.agendamento_servico.domain.enums.StatusAgendamento;
import com.fiap.agendamento_servico.infrastructure.persistence.entities.EnderecoEmbeddable;
import com.fiap.agendamento_servico.infrastructure.persistence.entities.EstabelecimentoEntity;
import com.fiap.agendamento_servico.infrastructure.persistence.entities.ProfissionalEntity;
import com.fiap.agendamento_servico.infrastructure.persistence.entities.ServicoEntity;
import com.fiap.agendamento_servico.infrastructure.persistence.mappers.PersistenciaMapper;
import com.fiap.agendamento_servico.infrastructure.persistence.repositories.JpaAgendamentoRepository;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(PersistenciaMapper.class)
@DisplayName("Repositório: AgendamentoGateway")
class AgendamentoGatewayRepositorioIT {

    @Autowired 
    private JpaAgendamentoRepository agendamentoRepository;
    
    @Autowired 
    private JpaEstabelecimentoRepository estabelecimentoRepository;
    
    @Autowired 
    private JpaProfissionalRepository profissionalRepository;
    
    @Autowired 
    private JpaServicoRepository servicoRepository;
    
    @Autowired 
    private PersistenciaMapper persistenciaMapper;

    private AgendamentoGateway gateway;

    private UUID profissionalId;
    private UUID servicoId;
    private UUID clienteId;

    @BeforeEach
    void setUp() {
        gateway = new AgendamentoGateway(agendamentoRepository, persistenciaMapper);

        profissionalId = UUID.randomUUID();
        servicoId = UUID.randomUUID();
        clienteId = UUID.randomUUID();

        EstabelecimentoEntity estab = new EstabelecimentoEntity();
        estab.setId(UUID.randomUUID());
        estab.setNome("Salão Teste");
        estab.setEndereco(new EnderecoEmbeddable("Avenida Ana Costa", "100", null, "Gonzaga", "01001-000", "Santos"));
        estab.setHoraInicioFuncionamento(LocalTime.of(8, 0));
        estab.setHoraFimFuncionamento(LocalTime.of(20, 0));
        estab.setIntervaloMinutosPadrao(30);
        estabelecimentoRepository.save(estab);

        ServicoEntity servico = new ServicoEntity();
        servico.setId(servicoId);
        servico.setNome("Serviço Teste");
        servico.setDescricao("Descrição");
        servico.setPreco(50.0);
        servico.setDuracaoMinutos(30);
        servico.setEstabelecimento(estab);
        servicoRepository.save(servico);

        ProfissionalEntity profissional = new ProfissionalEntity();
        profissional.setId(profissionalId);
        profissional.setNome("Profissional Teste");
        profissional.setEmail("prof@teste.com");
        profissional.setEstabelecimento(estab);
        profissional.setHoraInicioTrabalho(LocalTime.of(8, 0));
        profissional.setHoraFimTrabalho(LocalTime.of(18, 0));
        profissional.setServicosIds(List.of(servicoId));
        profissionalRepository.save(profissional);
    }

    @Nested
    @DisplayName("salvar() e buscarPorId()")
    class SalvarEBuscar {
        @Test
        @DisplayName("deve criar agendamento e retornar por ID")
        void deveCriarAgendamentoERetornarPorId() {
            Agendamento agendamentoCriado = Agendamento.criar(clienteId, profissionalId, servicoId,LocalDateTime.of(2026, 10, 15, 10, 0), 30);
            Agendamento agendamentoSalvo = gateway.salvar(agendamentoCriado);
            Optional<Agendamento> agendamentoEncontrado = gateway.buscarPorId(agendamentoSalvo.id());

            assertThat(agendamentoEncontrado).isPresent();
            assertThat(agendamentoEncontrado.get().clienteId()).isEqualTo(clienteId);
            assertThat(agendamentoEncontrado.get().status()).isEqualTo(StatusAgendamento.PENDENTE);
            assertThat(agendamentoEncontrado.get().dataHoraInicio()).isEqualTo(LocalDateTime.of(2026, 10, 15, 10, 0));
        }

        @Test
        @DisplayName("deve retornar Optional vazio para ID inexistente")
        void deveRetornarOptionalVazioParaIdInexistente() {
            Optional<Agendamento> agendamentoResultado = gateway.buscarPorId(UUID.randomUUID());
            
            assertThat(agendamentoResultado).isEmpty();
        }
    }

    @Nested
    @DisplayName("Atualização de status via salvar()")
    class AtualizacaoDeStatus {
        @Test
        @DisplayName("deve atualizar status do agendamento para CONFIRMADO")
        void deveAtualizarStatusDoAgendamentoParaConfirmado() {
            Agendamento agendamentoCriado = Agendamento.criar(clienteId, profissionalId, servicoId, LocalDateTime.of(2026, 10, 15, 11, 0), 30);
            Agendamento agendamentoSalvo = gateway.salvar(agendamentoCriado);

            gateway.salvar(agendamentoSalvo.confirmar());

            assertThat(gateway.buscarPorId(agendamentoSalvo.id()).orElseThrow().status()).isEqualTo(StatusAgendamento.CONFIRMADO);
        }

        @Test
        @DisplayName("deve atualizar status do agendamento para CANCELADO")
        void deveAtualizarStatusDoAgendamentoParaCancelado() {
            Agendamento agendamentoCriado = Agendamento.criar(clienteId, profissionalId, servicoId, LocalDateTime.of(2026, 10, 15, 12, 0), 30);
            Agendamento agendamentoSalvo = gateway.salvar(agendamentoCriado);

            gateway.salvar(agendamentoSalvo.cancelar());

            assertThat(gateway.buscarPorId(agendamentoSalvo.id()).orElseThrow().status()).isEqualTo(StatusAgendamento.CANCELADO);
        }

        @Test
        @DisplayName("deve atualizar status do agendamento para CONCLUIDO")
        void deveAtualizarStatusDoAgendamentoParaConcluido() {
            Agendamento agendamentoCriado = Agendamento.criar(clienteId, profissionalId, servicoId, LocalDateTime.of(2026, 10, 15, 13, 0), 30);
            Agendamento agendamentoSalvo = gateway.salvar(agendamentoCriado);

            gateway.salvar(agendamentoSalvo.confirmar());
            gateway.salvar(gateway.buscarPorId(agendamentoSalvo.id()).orElseThrow().concluir());

            assertThat(gateway.buscarPorId(agendamentoSalvo.id()).orElseThrow().status()).isEqualTo(StatusAgendamento.CONCLUIDO);
        }
    }

    @Nested
    @DisplayName("buscarPorProfissionalEData()")
    class BuscarPorProfissionalEData {
        @Test
        @DisplayName("deve retornar agendamentos em uma data específica")
        void deveRetornarAgendamentosEmUmaDataEspecifica() {
            LocalDate data = LocalDate.of(2026, 11, 10);

            gateway.salvar(Agendamento.criar(clienteId, profissionalId, servicoId, data.atTime(9, 0), 30));
            gateway.salvar(Agendamento.criar(clienteId, profissionalId, servicoId, data.atTime(10, 0), 30));
            gateway.salvar(Agendamento.criar(clienteId, profissionalId, servicoId, data.plusDays(1).atTime(9, 0), 30));

            List<Agendamento> resultado = gateway.buscarPorProfissionalEData(profissionalId, data);

            assertThat(resultado).hasSize(2);
            assertThat(resultado).allMatch(a -> a.dataHoraInicio().toLocalDate().equals(data));
        }

        @Test
        @DisplayName("deve retornar lista vazia quando não há agendamentos na data")
        void deveRetornarListaVaziaQuandoNaoHaAgendamentosNaData() {
            List<Agendamento> resultado = gateway.buscarPorProfissionalEData(profissionalId, LocalDate.of(2026, 12, 25));
            
            assertThat(resultado).isEmpty();
        }
    }

    @Nested
    @DisplayName("listarPorCliente()")
    class ListarPorCliente {
        @Test
        @DisplayName("deve retornar todos os agendamentos do cliente")
        void deveRetornarTodosOsAgendamentosDoCliente() {
            UUID outroClienteId = UUID.randomUUID();

            gateway.salvar(Agendamento.criar(clienteId, profissionalId, servicoId, LocalDateTime.of(2026, 11, 20, 9, 0), 30));
            gateway.salvar(Agendamento.criar(clienteId, profissionalId, servicoId, LocalDateTime.of(2026, 11, 20, 10, 0), 30));
            gateway.salvar(Agendamento.criar(outroClienteId, profissionalId, servicoId, LocalDateTime.of(2026, 11, 20, 11, 0), 30));

            List<Agendamento> resultado = gateway.listarPorCliente(clienteId);

            assertThat(resultado).hasSize(2);
            assertThat(resultado).allMatch(a -> a.clienteId().equals(clienteId));
        }
    }

    @Nested
    @DisplayName("deletar()")
    class Deletar {
        @Test
        @DisplayName("deve remover agendamento do banco")
        void deveRemoverAgendamentoDoBanco() {
            Agendamento ag = gateway.salvar(Agendamento.criar(clienteId, profissionalId, servicoId, LocalDateTime.of(2026, 11, 25, 9, 0), 30));

            gateway.deletar(ag.id());

            assertThat(gateway.buscarPorId(ag.id())).isEmpty();
        }
    }

    @Nested
    @DisplayName("existePorServicoId()")
    class ExistePorServico {
        @Test
        @DisplayName("deve retornar true quando há agendamento para o serviço")
        void deveRetornarTrueQuandoHaAgendamentoParaOServico() {
            gateway.salvar(Agendamento.criar(clienteId, profissionalId, servicoId,LocalDateTime.of(2026, 11, 30, 9, 0), 30));

            assertThat(gateway.existePorServicoId(servicoId)).isTrue();
        }

        @Test
        @DisplayName("deve retornar false quando não há agendamento para o serviço")
        void deveRetornarFalseQuandoNaoHaAgendamentoParaOServico() {
            assertThat(gateway.existePorServicoId(UUID.randomUUID())).isFalse();
        }
    }
}
