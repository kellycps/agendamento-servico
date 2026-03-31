package com.fiap.agendamento_servico.application.usecases;

import com.fiap.agendamento_servico.application.dto.DetalhesClienteDTO;
import com.fiap.agendamento_servico.application.ports.out.ClienteRepositorioPort;
import com.fiap.agendamento_servico.domain.entities.Cliente;
import com.fiap.agendamento_servico.domain.exceptions.EntidadeNaoEncontradaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GerenciarClienteUseCase")
class GerenciarClienteUseCaseTest {

    @Mock
    private ClienteRepositorioPort clienteRepositorioPort;

    @InjectMocks
    private GerenciarClienteUseCase useCase;

    private static final UUID ID      = UUID.randomUUID();
    private static final String NOME  = "Maria Pereira";
    private static final String FONE  = "(11) 99999-0000";
    private static final String EMAIL = "maria@email.com";

    private Cliente clienteSalvo;

    @BeforeEach
    void setUp() {
        clienteSalvo = new Cliente(ID, NOME, FONE, EMAIL);
    }

    @Nested
    @DisplayName("cadastrar()")
    class Cadastrar {
        @Test
        @DisplayName("deve salvar cliente e retornar DTO com dados corretos")
        void deveSalvarClienteERetornarDtoComDados() {
            when(clienteRepositorioPort.salvar(any(Cliente.class))).thenReturn(clienteSalvo);

            DetalhesClienteDTO dto = useCase.cadastrar(NOME, FONE, EMAIL);

            assertEquals(ID, dto.id());
            assertEquals(NOME, dto.nome());
            assertEquals(FONE, dto.telefone());
            assertEquals(EMAIL, dto.email());
            verify(clienteRepositorioPort).salvar(any(Cliente.class));
        }
    }

    @Nested
    @DisplayName("buscar()")
    class Buscar {
        @Test
        @DisplayName("deve retornar DTO quando cliente existe")
        void deveRetornarDtoQuandoClienteExiste() {
            when(clienteRepositorioPort.buscarPorId(ID)).thenReturn(Optional.of(clienteSalvo));

            DetalhesClienteDTO dto = useCase.buscar(ID);

            assertEquals(ID, dto.id());
            assertEquals(NOME, dto.nome());
        }

        @Test
        @DisplayName("deve lançar exceção quando cliente não existe")
        void deveLancarExcecaoQuandoClienteNaoExiste() {
            when(clienteRepositorioPort.buscarPorId(ID)).thenReturn(Optional.empty());

            assertThrows(EntidadeNaoEncontradaException.class, () -> useCase.buscar(ID));
        }

        @Test
        @DisplayName("deve lançar exceção quando id é nulo")
        void deveLancarExcecaoQuandoIdEhNulo() {
            assertThrows(IllegalArgumentException.class, () -> useCase.buscar(null));
        }
    }

    @Nested
    @DisplayName("listar()")
    class Listar {
        @Test
        @DisplayName("deve retornar lista de DTOs com todos os clientes")
        void deveRetornarListaDeDtosComTodosOsClientes() {
            Cliente cliente2 = new Cliente(UUID.randomUUID(), "Bruno", "22", "bruno@email.com");
            when(clienteRepositorioPort.listar()).thenReturn(List.of(clienteSalvo, cliente2));

            List<DetalhesClienteDTO> lista = useCase.listar();

            assertEquals(2, lista.size());
        }

        @Test
        @DisplayName("deve retornar lista vazia quando não há clientes")
        void deveRetornarListaVaziaQuandoNaoHaClientes() {
            when(clienteRepositorioPort.listar()).thenReturn(List.of());

            assertTrue(useCase.listar().isEmpty());
        }
    }

    @Nested
    @DisplayName("atualizar()")
    class Atualizar {
        @Test
        @DisplayName("deve atualizar e retornar DTO com novos dados")
        void deveAtualizarERetornarDtoComNovosDados() {
            Cliente clienteAtualizado = new Cliente(ID, "Novo Nome", "(21) 88888-0000", "novo@email.com");
            
            when(clienteRepositorioPort.buscarPorId(ID)).thenReturn(Optional.of(clienteSalvo));
            when(clienteRepositorioPort.salvar(any(Cliente.class))).thenReturn(clienteAtualizado);

            DetalhesClienteDTO dto = useCase.atualizar(ID, "Novo Nome", "(21) 88888-0000", "novo@email.com");

            assertEquals("Novo Nome", dto.nome());
            assertEquals("novo@email.com", dto.email());
        }

        @Test
        @DisplayName("deve lançar exceção quando cliente não existe")
        void deveLancarExcecaoQuandoClienteNaoExiste() {
            when(clienteRepositorioPort.buscarPorId(ID)).thenReturn(Optional.empty());

            assertThrows(EntidadeNaoEncontradaException.class, () -> useCase.atualizar(ID, NOME, FONE, EMAIL));
        }
    }

    @Nested
    @DisplayName("deletar()")
    class Deletar {
        @Test
        @DisplayName("deve remover o cliente quando ele existe")
        void deveRemoverClienteQuandoEleExiste() {
            when(clienteRepositorioPort.buscarPorId(ID)).thenReturn(Optional.of(clienteSalvo));
            doNothing().when(clienteRepositorioPort).remover(ID);

            assertDoesNotThrow(() -> useCase.deletar(ID));
            verify(clienteRepositorioPort).remover(ID);
        }

        @Test
        @DisplayName("deve lançar exceção quando cliente não existe")
        void deveLancarExcecaoQuandoClienteNaoExiste() {
            when(clienteRepositorioPort.buscarPorId(ID)).thenReturn(Optional.empty());

            assertThrows(EntidadeNaoEncontradaException.class, () -> useCase.deletar(ID));
            verify(clienteRepositorioPort, never()).remover(any());
        }
    }
}
