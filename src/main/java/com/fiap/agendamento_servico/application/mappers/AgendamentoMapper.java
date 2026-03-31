package com.fiap.agendamento_servico.application.mappers;

import com.fiap.agendamento_servico.application.dto.DetalhesAgendamentoDTO;
import com.fiap.agendamento_servico.application.dto.GradeHorariosDTO;
import com.fiap.agendamento_servico.application.dto.NovoAgendamentoDTO;
import com.fiap.agendamento_servico.application.dto.PeriodoAgendaDTO;
import com.fiap.agendamento_servico.domain.entities.Agendamento;
import com.fiap.agendamento_servico.domain.valueobjects.PeriodoAgenda;
import java.time.LocalDate;
import java.util.List;

public final class AgendamentoMapper {

    private AgendamentoMapper() {
    }

    public static Agendamento paraEntidade(NovoAgendamentoDTO novoAgendamentoDTO, int duracaoMinutosServico) {
        return Agendamento.criar(
                novoAgendamentoDTO.clienteId(),
                novoAgendamentoDTO.profissionalId(),
                novoAgendamentoDTO.servicoId(),
                novoAgendamentoDTO.dataHoraInicio(),
                duracaoMinutosServico
        );
    }

    public static DetalhesAgendamentoDTO paraDetalhesAgendamentoDTO(
            Agendamento agendamento,
            String clienteNome,
            String profissionalNome,
            String servicoNome
    ) {
        return new DetalhesAgendamentoDTO(
                agendamento.id(),
                clienteNome,
                profissionalNome,
                servicoNome,
                agendamento.dataHoraInicio(),
                agendamento.dataHoraFim(),
                agendamento.status()
        );
    }

    public static PeriodoAgendaDTO paraPeriodoAgendaDTO(PeriodoAgenda periodoAgenda) {
        return new PeriodoAgendaDTO(periodoAgenda.horaInicio(), periodoAgenda.horaFim(),
                periodoAgenda.estaDisponivel(), periodoAgenda.statusBloqueio());
    }

    public static GradeHorariosDTO paraGradeHorariosDTO(LocalDate data, String profissionalNome, List<PeriodoAgenda> periodos) {
        List<PeriodoAgendaDTO> periodosDto = periodos.stream()
                .map(AgendamentoMapper::paraPeriodoAgendaDTO)
                .toList();

        return new GradeHorariosDTO(data, profissionalNome, periodosDto);
    }
}
