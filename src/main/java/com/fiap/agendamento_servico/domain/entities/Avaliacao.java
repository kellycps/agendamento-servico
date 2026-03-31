package com.fiap.agendamento_servico.domain.entities;

import com.fiap.agendamento_servico.domain.exceptions.BusinessException;
import java.time.LocalDateTime;
import java.util.UUID;

public record Avaliacao(
        UUID id,
        UUID agendamentoId,
        int nota,
        String comentario,
        LocalDateTime dataCriacao
) {
    public Avaliacao {
        validarNota(nota);
    }

    private static void validarNota(int nota) {
        if (nota < 1 || nota > 5) {
            throw new BusinessException("Nota inválida. Selecione de 1 a 5");
        }
    }

    public static Avaliacao criar(UUID agendamentoId, int nota, String comentario) {
        return new Avaliacao(UUID.randomUUID(), agendamentoId, nota, comentario, LocalDateTime.now());
    }

    @Override
    public String toString() {
        return String.format("Avaliacao{id=%s, agendamentoId=%s, nota=%d/5, dataCriacao=%s}", id, agendamentoId, nota, dataCriacao);
    }
}
