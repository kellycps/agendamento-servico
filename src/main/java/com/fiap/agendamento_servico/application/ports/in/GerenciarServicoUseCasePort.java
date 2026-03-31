package com.fiap.agendamento_servico.application.ports.in;

import com.fiap.agendamento_servico.application.dto.DetalhesServicoDTO;
import java.util.UUID;

public interface GerenciarServicoUseCasePort {
    DetalhesServicoDTO cadastrar(UUID estabelecimentoId, String nome, String descricao, double preco, int duracaoMinutos);

    DetalhesServicoDTO atualizar(UUID servicoId, String nome, String descricao, double preco, int duracaoMinutos);

    void deletar(UUID servicoId);
}
