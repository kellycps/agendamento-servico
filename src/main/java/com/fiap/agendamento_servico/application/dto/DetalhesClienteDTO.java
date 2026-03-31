package com.fiap.agendamento_servico.application.dto;

import java.util.UUID;

public record DetalhesClienteDTO(
        UUID id,
        String nome,
        String telefone,
        String email
) {
}
