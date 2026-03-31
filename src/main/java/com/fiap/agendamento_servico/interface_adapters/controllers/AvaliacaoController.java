package com.fiap.agendamento_servico.interface_adapters.controllers;

import com.fiap.agendamento_servico.application.dto.NovaAvaliacaoDTO;
import com.fiap.agendamento_servico.application.ports.in.ListarAvaliacoesUseCasePort;
import com.fiap.agendamento_servico.application.ports.in.RegistrarAvaliacaoUseCasePort;
import com.fiap.agendamento_servico.interface_adapters.presenters.AvaliacaoPresenter;
import com.fiap.agendamento_servico.interface_adapters.presenters.RespostaApi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Avaliações", description = "Registro e listagem de avaliações de agendamentos")
@RestController
@RequestMapping("/avaliacoes")
public class AvaliacaoController {

    private final RegistrarAvaliacaoUseCasePort registrarAvaliacaoUseCasePort;
    private final ListarAvaliacoesUseCasePort listarAvaliacoesUseCasePort;
    private final AvaliacaoPresenter avaliacaoPresenter;

    public AvaliacaoController(
            RegistrarAvaliacaoUseCasePort registrarAvaliacaoUseCasePort,
            ListarAvaliacoesUseCasePort listarAvaliacoesUseCasePort,
            AvaliacaoPresenter avaliacaoPresenter)
    {
        this.registrarAvaliacaoUseCasePort = registrarAvaliacaoUseCasePort;
        this.listarAvaliacoesUseCasePort = listarAvaliacoesUseCasePort;
        this.avaliacaoPresenter = avaliacaoPresenter;
    }

    @Operation(summary = "Registrar avaliação", description = "Registra uma avaliação (nota 1-5) para um agendamento concluído")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Avaliação registrada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Agendamento não encontrado"),
            @ApiResponse(responseCode = "422", description = "Agendamento não está concluído ou já foi avaliado")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RespostaApi<AvaliacaoPresenter.RegistroAvaliacaoResposta> registrar(@RequestBody RegistrarAvaliacaoRequest request) {
        NovaAvaliacaoDTO dto = new NovaAvaliacaoDTO(request.agendamentoId(), request.nota(), request.comentario());
        
        registrarAvaliacaoUseCasePort.executar(dto);
        
        return avaliacaoPresenter.apresentarRegistro(request.agendamentoId(), request.nota());
    }

    @Operation(summary = "Listar avaliações do estabelecimento")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de avaliações"),
            @ApiResponse(responseCode = "204", description = "Nenhuma avaliação encontrada")
    })
    @GetMapping("/estabelecimento/{estabelecimentoId}")
    public RespostaApi<List<AvaliacaoPresenter.DetalhesAvaliacaoResposta>> listarPorEstabelecimento(@PathVariable UUID estabelecimentoId) {
        return avaliacaoPresenter.apresentarListagem(listarAvaliacoesUseCasePort.listarPorEstabelecimento(estabelecimentoId));
    }

    public record RegistrarAvaliacaoRequest(UUID agendamentoId, int nota, String comentario) {}
}