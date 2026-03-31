package com.fiap.agendamento_servico.application.ports.in;

import com.fiap.agendamento_servico.application.dto.DetalhesAgendamentoDTO;
import com.fiap.agendamento_servico.application.dto.NovoAgendamentoDTO;

public interface AgendarServicoUseCasePort {
    DetalhesAgendamentoDTO executar(NovoAgendamentoDTO novoAgendamento);
}
