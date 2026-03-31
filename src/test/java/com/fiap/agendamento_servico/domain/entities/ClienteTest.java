package com.fiap.agendamento_servico.domain.entities;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Cliente")
class ClienteTest {

    @Nested
    @DisplayName("criar()")
    class Criar {
        @Test
        @DisplayName("deve criar cliente com id gerado automaticamente")
        void deveCriarClienteComIdGeradoAutomaticamente() {
            Cliente cliente = Cliente.criar("Ana Silva", "(11) 99999-0000", "ana@email.com");

            assertNotNull(cliente.id());
            assertEquals("Ana Silva", cliente.nome());
            assertEquals("(11) 99999-0000", cliente.telefone());
            assertEquals("ana@email.com", cliente.email());
        }

        @Test
        @DisplayName("deve validar que ids devem ser distintos a cada criação de cliente")
        void deveValidarQueIdsSaoDistintosACadaCriacaoDeCliente() {
            Cliente c1 = Cliente.criar("Ana Silva", "(11) 99999-0000", "ana@email.com");
            Cliente c2 = Cliente.criar("Bruno Lima", "(22) 88888-1111", "bruno@email.com");
            assertNotEquals(c1.id(), c2.id());
        }
    }

    @Nested
    @DisplayName("Validações do construtor")
    class Validacoes {
        @Test
        @DisplayName("deve lançar exceção quando nome é nulo")
        void deveLancarExcecaoQuandoNomeENulo() {
            assertThrows(IllegalArgumentException.class, () -> Cliente.criar(null, "(11) 99999-0000", "ana@email.com"));
        }

        @Test
        @DisplayName("deve lançar exceção quando nome é vazio")
        void deveLancarExcecaoQuandoNomeEVazio() {
            assertThrows(IllegalArgumentException.class,() -> Cliente.criar("  ", "(11) 99999-0000", "ana@email.com"));
        }

        @Test
        @DisplayName("deve lançar exceção quando telefone é nulo")
        void deveLancarExcecaoQuandoTelefoneENulo() {
            assertThrows(IllegalArgumentException.class, () -> Cliente.criar("Ana", null, "ana@email.com"));
        }

        @Test
        @DisplayName("deve lançar exceção quando telefone é vazio")
        void deveLancarExcecaoQuandoTelefoneEVazio() {
            assertThrows(IllegalArgumentException.class, () -> Cliente.criar("Ana", "  ", "ana@email.com"));
        }

        @Test
        @DisplayName("deve lançar exceção quando email é nulo")
        void deveLancarExcecaoQuandoEmailENulo() {
            assertThrows(IllegalArgumentException.class, () -> Cliente.criar("Ana", "(11) 99999-0000", null));
        }

        @Test
        @DisplayName("deve lançar exceção quando email é vazio")
        void deveLancarExcecaoQuandoEmailEVazio() {
            assertThrows(IllegalArgumentException.class, () -> Cliente.criar("Ana", "(11) 99999-0000", "  "));
        }

        @Test
        @DisplayName("deve lançar exceção quando email tem formato inválido")
        void deveLancarExcecaoQuandoEmailTemFormatoInvalido() {
            assertThrows(IllegalArgumentException.class, () -> Cliente.criar("Ana", "(11) 99999-0000", "nao-e-um-email"));
        }
    }
}
