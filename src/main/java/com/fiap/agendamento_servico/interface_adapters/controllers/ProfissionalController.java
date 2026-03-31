package com.fiap.agendamento_servico.interface_adapters.controllers;

import com.fiap.agendamento_servico.application.dto.DetalhesProfissionalDTO;
import com.fiap.agendamento_servico.application.ports.in.GerenciarProfissionalUseCasePort;
import com.fiap.agendamento_servico.interface_adapters.presenters.ProfissionalPresenter;
import com.fiap.agendamento_servico.interface_adapters.presenters.RespostaApi;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Profissionais", description = "Cadastro, vinculação e gerenciamento de profissionais")
@RestController
@RequestMapping("/profissionais")
public class ProfissionalController {

    private final GerenciarProfissionalUseCasePort gerenciarProfissionalUseCasePort;
    private final ProfissionalPresenter profissionalPresenter;

    public ProfissionalController(
            GerenciarProfissionalUseCasePort gerenciarProfissionalUseCasePort,
            ProfissionalPresenter profissionalPresenter)
    {
        this.gerenciarProfissionalUseCasePort = gerenciarProfissionalUseCasePort;
        this.profissionalPresenter = profissionalPresenter;
    }

    @Operation(summary = "Cadastrar profissional", description = "Cria um novo profissional vinculado a um estabelecimento")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Profissional cadastrado"),
            @ApiResponse(responseCode = "404", description = "Estabelecimento não encontrado")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RespostaApi<ProfissionalPresenter.DetalhesProfissionalResposta> cadastrar(@RequestBody CadastroProfissionalRequest request) {
        return profissionalPresenter.apresentarVinculo(
                gerenciarProfissionalUseCasePort.cadastrar(request.nome(), request.email(), request.estabelecimentoId(), request.especialidades(), request.horaInicioTrabalho(), request.horaFimTrabalho()));
    }

    @Operation(summary = "Atualizar profissional")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profissional atualizado"),
            @ApiResponse(responseCode = "404", description = "Profissional não encontrado")
    })
    @PutMapping("/{profissionalId}")
    public RespostaApi<ProfissionalPresenter.DetalhesProfissionalResposta> atualizar(@PathVariable UUID profissionalId, @RequestBody AtualizarProfissionalRequest request) {
        return profissionalPresenter.apresentarVinculo(
                gerenciarProfissionalUseCasePort.atualizar(profissionalId, request.nome(), request.email(), request.especialidades(), request.horaInicioTrabalho(), request.horaFimTrabalho()));
    }

    @Operation(summary = "Remover profissional")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Profissional removido"),
            @ApiResponse(responseCode = "404", description = "Profissional não encontrado")
    })
    @DeleteMapping("/{profissionalId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable UUID profissionalId) {
        gerenciarProfissionalUseCasePort.deletar(profissionalId);
    }

    @Operation(summary = "Buscar profissionais por nota mínima")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de profissionais"),
            @ApiResponse(responseCode = "204", description = "Nenhum resultado")
    })
    @GetMapping("/busca")
    public ResponseEntity<RespostaApi<List<ProfissionalPresenter.DetalhesProfissionalResposta>>> listarPorNotaMinima(@RequestParam UUID estabelecimentoId, @RequestParam double notaMinima) {
        List<DetalhesProfissionalDTO> resultado = gerenciarProfissionalUseCasePort.listarPorEstabelecimentoENotaMinima(estabelecimentoId, notaMinima);
       
        if (resultado.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        
        return ResponseEntity.ok(profissionalPresenter.apresentarListagem(resultado));
    }

    @Operation(summary = "Vincular profissional existente a um estabelecimento")
    @ApiResponse(responseCode = "201", description = "Profissional vinculado com sucesso")
    @PostMapping("/vincular")
    @ResponseStatus(HttpStatus.CREATED)
    public RespostaApi<ProfissionalPresenter.DetalhesProfissionalResposta> vincularProfissional(@RequestBody VincularProfissionalRequest request) {
        return profissionalPresenter.apresentarVinculo(
                gerenciarProfissionalUseCasePort.vincularProfissional(request.nome(), request.estabelecimentoId()));
    }

    @Operation(summary = "Atualizar especialidades do profissional")
    @ApiResponse(responseCode = "200", description = "Especialidades atualizadas")
    @PutMapping("/{profissionalId}/especialidades")
    public RespostaApi<ProfissionalPresenter.DetalhesProfissionalResposta> atualizarEspecialidades(
            @PathVariable UUID profissionalId,
            @RequestBody AtualizarEspecialidadesRequest request)
    {
        return profissionalPresenter.apresentarEspecialidades(gerenciarProfissionalUseCasePort.atualizarEspecialidades(profissionalId, request.especialidades()));
    }

    @Operation(summary = "Definir horários de trabalho do profissional")
    @ApiResponse(responseCode = "200", description = "Horários definidos com sucesso")
    @PutMapping("/{profissionalId}/horarios")
    public RespostaApi<ProfissionalPresenter.JornadaProfissionalResposta> definirHorariosTrabalho(
            @PathVariable UUID profissionalId,
            @RequestBody DefinirHorariosTrabalhoRequest request) 
    {
        gerenciarProfissionalUseCasePort.definirHorariosTrabalho(profissionalId, request.horaInicio(), request.horaFim());
        
        return profissionalPresenter.apresentarHorarios(request.horaInicio(), request.horaFim());
    }

    @Operation(summary = "Vincular serviço ao profissional")
    @ApiResponse(responseCode = "200", description = "Serviço vinculado ao profissional")
    @PostMapping("/{profissionalId}/servicos/{servicoId}")
    public RespostaApi<Void> adicionarServico(@PathVariable UUID profissionalId, @PathVariable UUID servicoId) {
        gerenciarProfissionalUseCasePort.adicionarServico(profissionalId, servicoId);
        
        return RespostaApi.sucesso("Serviço vinculado ao profissional com sucesso.", null);
    }

    @Operation(summary = "Desvincular serviço do profissional")
    @ApiResponse(responseCode = "204", description = "Serviço desvinculado")
    @DeleteMapping("/{profissionalId}/servicos/{servicoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removerServico(@PathVariable UUID profissionalId, @PathVariable UUID servicoId) {
        gerenciarProfissionalUseCasePort.removerServico(profissionalId, servicoId);
    }

    public record CadastroProfissionalRequest(
            String nome,
            String email,
            UUID estabelecimentoId,
            List<String> especialidades,
            LocalTime horaInicioTrabalho,
            LocalTime horaFimTrabalho) {}

    public record AtualizarProfissionalRequest(
            String nome,
            String email,
            List<String> especialidades,
            LocalTime horaInicioTrabalho,
            LocalTime horaFimTrabalho) {}

    public record VincularProfissionalRequest(String nome, UUID estabelecimentoId) {}

    public record AtualizarEspecialidadesRequest(List<String> especialidades) {}

    public record DefinirHorariosTrabalhoRequest(LocalTime horaInicio, LocalTime horaFim) {}
}