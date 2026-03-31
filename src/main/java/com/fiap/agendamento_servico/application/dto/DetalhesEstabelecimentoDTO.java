package com.fiap.agendamento_servico.application.dto;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public record DetalhesEstabelecimentoDTO(
        UUID id,
        String nome,
        EnderecoDTO endereco,
        LocalTime horaInicio,
        LocalTime horaFim,
        int intervaloMinutos,
        double notaMedia,
        List<DetalhesServicoDTO> servicos,        List<DetalhesProfissionalDTO> profissionais,        String fotoPrincipalUrl,
        List<String> galeriaUrls
) {
}
