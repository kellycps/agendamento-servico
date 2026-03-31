package com.fiap.agendamento_servico.interface_adapters.controllers;

import com.fiap.agendamento_servico.application.ports.in.GerenciarServicoUseCasePort;
import com.fiap.agendamento_servico.interface_adapters.presenters.RespostaApi;
import com.fiap.agendamento_servico.interface_adapters.presenters.ServicoPresenter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Serviços", description = "Cadastro e gerenciamento de serviços oferecidos pelos estabelecimentos")
@RestController
@RequestMapping("/servicos")
public class ServicoController {

        private final GerenciarServicoUseCasePort gerenciarServicoUseCasePort;
        private final ServicoPresenter servicoPresenter;

        public ServicoController(GerenciarServicoUseCasePort gerenciarServicoUseCasePort, ServicoPresenter servicoPresenter) {
                this.gerenciarServicoUseCasePort = gerenciarServicoUseCasePort;
                this.servicoPresenter = servicoPresenter;
        }

        @Operation(summary = "Cadastrar serviço", description = "Adiciona um serviço a um estabelecimento existente")
        @ApiResponses({
                @ApiResponse(responseCode = "201", description = "Serviço cadastrado com sucesso"),
                @ApiResponse(responseCode = "404", description = "Estabelecimento não encontrado")
        })
        @PostMapping
        @ResponseStatus(HttpStatus.CREATED)
        public RespostaApi<ServicoPresenter.ServicoResposta> cadastrar(@RequestBody CadastroServicoRequest request) {
                return servicoPresenter.apresentarCadastro(
                        gerenciarServicoUseCasePort.cadastrar(
                                request.estabelecimentoId(),
                                request.nome(),
                                request.descricao(),
                                request.preco(),
                                request.duracaoMinutos()));
        }

        @Operation(summary = "Atualizar serviço")
        @ApiResponses({
                @ApiResponse(responseCode = "200", description = "Serviço atualizado"),
                @ApiResponse(responseCode = "404", description = "Serviço não encontrado")
        })
        @PutMapping("/{id}")
        public RespostaApi<ServicoPresenter.ServicoResposta> atualizar(@PathVariable UUID id, @RequestBody AtualizarServicoRequest request) {
                return servicoPresenter.apresentarAtualizacao(
                        gerenciarServicoUseCasePort.atualizar(
                                id,
                                request.nome(),
                                request.descricao(),
                                request.preco(),
                                request.duracaoMinutos()));
        }

        @Operation(summary = "Remover serviço")
        @ApiResponses({
                @ApiResponse(responseCode = "204", description = "Serviço removido"),
                @ApiResponse(responseCode = "404", description = "Serviço não encontrado")
        })
        @DeleteMapping("/{id}")
        @ResponseStatus(HttpStatus.NO_CONTENT)
        public void deletar(@PathVariable UUID id) {
                gerenciarServicoUseCasePort.deletar(id);
        }

        public record CadastroServicoRequest(
                UUID estabelecimentoId,
                String nome,
                String descricao,
                double preco,
                int duracaoMinutos) {}

        public record AtualizarServicoRequest(
                String nome,
                String descricao,
                double preco,
                int duracaoMinutos) {}
}
