package com.fiap.agendamento_servico.integration;

import com.fiap.agendamento_servico.infrastructure.persistence.repositories.JpaClienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Integração: Módulo Cliente")
class ClienteIntegracaoIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JpaClienteRepository clienteRepository;

    @MockitoBean
    private ConnectionFactory connectionFactory;

    @BeforeEach
    void limparBanco() {
        clienteRepository.deleteAll();
    }

    @Nested
    @DisplayName("POST /clientes")
    class Cadastrar {
        @Test
        @DisplayName("deve cadastrar cliente e retornar 201 com dados")
        void deveCadastrarClienteERetornarDados() throws Exception {
            mockMvc.perform(post("/clientes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"nome":"Ana Lima","telefone":"11999990001","email":"ana@email.com"}
                                    """))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.sucesso").value(true))
                    .andExpect(jsonPath("$.dados.nome").value("Ana Lima"))
                    .andExpect(jsonPath("$.dados.email").value("ana@email.com"))
                    .andExpect(jsonPath("$.dados.id").isNotEmpty());
        }

        @Test
        @DisplayName("deve persistir cliente no banco após cadastro")
        void devePersistirClienteNoBancoAposCadastro() throws Exception {
            mockMvc.perform(post("/clientes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"nome":"Bruno Silva","telefone":"11999990002","email":"bruno@email.com"}
                                    """))
                    .andExpect(status().isCreated());

            org.assertj.core.api.Assertions.assertThat(clienteRepository.count()).isEqualTo(1);
            org.assertj.core.api.Assertions.assertThat(clienteRepository.findAll().getFirst().getNome()).isEqualTo("Bruno Silva");
        }

        @Test
        @DisplayName("deve retornar 400 com email inválido")
        void deveRetornarErroComEmailInvalido() throws Exception {
            mockMvc.perform(post("/clientes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"nome":"Carla","telefone":"11999990003","email":"email-invalido"}
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.sucesso").value(false));
        }
    }

    @Nested
    @DisplayName("GET /clientes")
    class Listar {
        @Test
        @DisplayName("deve retornar 204 quando não há clientes")
        void deveRetornar204QuandoNaoHaClientes() throws Exception {
            mockMvc.perform(get("/clientes"))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("deve retornar 200 com lista quando há clientes cadastrados")
        void deveRetornar200ComListaQuandoHaClientesCadastrados() throws Exception {
            mockMvc.perform(post("/clientes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {"nome":"Dani Souza","telefone":"11999990004","email":"dani@email.com"}
                            """));
            mockMvc.perform(post("/clientes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {"nome":"Eduardo Rocha","telefone":"11999990005","email":"edu@email.com"}
                            """));

            mockMvc.perform(get("/clientes"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.sucesso").value(true))
                    .andExpect(jsonPath("$.dados", hasSize(2)))
                    .andExpect(jsonPath("$.dados[*].nome", containsInAnyOrder("Dani Souza", "Eduardo Rocha")));
        }
    }

    @Nested
    @DisplayName("GET /clientes/{id}")
    class Buscar {
        @Test
        @DisplayName("deve retornar 200 com dados do cliente existente")
        void deveRetornar200ComDadosDoClienteExistente() throws Exception {
            String resposta = mockMvc.perform(post("/clientes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"nome":"Fernanda Costa","telefone":"11999990006","email":"fer@email.com"}
                                    """))
                    .andReturn().getResponse().getContentAsString();

            String id = com.jayway.jsonpath.JsonPath.read(resposta, "$.dados.id");

            mockMvc.perform(get("/clientes/{id}", id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.sucesso").value(true))
                    .andExpect(jsonPath("$.dados.nome").value("Fernanda Costa"))
                    .andExpect(jsonPath("$.dados.email").value("fer@email.com"));
        }

        @Test
        @DisplayName("deve retornar 404 quando cliente não existe")
        void deveRetornar404QuandoClienteNaoExiste() throws Exception {
            mockMvc.perform(get("/clientes/{id}", java.util.UUID.randomUUID()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.sucesso").value(false));
        }
    }

    @Nested
    @DisplayName("PUT /clientes/{id}")
    class Atualizar {
        @Test
        @DisplayName("deve atualizar nome e retornar 200 com dados atualizados")
        void deveAtualizarNomeERetornar200ComDadosAtualizados() throws Exception {
            String resposta = mockMvc.perform(post("/clientes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"nome":"Gabriel Neto","telefone":"11999990007","email":"gabriel@email.com"}
                                    """))
                    .andReturn().getResponse().getContentAsString();

            String id = com.jayway.jsonpath.JsonPath.read(resposta, "$.dados.id");

            mockMvc.perform(put("/clientes/{id}", id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"nome":"Gabriel Neto Atualizado","telefone":"11999990007","email":"gabriel@email.com"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.sucesso").value(true))
                    .andExpect(jsonPath("$.dados.nome").value("Gabriel Neto Atualizado"));
        }

        @Test
        @DisplayName("deve retornar 404 ao atualizar cliente inexistente")
        void deveRetornar404AoAtualizarClienteInexistente() throws Exception {
            mockMvc.perform(put("/clientes/{id}", java.util.UUID.randomUUID())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"nome":"Ninguém","telefone":"11999990000","email":"x@email.com"}
                                    """))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /clientes/{id}")
    class Deletar {
        @Test
        @DisplayName("deve deletar cliente e retornar 204")
        void deveDeletarClienteERetornar204() throws Exception {
            String resposta = mockMvc.perform(post("/clientes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"nome":"Helena Martins","telefone":"11999990008","email":"helena@email.com"}
                                    """))
                    .andReturn().getResponse().getContentAsString();

            String id = com.jayway.jsonpath.JsonPath.read(resposta, "$.dados.id");

            mockMvc.perform(delete("/clientes/{id}", id)).andExpect(status().isNoContent());

            org.assertj.core.api.Assertions.assertThat(clienteRepository.count()).isZero();
        }

        @Test
        @DisplayName("deve retornar 404 ao deletar cliente inexistente")
        void deveRetornar404AoDeletarClienteInexistente() throws Exception {
            mockMvc.perform(delete("/clientes/{id}", java.util.UUID.randomUUID())).andExpect(status().isNotFound());
        }
    }

    @Test
    @DisplayName("fluxo completo CRUD de cliente")
    void fluxoCompletoCrud() throws Exception {
        String respostaCadastro = mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Igor Ferreira","telefone":"11999990009","email":"igor@email.com"}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        
        String id = com.jayway.jsonpath.JsonPath.read(respostaCadastro, "$.dados.id");

        mockMvc.perform(get("/clientes/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dados.nome").value("Igor Ferreira"));

        mockMvc.perform(put("/clientes/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Igor Ferreira Jr","telefone":"11999990009","email":"igor@email.com"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dados.nome").value("Igor Ferreira Jr"));

        mockMvc.perform(delete("/clientes/{id}", id)).andExpect(status().isNoContent());

        mockMvc.perform(get("/clientes/{id}", id)).andExpect(status().isNotFound());
    }
}
