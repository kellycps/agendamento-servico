package com.fiap.agendamento_servico.application.ports.in;

import com.fiap.agendamento_servico.application.dto.CadastroEstabelecimentoDTO;
import com.fiap.agendamento_servico.application.dto.DetalhesEstabelecimentoDTO;

public interface CadastrarEstabelecimentoUseCasePort {
    DetalhesEstabelecimentoDTO executar(CadastroEstabelecimentoDTO cadastroEstabelecimento);
}
