package com.fiap.agendamento_servico.application.dto;

public record FiltroEstabelecimentoDTO(
        String cidade,
        String nome,
        Double avaliacaoMinima,
        Double precoMinimo,
        Double precoMaximo
) {
}
