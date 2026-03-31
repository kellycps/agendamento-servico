package com.fiap.agendamento_servico.application.ports.out;

import com.fiap.agendamento_servico.domain.entities.Agendamento;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AgendamentoRepositorioPort {
    Agendamento salvar(Agendamento agendamento);

    Optional<Agendamento> buscarPorId(UUID agendamentoId);

    List<Agendamento> buscarPorProfissionalEData(UUID profissionalId, LocalDate data);

    List<Agendamento> listarPorCliente(UUID clienteId);

    List<Agendamento> listarPorProfissional(UUID profissionalId);

    void deletar(UUID agendamentoId);

    boolean existePorServicoId(UUID servicoId);
}
