package com.fiap.agendamento_servico.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReagendarAgendamentoDTO(
        UUID agendamentoId,
        LocalDateTime novaDataHoraInicio
) {
}
