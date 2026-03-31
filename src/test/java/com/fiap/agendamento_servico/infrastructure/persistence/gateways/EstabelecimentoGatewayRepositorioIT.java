package com.fiap.agendamento_servico.infrastructure.persistence.gateways;

import com.fiap.agendamento_servico.application.dto.FiltroEstabelecimentoDTO;
import com.fiap.agendamento_servico.domain.entities.Estabelecimento;
import com.fiap.agendamento_servico.domain.valueobjects.Endereco;
import com.fiap.agendamento_servico.infrastructure.persistence.mappers.PersistenciaMapper;
import com.fiap.agendamento_servico.infrastructure.persistence.repositories.JpaEstabelecimentoRepository;
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
@DisplayName("Repositório: EstabelecimentoGateway")
class EstabelecimentoGatewayRepositorioIT {

    @Autowired 
    private JpaEstabelecimentoRepository estabelecimentoRepository;
    
    @Autowired 
    private PersistenciaMapper persistenciaMapper;

    private EstabelecimentoGateway gateway;

    private static final Endereco ENDERECO_SP = new Endereco("Rua A", "10", null, "Centro", "01001-000", "São Paulo");
    private static final Endereco ENDERECO_RJ = new Endereco("Av B", "20", null, "Centro", "20040-020", "Rio de Janeiro");

    @BeforeEach
    void setUp() {
        gateway = new EstabelecimentoGateway(estabelecimentoRepository, persistenciaMapper);
    }

    private Estabelecimento salvar(String nome, Endereco endereco) {
        return gateway.salvar(Estabelecimento.criar(nome, endereco, LocalTime.of(8, 0), LocalTime.of(20, 0), 30));
    }

    @Nested
    @DisplayName("salvar() e buscarPorId()")
    class SalvarEBuscar {
        @Test
        @DisplayName("deve persistir estabelecimento e retornar por ID")
        void devePersistirEstabelecimentoERetornarPorId() {
            Estabelecimento salvo = salvar("Salão Teste", ENDERECO_SP);

            Optional<Estabelecimento> encontrado = gateway.buscarPorId(salvo.getId());

            assertThat(encontrado).isPresent();
            assertThat(encontrado.get().getNome()).isEqualTo("Salão Teste");
            assertThat(encontrado.get().getEndereco().cidade()).isEqualTo("São Paulo");
            assertThat(encontrado.get().getIntervaloMinutosPadrao()).isEqualTo(30);
        }

        @Test
        @DisplayName("deve retornar Optional vazio para ID inexistente")
        void deveRetornarOptionalVazioParaIdInexistente() {
            assertThat(gateway.buscarPorId(UUID.randomUUID())).isEmpty();
        }

        @Test
        @DisplayName("deve atualizar nome ao salvar com ID existente")
        void deveAtualizarNomeAoSalvarComIdExistente() {
            Estabelecimento original = salvar("Salão Original", ENDERECO_SP);
            UUID id = original.getId();

            Estabelecimento comNovoNome = new Estabelecimento(id, "Salão Atualizado", ENDERECO_SP, LocalTime.of(9, 0), LocalTime.of(18, 0), 60, List.of());
            gateway.salvar(comNovoNome);

            assertThat(gateway.buscarPorId(id).orElseThrow().getNome()).isEqualTo("Salão Atualizado");
        }
    }

    @Nested
    @DisplayName("listarTodos()")
    class ListarTodos {
        @Test
        @DisplayName("deve retornar todos os estabelecimentos persistidos")
        void deveRetornarTodosOsEstabelecimentosPersistidos() {
            salvar("Salão A", ENDERECO_SP);
            salvar("Salão B", ENDERECO_RJ);

            List<Estabelecimento> lista = gateway.listarTodos();

            assertThat(lista).hasSize(2);
            assertThat(lista).extracting(Estabelecimento::getNome).containsExactlyInAnyOrder("Salão A", "Salão B");
        }
    }

    @Nested
    @DisplayName("buscarPorCidade()")
    class BuscarPorCidade {

        @Test
        @DisplayName("deve retornar apenas estabelecimentos da cidade informada")
        void deveRetornarApenasEstabelecimentosDaCidadeInformada() {
            salvar("Salão SP", ENDERECO_SP);
            salvar("Salão RJ", ENDERECO_RJ);

            List<Estabelecimento> resultado = gateway.buscarPorCidade("São Paulo");

            assertThat(resultado).hasSize(1);
            assertThat(resultado.get(0).getNome()).isEqualTo("Salão SP");
        }

        @Test
        @DisplayName("deve ser case-insensitive na busca por cidade")
        void deveSerCaseInsensitiveNaBuscaPorCidade() {
            salvar("Salão SP", ENDERECO_SP);

            assertThat(gateway.buscarPorCidade("são paulo")).hasSize(1);
            assertThat(gateway.buscarPorCidade("SÃO PAULO")).hasSize(1);
        }
    }

    @Nested
    @DisplayName("filtrar()")
    class Filtrar {
        @Test
        @DisplayName("deve retornar estabelecimentos correspondentes ao filtro por nome")
        void deveRetornarEstabelecimentosCorrespondentesAoFiltroPorNome() {
            salvar("Barber King", ENDERECO_SP);
            salvar("Beauty Studio", ENDERECO_RJ);

            FiltroEstabelecimentoDTO filtro = new FiltroEstabelecimentoDTO(null, "Barber", null, null, null);
            List<Estabelecimento> resultado = gateway.filtrar(filtro);

            assertThat(resultado).hasSize(1);
            assertThat(resultado.get(0).getNome()).isEqualTo("Barber King");
        }

        @Test
        @DisplayName("deve retornar todos estabelecimentos quando filtro está vazio")
        void deveRetornarTodosEstabelecimentosQuandoFiltroEstaVazio() {
            salvar("Salão A", ENDERECO_SP);
            salvar("Salão B", ENDERECO_RJ);

            FiltroEstabelecimentoDTO filtro = new FiltroEstabelecimentoDTO(null, null, null, null, null);

            assertThat(gateway.filtrar(filtro)).hasSize(2);
        }
    }

    @Nested
    @DisplayName("deletar()")
    class Deletar {
        @Test
        @DisplayName("deve deletar estabelecimento por ID")
        void deveDeletarEstabelecimentoPorId() {
            Estabelecimento salvo = salvar("Salão Temporário", ENDERECO_SP);

            gateway.deletar(salvo.getId());

            assertThat(gateway.buscarPorId(salvo.getId())).isEmpty();
        }
    }
}
