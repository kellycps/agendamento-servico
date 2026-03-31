package com.fiap.agendamento_servico.application.dto;

import com.fiap.agendamento_servico.domain.enums.StatusAgendamento;
import java.time.LocalTime;

public record PeriodoAgendaDTO(
        LocalTime horaInicio,
        LocalTime horaFim,
        boolean disponivel,
        StatusAgendamento statusBloqueio
) {
}
