package com.fiap.agendamento_servico.application.ports.in;

import com.fiap.agendamento_servico.application.dto.NovaAvaliacaoDTO;

public interface RegistrarAvaliacaoUseCasePort {
    void executar(NovaAvaliacaoDTO novaAvaliacaoDTO);
}