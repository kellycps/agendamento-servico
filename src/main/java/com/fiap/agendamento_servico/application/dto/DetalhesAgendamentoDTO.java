package com.fiap.agendamento_servico.application.dto;

import com.fiap.agendamento_servico.domain.enums.StatusAgendamento;
import java.time.LocalDateTime;
import java.util.UUID;

public record DetalhesAgendamentoDTO(
        UUID id,
        String clienteNome,
        String profissionalNome,
        String servicoNome,
        LocalDateTime dataHoraInicio,
        LocalDateTime dataHoraFim,
        StatusAgendamento status
) {
}
