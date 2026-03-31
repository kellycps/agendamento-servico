package com.fiap.agendamento_servico.application.ports.in;

import com.fiap.agendamento_servico.application.dto.DetalhesAgendamentoDTO;
import com.fiap.agendamento_servico.application.dto.ReagendarAgendamentoDTO;
import java.util.List;
import java.util.UUID;

public interface GerenciarAgendamentoUseCasePort {
    void confirmar(UUID id);

    void cancelar(UUID id);

    void concluir(UUID id);

    void marcarNaoComparecimento(UUID id);

    void reagendar(ReagendarAgendamentoDTO dto);

    List<DetalhesAgendamentoDTO> listarPorCliente(UUID clienteId);

    void deletar(UUID id);
}
