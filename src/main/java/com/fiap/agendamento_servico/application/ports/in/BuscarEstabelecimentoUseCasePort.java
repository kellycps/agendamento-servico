package com.fiap.agendamento_servico.application.ports.in;

import com.fiap.agendamento_servico.application.dto.DetalhesEstabelecimentoDTO;
import com.fiap.agendamento_servico.application.dto.DetalhesProfissionalDTO;
import com.fiap.agendamento_servico.application.dto.DetalhesServicoDTO;
import com.fiap.agendamento_servico.application.dto.FiltroEstabelecimentoDTO;
import com.fiap.agendamento_servico.application.dto.ServicoComProfissionaisDTO;
import java.util.List;
import java.util.UUID;

public interface BuscarEstabelecimentoUseCasePort {
    List<DetalhesEstabelecimentoDTO> buscarPorCidade(String cidade, Double notaMinima);

    List<DetalhesEstabelecimentoDTO> filtrar(FiltroEstabelecimentoDTO filtro);

    List<DetalhesServicoDTO> listarServicosPorEstabelecimento(UUID estabelecimentoId);

    List<DetalhesProfissionalDTO> listarProfissionaisPorEstabelecimento(UUID estabelecimentoId);

    List<ServicoComProfissionaisDTO> listarServicosComProfissionais(UUID estabelecimentoId);
}