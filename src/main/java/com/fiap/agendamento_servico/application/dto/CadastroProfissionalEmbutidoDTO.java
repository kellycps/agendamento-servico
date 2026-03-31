package com.fiap.agendamento_servico.application.dto;

import java.time.LocalTime;
import java.util.List;

public record CadastroProfissionalEmbutidoDTO(
        String nome,
        String email,
        List<String> especialidades,
        LocalTime horaInicioTrabalho,
        LocalTime horaFimTrabalho
) {}
