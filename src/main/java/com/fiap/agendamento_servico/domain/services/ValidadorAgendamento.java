package com.fiap.agendamento_servico.domain.services;

import com.fiap.agendamento_servico.domain.entities.Agendamento;
import com.fiap.agendamento_servico.domain.entities.Estabelecimento;
import com.fiap.agendamento_servico.domain.entities.Profissional;
import com.fiap.agendamento_servico.domain.enums.StatusAgendamento;
import com.fiap.agendamento_servico.domain.exceptions.HorarioIndisponivelException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

public class ValidadorAgendamento {

    public void validarHorarioTrabalho(Agendamento novoAgendamento, Profissional profissional) {
        LocalTime horaInicio = profissional.getHoraInicioTrabalho();
        
        LocalTime horaFim = profissional.getHoraFimTrabalho();

        if (horaInicio == null || horaFim == null) {
            return;
        }

        LocalTime horaInicioAgendamento = novoAgendamento.dataHoraInicio().toLocalTime();
        
        LocalTime horaFimAgendamento = novoAgendamento.dataHoraFim().toLocalTime();

        if (horaInicioAgendamento.isBefore(horaInicio) || horaFimAgendamento.isAfter(horaFim)) {
            throw new HorarioIndisponivelException("Profissional não disponível para este horário");
        }
    }

    public void validarSemSobreposicao(Agendamento novoAgendamento, List<Agendamento> agendamentosExistentes) {
        if (novoAgendamento == null) {
            throw new IllegalArgumentException("Novo agendamento não pode ser nulo");
        }

        List<Agendamento> existentes = agendamentosExistentes == null ? Collections.emptyList() : agendamentosExistentes;

        boolean possuiSobreposicao = existentes.stream()
                .filter(agendamento -> agendamento.status() != StatusAgendamento.CANCELADO)
                .anyMatch(agendamento -> existeSobreposicao(
                        novoAgendamento.dataHoraInicio(),
                        novoAgendamento.dataHoraFim(),
                        agendamento.dataHoraInicio(),
                        agendamento.dataHoraFim()
                ));

        if (possuiSobreposicao) {
            throw new HorarioIndisponivelException("O horário informado já está ocupado por outro agendamento");
        }
    }

    public void validarAlinhamentoComGrade(Agendamento agendamento, Estabelecimento estabelecimento) {
        LocalTime horaInicioFuncionamento = estabelecimento.getHoraInicioFuncionamento();
        LocalTime horaFimFuncionamento = estabelecimento.getHoraFimFuncionamento();
        int intervalo = estabelecimento.getIntervaloMinutosPadrao();

        LocalTime horaInicioAgendamento = agendamento.dataHoraInicio().toLocalTime();

        if (horaInicioAgendamento.isBefore(horaInicioFuncionamento) || !horaInicioAgendamento.isBefore(horaFimFuncionamento)) {
            throw new HorarioIndisponivelException("Horário fora do período de funcionamento do estabelecimento");
        }

        long minutosDesdeAbertura = ChronoUnit.MINUTES.between(horaInicioFuncionamento, horaInicioAgendamento);
        
        if (minutosDesdeAbertura % intervalo != 0) {
            throw new HorarioIndisponivelException(String.format("Horário %s não corresponde a um slot da grade (intervalo de %d minutos a partir de %s)", horaInicioAgendamento, intervalo, horaInicioFuncionamento));
        }
    }

    private boolean existeSobreposicao(LocalDateTime inicioNovo, LocalDateTime fimNovo, LocalDateTime inicioExistente, LocalDateTime fimExistente) {
        return inicioNovo.isBefore(fimExistente) && fimNovo.isAfter(inicioExistente);
    }
}
