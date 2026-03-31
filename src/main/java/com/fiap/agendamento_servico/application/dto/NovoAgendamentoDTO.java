package com.fiap.agendamento_servico.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record NovoAgendamentoDTO(
        UUID clienteId,
        UUID profissionalId,
        UUID servicoId,
        LocalDateTime dataHoraInicio
) {
}
