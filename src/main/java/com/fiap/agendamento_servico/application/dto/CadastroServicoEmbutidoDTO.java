package com.fiap.agendamento_servico.application.dto;

public record CadastroServicoEmbutidoDTO(
        String nome,
        String descricao,
        double preco,
        int duracaoMinutos
) {}
