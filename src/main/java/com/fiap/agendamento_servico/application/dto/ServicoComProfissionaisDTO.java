package com.fiap.agendamento_servico.application.dto;

import java.util.List;
import java.util.UUID;

public record ServicoComProfissionaisDTO(
        UUID id,
        String nome,
        String descricao,
        double preco,
        int duracaoMinutos,
        List<DetalhesProfissionalDTO> profissionais
) {}
