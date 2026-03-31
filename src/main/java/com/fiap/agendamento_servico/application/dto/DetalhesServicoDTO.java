package com.fiap.agendamento_servico.application.dto;

import java.util.UUID;

public record DetalhesServicoDTO(
        UUID id,
        String nome,
        double preco,
        int duracaoMinutos
) {
}
