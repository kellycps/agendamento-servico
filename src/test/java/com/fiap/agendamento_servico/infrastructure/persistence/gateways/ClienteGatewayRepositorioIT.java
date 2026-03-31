package com.fiap.agendamento_servico.infrastructure.persistence.gateways;

import com.fiap.agendamento_servico.domain.entities.Cliente;
import com.fiap.agendamento_servico.infrastructure.persistence.repositories.JpaClienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Repositório: ClienteGateway")
class ClienteGatewayRepositorioIT {

    @Autowired
    private JpaClienteRepository clienteRepository;

    private ClienteGateway gateway;

    @BeforeEach
    void setUp() {
        gateway = new ClienteGateway(clienteRepository);
    }

    @Nested
    @DisplayName("salvar() e buscarPorId()")
    class SalvarEBuscar {
        @Test
        @DisplayName("deve persistir cliente e retornar por ID")
        void devePersistirClienteEBuscarPorId() {
            Cliente cliente = Cliente.criar("Ana Silva", "11999990001", "ana@email.com");
            Cliente salvo = gateway.salvar(cliente);

            Optional<Cliente> encontrado = gateway.buscarPorId(salvo.id());
            
            assertThat(encontrado).isPresent();
            assertThat(encontrado.get().nome()).isEqualTo("Ana Silva");
            assertThat(encontrado.get().email()).isEqualTo("ana@email.com");
            assertThat(encontrado.get().telefone()).isEqualTo("11999990001");
        }

        @Test
        @DisplayName("deve retornar Optional vazio para ID inexistente")
        void deveRetornarOptionalVazioParaIdInexistente() {
            Optional<Cliente> resultado = gateway.buscarPorId(UUID.randomUUID());
            
            assertThat(resultado).isEmpty();
        }

        @Test
        @DisplayName("deve atualizar dados do cliente ao salvar com mesmo ID")
        void deveAtualizarDadosDoClienteAoSalvarComMesmoId() {
            Cliente original = gateway.salvar(Cliente.criar("Bruno Santos", "11999990002", "bruno@email.com"));
            Cliente atualizado = new Cliente(original.id(), "Bruno Santos Atualizado", "11999990002", "bruno@email.com");
            
            gateway.salvar(atualizado);

            assertThat(gateway.buscarPorId(original.id()).orElseThrow().nome()).isEqualTo("Bruno Santos Atualizado");
            assertThat(clienteRepository.count()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("listar()")
    class Listar {
        @Test
        @DisplayName("deve retornar lista vazia quando não há clientes")
        void deveRetornarListaVaziaQuandoNaoHaClientes() {
            assertThat(gateway.listar()).isEmpty();
        }

        @Test
        @DisplayName("deve retornar todos os clientes cadastrados")
        void deveRetornarTodosOsClientesCadastrados() {
            gateway.salvar(Cliente.criar("Carlos Pereira", "11999990003", "carlos@email.com"));
            gateway.salvar(Cliente.criar("Daniela Ramos", "11999990004", "daniela@email.com"));

            List<Cliente> resultado = gateway.listar();

            assertThat(resultado).hasSize(2);
            assertThat(resultado).extracting(Cliente::nome).containsExactlyInAnyOrder("Carlos Pereira", "Daniela Ramos");
        }
    }

    @Nested
    @DisplayName("remover()")
    class Remover {
        @Test
        @DisplayName("deve remover cliente e retornar lista vazia")
        void deveRemoverClienteERetornarListaVazia() {
            Cliente salvo = gateway.salvar(Cliente.criar("Eduardo Costa", "11999990005", "edu@email.com"));

            gateway.remover(salvo.id());

            assertThat(gateway.buscarPorId(salvo.id())).isEmpty();
            assertThat(clienteRepository.count()).isZero();
        }
    }
}
