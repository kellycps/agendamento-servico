package com.fiap.agendamento_servico.application.usecases;

import com.fiap.agendamento_servico.application.dto.DetalhesClienteDTO;
import com.fiap.agendamento_servico.application.ports.in.GerenciarClienteUseCasePort;
import com.fiap.agendamento_servico.application.ports.out.ClienteRepositorioPort;
import com.fiap.agendamento_servico.domain.entities.Cliente;
import com.fiap.agendamento_servico.domain.exceptions.EntidadeNaoEncontradaException;
import java.util.List;
import java.util.UUID;

public class GerenciarClienteUseCase implements GerenciarClienteUseCasePort {

    private final ClienteRepositorioPort clienteRepositorioPort;

    public GerenciarClienteUseCase(ClienteRepositorioPort clienteRepositorioPort) {
        this.clienteRepositorioPort = clienteRepositorioPort;
    }

    @Override
    public DetalhesClienteDTO cadastrar(String nome, String telefone, String email) {
        Cliente cliente = Cliente.criar(nome, telefone, email);
        return paraDTO(clienteRepositorioPort.salvar(cliente));
    }

    @Override
    public DetalhesClienteDTO atualizar(UUID id, String nome, String telefone, String email) {
        Cliente atual = buscarCliente(id);
        Cliente atualizado = new Cliente(atual.id(), nome, telefone, email);
        return paraDTO(clienteRepositorioPort.salvar(atualizado));
    }

    @Override
    public DetalhesClienteDTO buscar(UUID id) {
        return paraDTO(buscarCliente(id));
    }

    @Override
    public List<DetalhesClienteDTO> listar() {
        return clienteRepositorioPort.listar().stream()
                .map(this::paraDTO)
                .toList();
    }

    @Override
    public void deletar(UUID id) {
        buscarCliente(id);
        clienteRepositorioPort.remover(id);
    }

    private Cliente buscarCliente(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("Id do cliente não pode ser nulo");
        }

        return clienteRepositorioPort.buscarPorId(id).orElseThrow(() -> EntidadeNaoEncontradaException.para("Cliente", id));
    }

    private DetalhesClienteDTO paraDTO(Cliente cliente) {
        return new DetalhesClienteDTO(cliente.id(), cliente.nome(), cliente.telefone(), cliente.email());
    }
}
