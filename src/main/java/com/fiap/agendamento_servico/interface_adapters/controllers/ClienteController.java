package com.fiap.agendamento_servico.interface_adapters.controllers;

import com.fiap.agendamento_servico.application.dto.DetalhesClienteDTO;
import com.fiap.agendamento_servico.application.ports.in.GerenciarClienteUseCasePort;
import com.fiap.agendamento_servico.interface_adapters.presenters.ClientePresenter;
import com.fiap.agendamento_servico.interface_adapters.presenters.RespostaApi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Clientes", description = "Cadastro e gerenciamento de clientes")
@RestController
@RequestMapping("/clientes")
public class ClienteController {

    private final GerenciarClienteUseCasePort gerenciarClienteUseCasePort;
    private final ClientePresenter clientePresenter;

    public ClienteController(GerenciarClienteUseCasePort gerenciarClienteUseCasePort,
                                ClientePresenter clientePresenter) {
        this.gerenciarClienteUseCasePort = gerenciarClienteUseCasePort;
        this.clientePresenter = clientePresenter;
    }

    @Operation(summary = "Cadastrar cliente")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cliente cadastrado com sucesso"),
            @ApiResponse(responseCode = "422", description = "E-mail já cadastrado")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RespostaApi<ClientePresenter.ClienteResposta> cadastrar(@RequestBody CadastroClienteRequest request) {
        return clientePresenter.apresentarCadastro(gerenciarClienteUseCasePort.cadastrar(request.nome(), request.telefone(), request.email()));
    }

    @Operation(summary = "Atualizar cliente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente atualizado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    })
    @PutMapping("/{id}")
    public RespostaApi<ClientePresenter.ClienteResposta> atualizar(@PathVariable UUID id,
                                                                    @RequestBody AtualizarClienteRequest request) {
        return clientePresenter.apresentarAtualizacao(gerenciarClienteUseCasePort.atualizar(id, request.nome(), request.telefone(), request.email()));
    }

    @Operation(summary = "Buscar cliente por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente encontrado"),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    })
    @GetMapping("/{id}")
    public RespostaApi<ClientePresenter.ClienteResposta> buscar(@PathVariable UUID id) {
        return clientePresenter.apresentarDetalhes(gerenciarClienteUseCasePort.buscar(id));
    }

    @Operation(summary = "Listar todos os clientes")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de clientes"),
            @ApiResponse(responseCode = "204", description = "Nenhum cliente cadastrado")
    })
    @GetMapping
    public ResponseEntity<RespostaApi<List<ClientePresenter.ClienteResposta>>> listar() {
        List<DetalhesClienteDTO> lista = gerenciarClienteUseCasePort.listar();
        
        if (lista.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        
        return ResponseEntity.ok(clientePresenter.apresentarLista(lista));
    }

    @Operation(summary = "Remover cliente")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Cliente removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable UUID id) {
        gerenciarClienteUseCasePort.deletar(id);
    }

    public record CadastroClienteRequest(String nome, String telefone, String email) {}

    public record AtualizarClienteRequest(String nome, String telefone, String email) {}
}
