package com.fiap.agendamento_servico.infrastructure.persistence.gateways;

import com.fiap.agendamento_servico.application.ports.out.ClienteRepositorioPort;
import com.fiap.agendamento_servico.domain.entities.Cliente;
import com.fiap.agendamento_servico.infrastructure.persistence.entities.ClienteEntity;
import com.fiap.agendamento_servico.infrastructure.persistence.repositories.JpaClienteRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class ClienteGateway implements ClienteRepositorioPort {

    private final JpaClienteRepository jpaClienteRepository;

    public ClienteGateway(JpaClienteRepository jpaClienteRepository) {
        this.jpaClienteRepository = jpaClienteRepository;
    }

    @Override
    public Cliente salvar(Cliente cliente) {
        ClienteEntity entity = jpaClienteRepository.findById(cliente.id()).orElseGet(ClienteEntity::new);
        copiarParaEntity(cliente, entity);
        return paraDominio(jpaClienteRepository.save(entity));
    }

    @Override
    public Optional<Cliente> buscarPorId(UUID id) {
        return jpaClienteRepository.findById(id).map(this::paraDominio);
    }

    @Override
    public List<Cliente> listar() {
        return jpaClienteRepository.findAll().stream().map(this::paraDominio).toList();
    }

    @Override
    public void remover(UUID id) {
        jpaClienteRepository.deleteById(id);
    }

    private Cliente paraDominio(ClienteEntity entity) {
        return new Cliente(entity.getId(), entity.getNome(), entity.getTelefone(), entity.getEmail());
    }

    private void copiarParaEntity(Cliente dominio, ClienteEntity entity) {
        entity.setId(dominio.id());
        entity.setNome(dominio.nome());
        entity.setTelefone(dominio.telefone());
        entity.setEmail(dominio.email());
    }
}
