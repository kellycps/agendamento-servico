package com.fiap.agendamento_servico.application.ports.in;

import com.fiap.agendamento_servico.application.dto.GradeEstabelecimentoDTO;
import java.time.LocalDate;
import java.util.UUID;

public interface ConsultarGradeEstabelecimentoUseCasePort {
    GradeEstabelecimentoDTO consultarGradeCompleta(UUID estabelecimentoId, LocalDate data);

    GradeEstabelecimentoDTO consultarGradeDisponivel(UUID estabelecimentoId, LocalDate data);

    GradeEstabelecimentoDTO consultarGradePorServico(UUID estabelecimentoId, LocalDate data, UUID servicoId);
}
