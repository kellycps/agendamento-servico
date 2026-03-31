package com.fiap.agendamento_servico.domain.valueobjects;

import com.fiap.agendamento_servico.domain.enums.StatusAgendamento;
import java.time.LocalTime;

public record PeriodoAgenda(
        LocalTime horaInicio,
        LocalTime horaFim,
        boolean estaDisponivel,
        StatusAgendamento statusBloqueio
) {
    public PeriodoAgenda {
        if (horaInicio == null) {
            throw new IllegalArgumentException("Hora de início do periodo não pode ser nula");
        }

        if (horaFim == null) {
            throw new IllegalArgumentException("Hora de fim do periodo não pode ser nula");
        }

        if (!horaInicio.isBefore(horaFim)) {
            throw new IllegalArgumentException("Hora de início do periodo deve ser anterior à hora de fim");
        }
    }

    public boolean contemHorario(LocalTime inicio, LocalTime fim) {
        if (inicio == null || fim == null) {
            throw new IllegalArgumentException("Início e fim do intervalo não podem ser nulos");
        }

        if (!inicio.isBefore(fim)) {
            throw new IllegalArgumentException("Início do intervalo deve ser anterior ao fim");
        }

        return !horaInicio.isBefore(inicio) && !horaFim.isAfter(fim);
    }
}
