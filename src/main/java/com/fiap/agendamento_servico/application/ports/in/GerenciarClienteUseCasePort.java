package com.fiap.agendamento_servico.application.ports.in;

import com.fiap.agendamento_servico.application.dto.DetalhesClienteDTO;
import java.util.List;
import java.util.UUID;

public interface GerenciarClienteUseCasePort {
    DetalhesClienteDTO cadastrar(String nome, String telefone, String email);

    DetalhesClienteDTO atualizar(UUID id, String nome, String telefone, String email);

    DetalhesClienteDTO buscar(UUID id);

    List<DetalhesClienteDTO> listar();

    void deletar(UUID id);
}
