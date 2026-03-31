package com.fiap.agendamento_servico.application.ports.out;

import com.fiap.agendamento_servico.domain.entities.Servico;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ServicoRepositorioPort {

    Servico salvar(Servico servico);

    Optional<Servico> buscarPorId(UUID servicoId);

    List<Servico> buscarPorIds(List<UUID> ids);

    List<Servico> listarPorEstabelecimento(UUID estabelecimentoId);

    void remover(UUID servicoId);
}
