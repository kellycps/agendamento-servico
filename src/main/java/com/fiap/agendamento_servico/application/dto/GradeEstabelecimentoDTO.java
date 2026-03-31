package com.fiap.agendamento_servico.application.dto;

import java.time.LocalDate;
import java.util.List;

public record GradeEstabelecimentoDTO(LocalDate data, List<PeriodoComProfissionaisDTO> periodos) {
}
