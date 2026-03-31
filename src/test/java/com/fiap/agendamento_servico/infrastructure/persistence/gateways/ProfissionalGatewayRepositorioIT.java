package com.fiap.agendamento_servico.infrastructure.persistence.gateways;

import com.fiap.agendamento_servico.domain.entities.Profissional;
import com.fiap.agendamento_servico.infrastructure.persistence.entities.EnderecoEmbeddable;
import com.fiap.agendamento_servico.infrastructure.persistence.entities.EstabelecimentoEntity;
import com.fiap.agendamento_servico.infrastructure.persistence.mappers.PersistenciaMapper;
import com.fiap.agendamento_servico.infrastructure.persistence.repositories.JpaEstabelecimentoRepository;
import com.fiap.agendamento_servico.infrastructure.persistence.repositories.JpaProfissionalRepository;
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
@DisplayName("Repositório: ProfissionalGateway")
class ProfissionalGatewayRepositorioIT {

    @Autowired 
    private JpaProfissionalRepository profissionalRepository;
    
    @Autowired 
    private JpaEstabelecimentoRepository estabelecimentoRepository;
    
    @Autowired 
    private PersistenciaMapper persistenciaMapper;

    private ProfissionalGateway gateway;
    private UUID estabelecimentoId;
    private UUID outroEstabelecimentoId;

    @BeforeEach
    void setUp() {
        gateway = new ProfissionalGateway(profissionalRepository, estabelecimentoRepository, persistenciaMapper);

        estabelecimentoId = UUID.randomUUID();
        outroEstabelecimentoId = UUID.randomUUID();

        salvarEstabelecimento(estabelecimentoId, "Salão Teste");
        salvarEstabelecimento(outroEstabelecimentoId, "Outro Salão");
    }

    private void salvarEstabelecimento(UUID id, String nome) {
        EstabelecimentoEntity estab = new EstabelecimentoEntity();
        estab.setId(id);
        estab.setNome(nome);
        estab.setEndereco(new EnderecoEmbeddable("Av Ana Costa", "100", null, "Gonzaga", "01001-000", "Santos"));
        estab.setHoraInicioFuncionamento(LocalTime.of(8, 0));
        estab.setHoraFimFuncionamento(LocalTime.of(20, 0));
        estab.setIntervaloMinutosPadrao(30);
        estabelecimentoRepository.save(estab);
    }

    private Profissional salvarProfissional(String nome, UUID estabId) {
        Profissional p = Profissional.vincular(nome, estabId);
        return gateway.salvar(p);
    }

    @Nested
    @DisplayName("salvar() e buscarPorId()")
    class SalvarEBuscar {
        @Test
        @DisplayName("deve persistir profissional e retornar por ID")
        void devePersistirProfissionalERetornarPorId() {
            Profissional salvo = salvarProfissional("Ana Silva", estabelecimentoId);

            Optional<Profissional> encontrado = gateway.buscarPorId(salvo.getId());

            assertThat(encontrado).isPresent();
            assertThat(encontrado.get().getNome()).isEqualTo("Ana Silva");
            assertThat(encontrado.get().getEstabelecimentoId()).isEqualTo(estabelecimentoId);
        }

        @Test
        @DisplayName("deve retornar Optional vazio para ID inexistente")
        void deveRetornarOptionalVazioParaIdInexistente() {
            assertThat(gateway.buscarPorId(UUID.randomUUID())).isEmpty();
        }

    }

    @Nested
    @DisplayName("listarPorEstabelecimento()")
    class ListarPorEstabelecimento {
        @Test
        @DisplayName("deve retornar apenas profissionais do estabelecimento informado")
        void deveRetornarApenasProfissionaisDoEstabelecimentoInformado() {
            salvarProfissional("Ana Silva",    estabelecimentoId);
            salvarProfissional("Bruno Souza",  estabelecimentoId);
            salvarProfissional("Carlos Lima", outroEstabelecimentoId);

            List<Profissional> resultado = gateway.listarPorEstabelecimento(estabelecimentoId);

            assertThat(resultado).hasSize(2);
            assertThat(resultado).extracting(Profissional::getNome).containsExactlyInAnyOrder("Ana Silva", "Bruno Souza");
        }

        @Test
        @DisplayName("deve retornar lista vazia para estabelecimento sem profissionais")
        void deveRetornarListaVaziaParaEstabelecimentoSemProfissionais() {
            UUID idSemProfissional = UUID.randomUUID();
            salvarEstabelecimento(idSemProfissional, "Sem Profissionais");

            assertThat(gateway.listarPorEstabelecimento(idSemProfissional)).isEmpty();
        }
    }

    @Nested
    @DisplayName("listarPorEstabelecimentoENotaMinima()")
    class ListarPorNotaMinima {
        @Test
        @DisplayName("deve retornar todos os profissionais quando notaMinima é zero")
        void deveRetornarTodosOsProfissionaisQuandoNotaMinimaEZero() {
            salvarProfissional("Ana Silva",   estabelecimentoId);
            salvarProfissional("Bruno Souza", estabelecimentoId);

            List<Profissional> resultado = gateway.listarPorEstabelecimentoENotaMinima(estabelecimentoId, 0.0);

            assertThat(resultado).hasSize(2);
        }

        @Test
        @DisplayName("deve retornar lista vazia quando nenhum profissional atinge a nota mínima")
        void deveRetornarListaVaziaQuandoNenhumProfissionalAtingeANotaMinima() {
            salvarProfissional("Ana Silva", estabelecimentoId);

            List<Profissional> resultado = gateway.listarPorEstabelecimentoENotaMinima(estabelecimentoId, 3.0);

            assertThat(resultado).isEmpty();
        }
    }

    @Nested
    @DisplayName("remover()")
    class Remover {
        @Test
        @DisplayName("deve excluir profissional por ID")
        void deveExcluirProfissionalPorId() {
            Profissional salvo = salvarProfissional("Profissional Temporário", estabelecimentoId);

            gateway.remover(salvo.getId());

            assertThat(gateway.buscarPorId(salvo.getId())).isEmpty();
        }
    }
}
