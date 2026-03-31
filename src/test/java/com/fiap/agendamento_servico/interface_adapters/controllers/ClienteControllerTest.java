package com.fiap.agendamento_servico.interface_adapters.controllers;

import com.fiap.agendamento_servico.application.dto.DetalhesClienteDTO;
import com.fiap.agendamento_servico.application.ports.in.GerenciarClienteUseCasePort;
import com.fiap.agendamento_servico.domain.exceptions.EntidadeNaoEncontradaException;
import com.fiap.agendamento_servico.infrastructure.config.ConfiguracaoJson;
import com.fiap.agendamento_servico.interface_adapters.presenters.ClientePresenter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ClienteController.class)
@Import({ClientePresenter.class, ConfiguracaoJson.class})
@DisplayName("ClienteController")
class ClienteControllerTest {

    @Autowired 
    private MockMvc mockMvc;

    @MockitoBean 
    private GerenciarClienteUseCasePort gerenciarClienteUseCasePort;

    private static final UUID ID = UUID.randomUUID();
    private static final String NOME  = "Ana Silva";
    private static final String FONE  = "(11) 99999-0000";
    private static final String EMAIL = "ana@email.com";

    private DetalhesClienteDTO dto() {
        return new DetalhesClienteDTO(ID, NOME, FONE, EMAIL);
    }

    @Nested
    @DisplayName("POST /clientes")
    class Cadastrar {
        @Test
        @DisplayName("deve retornar 201 com dados do cliente criado")
        void deveRetornar201ComDadosDoClienteCriado() throws Exception {
            when(gerenciarClienteUseCasePort.cadastrar(NOME, FONE, EMAIL)).thenReturn(dto());

            mockMvc.perform(post("/clientes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"nome":"Ana Silva","telefone":"(11) 99999-0000","email":"ana@email.com"}
                                    """))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.sucesso").value(true))
                    .andExpect(jsonPath("$.dados.nome").value(NOME))
                    .andExpect(jsonPath("$.dados.email").value(EMAIL));
        }
    }

    @Nested
    @DisplayName("GET /clientes/{id}")
    class Buscar {
        @Test
        @DisplayName("deve retornar 200 com dados do cliente")
        void deveRetornar200ComDadosDoCliente() throws Exception {
            when(gerenciarClienteUseCasePort.buscar(ID)).thenReturn(dto());

            mockMvc.perform(get("/clientes/{id}", ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.dados.id").value(ID.toString()))
                    .andExpect(jsonPath("$.dados.nome").value(NOME));
        }

        @Test
        @DisplayName("deve retornar 404 quando cliente não existe")
        void deveRetornar404QuandoClienteNaoExiste() throws Exception {
            when(gerenciarClienteUseCasePort.buscar(ID))
                    .thenThrow(EntidadeNaoEncontradaException.para("Cliente", ID));

            mockMvc.perform(get("/clientes/{id}", ID))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.sucesso").value(false));
        }
    }

    @Nested
    @DisplayName("GET /clientes")
    class Listar {
        @Test
        @DisplayName("deve retornar 200 com lista de clientes")
        void deveRetornar200ComListaDeClientes() throws Exception {
            when(gerenciarClienteUseCasePort.listar()).thenReturn(List.of(dto()));

            mockMvc.perform(get("/clientes"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.dados[0].nome").value(NOME));
        }

        @Test
        @DisplayName("deve retornar 204 quando não há clientes")
        void deveRetornar204QuandoNaoHaClientes() throws Exception {
            when(gerenciarClienteUseCasePort.listar()).thenReturn(List.of());

            mockMvc.perform(get("/clientes")).andExpect(status().isNoContent());
        }
    }

    @Nested
    @DisplayName("PUT /clientes/{id}")
    class Atualizar {
        @Test
        @DisplayName("deve retornar 200 com dados atualizados")
        void deveRetornar200ComDadosAtualizados() throws Exception {
            when(gerenciarClienteUseCasePort.atualizar(eq(ID), any(), any(), any())).thenReturn(dto());

            mockMvc.perform(put("/clientes/{id}", ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"nome":"Ana Lima","telefone":"(11) 99999-0000","email":"ana@email.com"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.dados.nome").value(NOME));
        }

        @Test
        @DisplayName("deve retornar 404 quando atualizar cliente não existente")
        void deveRetornar404QuandoAtualizarClienteNaoExiste() throws Exception {
            when(gerenciarClienteUseCasePort.atualizar(eq(ID), any(), any(), any())).thenThrow(EntidadeNaoEncontradaException.para("Cliente", ID));

            mockMvc.perform(put("/clientes/{id}", ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"nome":"X","telefone":"1","email":"x@x.com"}
                                    """))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /clientes/{id}")
    class Deletar {
        @Test
        @DisplayName("deve retornar 204 ao deletar cliente existente")
        void deveRetornar204AoDeletarClienteExistente() throws Exception {
            doNothing().when(gerenciarClienteUseCasePort).deletar(ID);

            mockMvc.perform(delete("/clientes/{id}", ID)).andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("deve retornar 404 quando cliente não existe")
        void deveRetornar404QuandoClienteNaoExiste() throws Exception {
            doThrow(EntidadeNaoEncontradaException.para("Cliente", ID)).when(gerenciarClienteUseCasePort).deletar(ID);

            mockMvc.perform(delete("/clientes/{id}", ID)).andExpect(status().isNotFound());
        }
    }
}
