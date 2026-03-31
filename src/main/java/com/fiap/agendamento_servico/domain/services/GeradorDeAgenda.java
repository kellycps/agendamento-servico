package com.fiap.agendamento_servico.domain.services;

import com.fiap.agendamento_servico.domain.entities.Agendamento;
import com.fiap.agendamento_servico.domain.enums.StatusAgendamento;
import com.fiap.agendamento_servico.domain.valueobjects.PeriodoAgenda;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GeradorDeAgenda {

    public List<PeriodoAgenda> gerarGradeVazia(LocalTime horaInicio, LocalTime horaFim, int intervaloMinutos) {
        validarParametros(horaInicio, horaFim, intervaloMinutos);

        List<PeriodoAgenda> grade = new ArrayList<>();
        
        LocalTime horarioAtual = horaInicio;

        while (horarioAtual.isBefore(horaFim)) {
            LocalTime proximoHorario = horarioAtual.plusMinutes(intervaloMinutos);

            if (proximoHorario.isAfter(horaFim)) {
                break;
            }

            grade.add(new PeriodoAgenda(horarioAtual, proximoHorario, true, null));
            
            horarioAtual = proximoHorario;
        }

        return grade;
    }

    public List<PeriodoAgenda> mapearDisponibilidade(List<PeriodoAgenda> gradeVazia, List<Agendamento> agendamentos) {
        if (gradeVazia == null) {
            throw new IllegalArgumentException("Grade de periodos não pode ser nula");
        }

        List<Agendamento> agendamentosNoDia = agendamentos == null ? Collections.emptyList() : agendamentos;
        
        List<PeriodoAgenda> gradeAtualizada = new ArrayList<>(gradeVazia.size());

        for (PeriodoAgenda periodo : gradeVazia) {
            boolean estaDisponivel = true;
            
            StatusAgendamento statusBloqueio = null;

            for (Agendamento agendamento : agendamentosNoDia) {
                if (existeSobreposicao(periodo.horaInicio(), periodo.horaFim(), agendamento.horaInicio(), agendamento.horaFim())) {
                    StatusAgendamento status = agendamento.status();
                    
                    if (status == StatusAgendamento.CANCELADO) {
                        continue;
                    }
                    
                    estaDisponivel = false;
                    statusBloqueio = status;
                    break;
                }
            }

            gradeAtualizada.add(new PeriodoAgenda(periodo.horaInicio(), periodo.horaFim(), estaDisponivel, statusBloqueio));
        }

        return gradeAtualizada;
    }

    private boolean existeSobreposicao(
            LocalTime inicioPeriodo,
            LocalTime fimPeriodo,
            LocalTime inicioAgendamento,
            LocalTime fimAgendamento) 
    {
        return inicioPeriodo.isBefore(fimAgendamento) && fimPeriodo.isAfter(inicioAgendamento);
    }

    private void validarParametros(LocalTime horaInicio, LocalTime horaFim, int intervaloMinutos) {
        if (horaInicio == null) {
            throw new IllegalArgumentException("Hora de início não pode ser nula");
        }

        if (horaFim == null) {
            throw new IllegalArgumentException("Hora de fim não pode ser nula");
        }

        if (!horaInicio.isBefore(horaFim)) {
            throw new IllegalArgumentException("Hora de início deve ser anterior à hora de fim");
        }

        if (intervaloMinutos <= 0) {
            throw new IllegalArgumentException("Intervalo em minutos deve ser positivo");
        }
    }
}
