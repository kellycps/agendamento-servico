package com.fiap.agendamento_servico.infrastructure.persistence.gateways;

import com.fiap.agendamento_servico.application.ports.out.ServicoRepositorioPort;
import com.fiap.agendamento_servico.domain.entities.Servico;
import com.fiap.agendamento_servico.infrastructure.persistence.mappers.PersistenciaMapper;
import com.fiap.agendamento_servico.infrastructure.persistence.entities.EstabelecimentoEntity;
import com.fiap.agendamento_servico.infrastructure.persistence.entities.ServicoEntity;
import com.fiap.agendamento_servico.infrastructure.persistence.repositories.JpaEstabelecimentoRepository;
import com.fiap.agendamento_servico.infrastructure.persistence.repositories.JpaServicoRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class ServicoGateway implements ServicoRepositorioPort {

    private final JpaServicoRepository jpaServicoRepository;
    private final JpaEstabelecimentoRepository jpaEstabelecimentoRepository;
    private final PersistenciaMapper persistenciaMapper;

    public ServicoGateway(
            JpaServicoRepository jpaServicoRepository,
            JpaEstabelecimentoRepository jpaEstabelecimentoRepository,
            PersistenciaMapper persistenciaMapper)
    {
        this.jpaServicoRepository = jpaServicoRepository;
        this.jpaEstabelecimentoRepository = jpaEstabelecimentoRepository;
        this.persistenciaMapper = persistenciaMapper;
    }

    @Override
    public Servico salvar(Servico servico) {
        ServicoEntity entity = jpaServicoRepository.findById(servico.id()).orElseGet(ServicoEntity::new);
        EstabelecimentoEntity estabelecimento = jpaEstabelecimentoRepository.getReferenceById(servico.estabelecimentoId());
        
        persistenciaMapper.copiarParaEntity(servico, entity, estabelecimento);
        
        return persistenciaMapper.paraDominio(jpaServicoRepository.save(entity));
    }

    @Override
    public Optional<Servico> buscarPorId(UUID servicoId) {
        return jpaServicoRepository.findById(servicoId).map(persistenciaMapper::paraDominio);
    }

    @Override
    public List<Servico> buscarPorIds(List<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        return jpaServicoRepository.findAllById(ids).stream().map(persistenciaMapper::paraDominio).toList();
    }

    @Override
    public List<Servico> listarPorEstabelecimento(UUID estabelecimentoId) {
        return jpaServicoRepository.findByEstabelecimentoId(estabelecimentoId).stream().map(persistenciaMapper::paraDominio).toList();
    }

    @Override
    public void remover(UUID servicoId) {
        jpaServicoRepository.deleteById(servicoId);
    }
}