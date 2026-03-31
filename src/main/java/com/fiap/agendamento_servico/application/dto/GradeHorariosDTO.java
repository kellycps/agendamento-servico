package com.fiap.agendamento_servico.application.dto;

import java.time.LocalDate;
import java.util.List;

public record GradeHorariosDTO(
        LocalDate data,
        String profissionalNome,
        List<PeriodoAgendaDTO> periodos
) {
}
