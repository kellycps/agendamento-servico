package com.fiap.agendamento_servico.application.ports.in;

import com.fiap.agendamento_servico.application.dto.GradeHorariosDTO;
import java.time.LocalDate;
import java.util.UUID;

public interface ConsultarDisponibilidadeUseCasePort {
    GradeHorariosDTO executar(UUID profissionalId, LocalDate data);
}
