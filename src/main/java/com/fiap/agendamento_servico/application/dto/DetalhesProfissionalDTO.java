package com.fiap.agendamento_servico.application.dto;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public record DetalhesProfissionalDTO(
        UUID id,
        String nome,
        String email,
        List<String> especialidades,
        double notaMedia,
        LocalTime horaInicioTrabalho,
        LocalTime horaFimTrabalho
) {
}
