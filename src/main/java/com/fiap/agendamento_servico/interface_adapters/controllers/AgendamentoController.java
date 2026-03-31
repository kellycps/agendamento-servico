package com.fiap.agendamento_servico.interface_adapters.controllers;

import com.fiap.agendamento_servico.application.dto.NovoAgendamentoDTO;
import com.fiap.agendamento_servico.application.ports.in.AgendarServicoUseCasePort;
import com.fiap.agendamento_servico.application.ports.in.ConsultarDisponibilidadeUseCasePort;
import com.fiap.agendamento_servico.application.ports.in.ConsultarGradeEstabelecimentoUseCasePort;
import com.fiap.agendamento_servico.interface_adapters.presenters.AgendamentoPresenter;
import com.fiap.agendamento_servico.interface_adapters.presenters.RespostaApi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Agendamento", description = "Consulta de disponibilidade e reserva de horários")
@RestController
@RequestMapping("/agendamento")
public class AgendamentoController {

        private final ConsultarDisponibilidadeUseCasePort consultarDisponibilidadeUseCasePort;
        private final ConsultarGradeEstabelecimentoUseCasePort consultarGradeEstabelecimentoUseCasePort;
        private final AgendarServicoUseCasePort agendarServicoUseCasePort;
        private final AgendamentoPresenter agendamentoPresenter;

        public AgendamentoController(
                ConsultarDisponibilidadeUseCasePort consultarDisponibilidadeUseCasePort,
                ConsultarGradeEstabelecimentoUseCasePort consultarGradeEstabelecimentoUseCasePort,
                AgendarServicoUseCasePort agendarServicoUseCasePort,
                AgendamentoPresenter agendamentoPresenter)
        {
                this.consultarDisponibilidadeUseCasePort = consultarDisponibilidadeUseCasePort;
                this.consultarGradeEstabelecimentoUseCasePort = consultarGradeEstabelecimentoUseCasePort;
                this.agendarServicoUseCasePort = agendarServicoUseCasePort;
                this.agendamentoPresenter = agendamentoPresenter;
        }

        @Operation(summary = "Consultar disponibilidade", description = "Retorna os horários disponíveis de um profissional em uma data específica")
        @ApiResponses({
                @ApiResponse(responseCode = "200", description = "Grade de horários retornada com sucesso")
        })
        @GetMapping("/disponibilidade")
        public RespostaApi<AgendamentoPresenter.GradeHorariosResposta> consultarDisponibilidade(@RequestParam UUID profissionalId, @RequestParam LocalDate data) 
        {
                return agendamentoPresenter.apresentarGrade(
                        consultarDisponibilidadeUseCasePort.executar(profissionalId, data)
                );
        }

        @Operation(summary = "Grade completa do estabelecimento", description = "Retorna a grade completa de todos os profissionais do estabelecimento em uma data")
        @ApiResponse(responseCode = "200", description = "Grade retornada com sucesso")
        @GetMapping("/grade/estabelecimento")
        public RespostaApi<AgendamentoPresenter.GradeEstabelecimentoResposta> gradeCompleta(
                @RequestParam UUID estabelecimentoId,
                @RequestParam LocalDate data)
        {
                return agendamentoPresenter.apresentarGradeEstabelecimento(consultarGradeEstabelecimentoUseCasePort.consultarGradeCompleta(estabelecimentoId, data));
        }

        @Operation(summary = "Grade de horários disponíveis", description = "Retorna apenas os horários livres do estabelecimento em uma data")
        @ApiResponse(responseCode = "200", description = "Grade de horários disponíveis retornada com sucesso")
        @GetMapping("/grade/disponivel")
        public RespostaApi<AgendamentoPresenter.GradeEstabelecimentoResposta> gradeDisponivel(
                @RequestParam UUID estabelecimentoId,
                @RequestParam LocalDate data)
        {
                return agendamentoPresenter.apresentarGradeEstabelecimento(
                        consultarGradeEstabelecimentoUseCasePort.consultarGradeDisponivel(estabelecimentoId, data));
        }

        @Operation(summary = "Grade por serviço", description = "Retorna os horários disponíveis filtrados por serviço específico")
        @ApiResponse(responseCode = "200", description = "Grade por serviço retornada com sucesso")
        @GetMapping("/grade/servico")
        public RespostaApi<AgendamentoPresenter.GradeEstabelecimentoResposta> gradePorServico(
                @RequestParam UUID estabelecimentoId,
                @RequestParam LocalDate data,
                @RequestParam UUID servicoId)
        {
                return agendamentoPresenter.apresentarGradeEstabelecimento(
                        consultarGradeEstabelecimentoUseCasePort.consultarGradePorServico(estabelecimentoId, data, servicoId));
        }

        @Operation(summary = "Reservar agendamento", description = "Cria um novo agendamento com status PENDENTE para o horário solicitado")
        @ApiResponses({
                @ApiResponse(responseCode = "201", description = "Agendamento criado com status PENDENTE"),
                @ApiResponse(responseCode = "404", description = "Profissional, cliente ou serviço não encontrado"),
                @ApiResponse(responseCode = "422", description = "Horário indisponível ou conflito de agenda")
        })
        @PostMapping("/reservar")
        @ResponseStatus(HttpStatus.CREATED)
        public RespostaApi<AgendamentoPresenter.DetalhesAgendamentoResposta> reservar(@RequestBody ReservarAgendamentoRequest request) {
                NovoAgendamentoDTO dto = new NovoAgendamentoDTO(
                        request.clienteId(),
                        request.profissionalId(),
                        request.servicoId(),
                        request.dataHoraInicio());
                return agendamentoPresenter.apresentarReservaCriada(agendarServicoUseCasePort.executar(dto));
        }
}