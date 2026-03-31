package com.fiap.agendamento_servico.application.ports.out;

import com.fiap.agendamento_servico.application.dto.FiltroEstabelecimentoDTO;
import com.fiap.agendamento_servico.domain.entities.Estabelecimento;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EstabelecimentoRepositorioPort {

    Estabelecimento salvar(Estabelecimento estabelecimento);

    Optional<Estabelecimento> buscarPorId(UUID estabelecimentoId);

    List<Estabelecimento> listarTodos();

    List<Estabelecimento> buscarPorCidade(String cidade);

    List<Estabelecimento> filtrar(FiltroEstabelecimentoDTO filtro);

    void deletar(UUID id);
}
