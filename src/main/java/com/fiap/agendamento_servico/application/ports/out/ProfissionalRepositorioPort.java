package com.fiap.agendamento_servico.application.ports.out;

import com.fiap.agendamento_servico.domain.entities.Profissional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProfissionalRepositorioPort {

    Profissional salvar(Profissional profissional);

    Optional<Profissional> buscarPorId(UUID profissionalId);

    List<Profissional> listarPorEstabelecimento(UUID estabelecimentoId);

    List<Profissional> listarPorEstabelecimentoENotaMinima(UUID estabelecimentoId, double notaMinima);

    void remover(UUID profissionalId);
}
