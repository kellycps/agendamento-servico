package com.fiap.agendamento_servico.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record DetalhesAvaliacaoDTO(
        UUID id,
        UUID agendamentoId,
        int nota,
        String comentario,
        LocalDateTime dataCriacao,
        String nomeProfissional
) {
}
