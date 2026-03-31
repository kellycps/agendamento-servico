package com.fiap.agendamento_servico.interface_adapters.controllers;

import com.fiap.agendamento_servico.application.dto.ReagendarAgendamentoDTO;
import com.fiap.agendamento_servico.application.ports.in.GerenciarAgendamentoUseCasePort;
import com.fiap.agendamento_servico.interface_adapters.presenters.AgendamentoPresenter;
import com.fiap.agendamento_servico.interface_adapters.presenters.RespostaApi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Gerenciamento de Agendamentos", description = "Confirmação, cancelamento, conclusão e reagendamento")
@RestController
@RequestMapping("/gerenciamento/agendamento")
public class GerenciamentoAgendamentoController {

    private final GerenciarAgendamentoUseCasePort gerenciarAgendamentoUseCasePort;
    private final AgendamentoPresenter agendamentoPresenter;

    public GerenciamentoAgendamentoController(
            GerenciarAgendamentoUseCasePort gerenciarAgendamentoUseCasePort,
            AgendamentoPresenter agendamentoPresenter)
    {
        this.gerenciarAgendamentoUseCasePort = gerenciarAgendamentoUseCasePort;
        this.agendamentoPresenter = agendamentoPresenter;
    }

    @Operation(summary = "Confirmar agendamento", description = "Confirma um agendamento com status PENDENTE")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Agendamento confirmado"),
            @ApiResponse(responseCode = "400", description = "Transição de status inválida"),
            @ApiResponse(responseCode = "404", description = "Agendamento não encontrado")
    })
    @PatchMapping("/confirmar/{id}")
    public RespostaApi<AgendamentoPresenter.AcaoAgendamentoResposta> confirmar(@PathVariable UUID id) {
        gerenciarAgendamentoUseCasePort.confirmar(id);
        
        return agendamentoPresenter.apresentarConfirmacao(id.toString());
    }

    @Operation(summary = "Cancelar agendamento")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Agendamento cancelado"),
            @ApiResponse(responseCode = "400", description = "Transição de status inválida"),
            @ApiResponse(responseCode = "404", description = "Agendamento não encontrado")
    })
    @PatchMapping("/cancelar/{id}")
    public RespostaApi<AgendamentoPresenter.AcaoAgendamentoResposta> cancelar(@PathVariable UUID id) {
        gerenciarAgendamentoUseCasePort.cancelar(id);
        
        return agendamentoPresenter.apresentarCancelamento(id.toString());
    }

    @Operation(summary = "Concluir agendamento", description = "Marca o agendamento como CONCLUIDO após o atendimento")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Agendamento concluído"),
            @ApiResponse(responseCode = "400", description = "Transição de status inválida"),
            @ApiResponse(responseCode = "404", description = "Agendamento não encontrado")
    })
    @PatchMapping("/concluir/{id}")
    public RespostaApi<AgendamentoPresenter.AcaoAgendamentoResposta> concluir(@PathVariable UUID id) {
        gerenciarAgendamentoUseCasePort.concluir(id);
        
        return agendamentoPresenter.apresentarConclusao(id.toString());
    }

    @Operation(summary = "Marcar não comparecimento", description = "Registra que o cliente não compareceu ao agendamento")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Não comparecimento registrado"),
            @ApiResponse(responseCode = "404", description = "Agendamento não encontrado")
    })
    @PatchMapping("/nao-compareceu/{id}")
    public RespostaApi<AgendamentoPresenter.AcaoAgendamentoResposta> naoCompareceu(@PathVariable UUID id) {
        gerenciarAgendamentoUseCasePort.marcarNaoComparecimento(id);
        
        return agendamentoPresenter.apresentarNaoComparecimento(id.toString());
    }

    @Operation(summary = "Reagendar agendamento", description = "Altera a data e hora de um agendamento PENDENTE")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Agendamento reagendado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Transição inválida ou horário ocupado"),
            @ApiResponse(responseCode = "404", description = "Agendamento não encontrado")
    })
    @PatchMapping("/reagendar/{id}")
    public RespostaApi<AgendamentoPresenter.AcaoReagendarResposta> reagendar(
            @PathVariable UUID id,
            @RequestBody ReagendarRequest request
    ) {
        ReagendarAgendamentoDTO dto = new ReagendarAgendamentoDTO(id, request.novaDataHoraInicio());
        
        gerenciarAgendamentoUseCasePort.reagendar(dto);
        
        return agendamentoPresenter.apresentarReagendamento(id.toString(), request.novaDataHoraInicio());
    }

    @Operation(summary = "Listar agendamentos do cliente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de agendamentos"),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    })
    @GetMapping("/cliente/{clienteId}")
    public RespostaApi<List<AgendamentoPresenter.DetalhesAgendamentoResposta>> listarPorCliente(@PathVariable UUID clienteId) {
        return agendamentoPresenter.apresentarListaDoCliente(gerenciarAgendamentoUseCasePort.listarPorCliente(clienteId));
    }

    @Operation(summary = "[ADMIN] Remover agendamento fisicamente",
                description = "Operação administrativa: exclui fisicamente o registro. " +
                    "Restrito a agendamentos com status PENDENTE ou CANCELADO. " +
                    "Para cancelar um agendamento ativo, use PATCH /cancelar/{id}.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Agendamento removido"),
            @ApiResponse(responseCode = "400", description = "Status não permite exclusão física"),
            @ApiResponse(responseCode = "404", description = "Agendamento não encontrado")
    })
    @DeleteMapping("/deletar/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable UUID id) {
        gerenciarAgendamentoUseCasePort.deletar(id);
    }

    public record ReagendarRequest(LocalDateTime novaDataHoraInicio) {}
}