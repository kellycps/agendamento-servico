package com.fiap.agendamento_servico.domain.entities;

import com.fiap.agendamento_servico.domain.enums.StatusAgendamento;
import com.fiap.agendamento_servico.domain.exceptions.BusinessException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

public record Agendamento(
        UUID id,
        UUID clienteId,
        UUID profissionalId,
        UUID servicoId,
        LocalDateTime dataHoraInicio,
        LocalDateTime dataHoraFim,
        StatusAgendamento status
) {
    public Agendamento {
        if (id == null) {
            throw new IllegalArgumentException("Id do agendamento não pode ser nulo");
        }

        if (clienteId == null) {
            throw new IllegalArgumentException("Id do cliente não pode ser nulo");
        }

        if (profissionalId == null) {
            throw new IllegalArgumentException("Id do profissional não pode ser nulo");
        }

        if (servicoId == null) {
            throw new IllegalArgumentException("Id do serviço não pode ser nulo");
        }

        if (dataHoraInicio == null) {
            throw new IllegalArgumentException("Hora de início do agendamento não pode ser nula");
        }

        if (dataHoraFim == null) {
            throw new IllegalArgumentException("Hora de fim do agendamento não pode ser nula");
        }

        if (!dataHoraInicio.isBefore(dataHoraFim)) {
            throw new IllegalArgumentException("Hora de início do agendamento deve ser anterior à hora de fim");
        }

        if (status == null) {
            throw new IllegalArgumentException("Status do agendamento não pode ser nulo");
        }
    }

    public static Agendamento criar(
            UUID clienteId,
            UUID profissionalId,
            UUID servicoId,
            LocalDateTime dataHoraInicio,
            int duracaoMinutos) 
    {
        
        if (duracaoMinutos <= 0) {
            throw new IllegalArgumentException("Duração do serviço deve ser maior que zero");
        }

        return new Agendamento(
                UUID.randomUUID(),
                clienteId,
                profissionalId,
                servicoId,
                dataHoraInicio,
                dataHoraInicio.plusMinutes(duracaoMinutos),
                StatusAgendamento.PENDENTE
        );
    }

    public LocalDate data() {
        return dataHoraInicio.toLocalDate();
    }

    public LocalTime horaInicio() {
        return dataHoraInicio.toLocalTime();
    }

    public LocalTime horaFim() {
        return dataHoraFim.toLocalTime();
    }

    public Agendamento confirmar() {
        if (status != StatusAgendamento.PENDENTE) {
            throw new BusinessException("Somente agendamentos pendentes podem ser confirmados");
        }

        return new Agendamento(
                id,
                clienteId,
                profissionalId,
                servicoId,
                dataHoraInicio,
                dataHoraFim,
                StatusAgendamento.CONFIRMADO
        );
    }

    public Agendamento cancelar() {
        if (status == StatusAgendamento.CANCELADO) {
            throw new BusinessException("Agendamento já está cancelado");
        }

        if (status == StatusAgendamento.CONCLUIDO) {
            throw new BusinessException("Agendamento concluído não pode ser cancelado");
        }

        return new Agendamento(
                id,
                clienteId,
                profissionalId,
                servicoId,
                dataHoraInicio,
                dataHoraFim,
                StatusAgendamento.CANCELADO
        );
    }

    public Agendamento concluir() {
        if (status != StatusAgendamento.CONFIRMADO) {
            throw new BusinessException("Apenas agendamentos confirmados podem ser concluídos. Status atual: " + status.getDescricao());
        }

        return new Agendamento(
                id,
                clienteId,
                profissionalId,
                servicoId,
                dataHoraInicio,
                dataHoraFim,
                StatusAgendamento.CONCLUIDO
        );
    }

    public Agendamento marcarNaoComparecimento() {
        if (status != StatusAgendamento.CONFIRMADO) {
            throw new BusinessException("Não comparecimento só pode ser registrado para agendamentos confirmados. Status atual: " + status.getDescricao());
        }

        return new Agendamento(
                id,
                clienteId,
                profissionalId,
                servicoId,
                dataHoraInicio,
                dataHoraFim,
                StatusAgendamento.NAO_COMPARECEU
        );
    }

    public Agendamento reagendar(LocalDateTime novaDataHoraInicio, int duracaoMinutos) {
        if (status == StatusAgendamento.CANCELADO || status == StatusAgendamento.CONCLUIDO || status == StatusAgendamento.NAO_COMPARECEU) {
            throw new BusinessException("Agendamento não pode ser reagendado no status: " + status.getDescricao());
        }

        if (novaDataHoraInicio == null) {
            throw new IllegalArgumentException("Nova data e hora de início não podem ser nulas");
        }

        if (duracaoMinutos <= 0) {
            throw new IllegalArgumentException("Duração do serviço deve ser maior que zero");
        }

        return new Agendamento(
                id,
                clienteId,
                profissionalId,
                servicoId,
                novaDataHoraInicio,
                novaDataHoraInicio.plusMinutes(duracaoMinutos),
                StatusAgendamento.PENDENTE
        );
    }
}