package com.fiap.agendamento_servico.application.ports.out;

import com.fiap.agendamento_servico.domain.entities.Avaliacao;
import java.util.List;
import java.util.UUID;

public interface AvaliacaoRepositorioPort {
    Avaliacao salvar(Avaliacao avaliacao);

    boolean existePorAgendamentoId(UUID agendamentoId);

    void deletarPorAgendamentoId(UUID agendamentoId);

    List<Avaliacao> listarPorEstabelecimento(UUID estabelecimentoId);

    List<Avaliacao> listarPorProfissional(UUID profissionalId);
}
