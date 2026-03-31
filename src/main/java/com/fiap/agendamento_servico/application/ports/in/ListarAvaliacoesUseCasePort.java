package com.fiap.agendamento_servico.application.ports.in;

import com.fiap.agendamento_servico.application.dto.DetalhesAvaliacaoDTO;
import java.util.List;
import java.util.UUID;

public interface ListarAvaliacoesUseCasePort {
    List<DetalhesAvaliacaoDTO> listarPorEstabelecimento(UUID estabelecimentoId);
}
