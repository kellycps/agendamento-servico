package com.fiap.agendamento_servico.application.dto;

import java.util.UUID;

public record NovaAvaliacaoDTO(
        UUID agendamentoId,
        int nota,
        String comentario
) {
}
