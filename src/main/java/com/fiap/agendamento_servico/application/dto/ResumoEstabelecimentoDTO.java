package com.fiap.agendamento_servico.application.dto;

import java.util.UUID;

public record ResumoEstabelecimentoDTO(
        UUID id,
        String nome,
        String cidade,
        double notaMedia
) {
}
