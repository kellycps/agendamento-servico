package com.fiap.agendamento_servico.application.ports.out;

import com.fiap.agendamento_servico.domain.entities.Cliente;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClienteRepositorioPort {
    Cliente salvar(Cliente cliente);

    Optional<Cliente> buscarPorId(UUID id);

    List<Cliente> listar();

    void remover(UUID id);
}
