package com.fiap.agendamento_servico.application.dto;

import java.time.LocalTime;
import java.util.List;

public record PeriodoComProfissionaisDTO(
        LocalTime horaInicio,
        LocalTime horaFim,
        boolean disponivel,
        List<ProfissionalNoPeriodoDTO> profissionaisDisponiveis
) {
}
