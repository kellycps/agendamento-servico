package com.fiap.agendamento_servico.infrastructure.persistence.gateways;

import com.fiap.agendamento_servico.domain.entities.Servico;
import com.fiap.agendamento_servico.infrastructure.persistence.entities.EnderecoEmbeddable;
import com.fiap.agendamento_servico.infrastructure.persistence.entities.EstabelecimentoEntity;
import com.fiap.agendamento_servico.infrastructure.persistence.mappers.PersistenciaMapper;
import com.fiap.agendamento_servico.infrastructure.persistence.repositories.JpaEstabelecimentoRepository;
import com.fiap.agendamento_servico.infrastructure.persistence.repositories.JpaServicoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(PersistenciaMapper.class)
@DisplayName("Repositório: ServicoGateway")
class ServicoGatewayRepositorioIT {

    @Autowired 
    private JpaServicoRepository servicoRepository;
    
    @Autowired 
    private JpaEstabelecimentoRepository estabelecimentoRepository;
    
    @Autowired 
    private PersistenciaMapper persistenciaMapper;

    private ServicoGateway gateway;
    private UUID estabelecimentoId;
    private UUID outroEstabelecimentoId;

    @BeforeEach
    void setUp() {
        gateway = new ServicoGateway(servicoRepository, estabelecimentoRepository, persistenciaMapper);

        estabelecimentoId = UUID.randomUUID();
        outroEstabelecimentoId = UUID.randomUUID();

        salvarEstabelecimento(estabelecimentoId, "Salão Teste");
        salvarEstabelecimento(outroEstabelecimentoId, "Outro Salão");
    }

    private void salvarEstabelecimento(UUID id, String nome) {
        EstabelecimentoEntity estab = new EstabelecimentoEntity();
        estab.setId(id);
        estab.setNome(nome);
        estab.setEndereco(new EnderecoEmbeddable("Rua A", "1", null, "Centro", "01001-000", "SP"));
        estab.setHoraInicioFuncionamento(LocalTime.of(8, 0));
        estab.setHoraFimFuncionamento(LocalTime.of(20, 0));
        estab.setIntervaloMinutosPadrao(30);
        estabelecimentoRepository.save(estab);
    }

    private Servico salvarServico(String nome, UUID estabId) {
        return gateway.salvar(new Servico(UUID.randomUUID(), nome, "Descrição de " + nome, 50.0, 30, estabId));
    }

    @Nested
    @DisplayName("salvar() e buscarPorId()")
    class SalvarEBuscar {
        @Test
        @DisplayName("deve persistir serviço e retornar por ID")
        void devePersistirServicoERetornarPorId() {
            Servico salvo = salvarServico("Corte de Cabelo", estabelecimentoId);

            Optional<Servico> encontrado = gateway.buscarPorId(salvo.id());

            assertThat(encontrado).isPresent();
            assertThat(encontrado.get().nome()).isEqualTo("Corte de Cabelo");
            assertThat(encontrado.get().preco()).isEqualTo(50.0);
            assertThat(encontrado.get().estabelecimentoId()).isEqualTo(estabelecimentoId);
        }

        @Test
        @DisplayName("deve retornar Optional vazio para ID inexistente")
        void deveRetornarOptionalVazioParaIdInexistente() {
            assertThat(gateway.buscarPorId(UUID.randomUUID())).isEmpty();
        }
    }

    @Nested
    @DisplayName("buscarPorIds()")
    class BuscarPorIds {
        @Test
        @DisplayName("deve retornar apenas os serviços com os IDs informados")
        void deveRetornarApenasOsServicosComOsIdsInformados() {
            Servico s1 = salvarServico("Corte", estabelecimentoId);
            Servico s2 = salvarServico("Barba", estabelecimentoId);
            Servico s3 = salvarServico("Manicure", estabelecimentoId);

            List<Servico> resultado = gateway.buscarPorIds(List.of(s1.id(), s2.id(),s3.id()));

            assertThat(resultado).hasSize(3);
            assertThat(resultado).extracting(Servico::nome).containsExactlyInAnyOrder("Corte", "Barba", "Manicure");
        }

        @Test
        @DisplayName("deve retornar lista vazia para lista de IDs vazios")
        void deveRetornarListaVaziaParaIdsVazios() {
            salvarServico("Corte", estabelecimentoId);

            assertThat(gateway.buscarPorIds(List.of())).isEmpty();
        }

        @Test
        @DisplayName("deve retornar lista vazia para IDs inexistentes")
        void deveRetornarListaVaziaParaIdsInexistentes() {
            assertThat(gateway.buscarPorIds(List.of(UUID.randomUUID()))).isEmpty();
        }
    }

    @Nested
    @DisplayName("listarPorEstabelecimento()")
    class ListarPorEstabelecimento {
        @Test
        @DisplayName("deve retornar apenas serviços do estabelecimento informado")
        void deveRetornarApenasServicosDoEstabelecimentoInformado() {
            salvarServico("Corte", estabelecimentoId);
            salvarServico("Barba", estabelecimentoId);
            salvarServico("Manicure", outroEstabelecimentoId);

            List<Servico> resultado = gateway.listarPorEstabelecimento(estabelecimentoId);

            assertThat(resultado).hasSize(2);
            assertThat(resultado).extracting(Servico::nome).containsExactlyInAnyOrder("Corte", "Barba");
        }

        @Test
        @DisplayName("deve retornar lista vazia para estabelecimento sem serviços")
        void deveRetornarListaVaziaParaEstabelecimentoSemServicos() {
            UUID idSemServicos = UUID.randomUUID();
            salvarEstabelecimento(idSemServicos, "Sem Serviços");

            assertThat(gateway.listarPorEstabelecimento(idSemServicos)).isEmpty();
        }
    }

    @Nested
    @DisplayName("remover()")
    class Remover {
        @Test
        @DisplayName("deve excluir serviço por ID")
        void deveExcluirServicoPorId() {
            Servico salvo = salvarServico("Serviço Temporário", estabelecimentoId);

            gateway.remover(salvo.id());

            assertThat(gateway.buscarPorId(salvo.id())).isEmpty();
        }
    }
}
