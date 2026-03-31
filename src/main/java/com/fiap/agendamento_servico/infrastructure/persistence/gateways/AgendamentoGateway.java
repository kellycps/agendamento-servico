package com.fiap.agendamento_servico.infrastructure.persistence.gateways;

import com.fiap.agendamento_servico.application.ports.out.AgendamentoRepositorioPort;
import com.fiap.agendamento_servico.domain.entities.Agendamento;
import com.fiap.agendamento_servico.infrastructure.persistence.mappers.PersistenciaMapper;
import com.fiap.agendamento_servico.infrastructure.persistence.entities.AgendamentoEntity;
import com.fiap.agendamento_servico.infrastructure.persistence.repositories.JpaAgendamentoRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class AgendamentoGateway implements AgendamentoRepositorioPort {

    private final JpaAgendamentoRepository jpaAgendamentoRepository;
    private final PersistenciaMapper persistenciaMapper;

    public AgendamentoGateway(
            JpaAgendamentoRepository jpaAgendamentoRepository,
            PersistenciaMapper persistenciaMapper
    ) {
        this.jpaAgendamentoRepository = jpaAgendamentoRepository;
        this.persistenciaMapper = persistenciaMapper;
    }

    @Override
    public Agendamento salvar(Agendamento agendamento) {
        AgendamentoEntity entity = jpaAgendamentoRepository.findById(agendamento.id())
                .orElseGet(AgendamentoEntity::new);
        persistenciaMapper.copiarParaEntity(agendamento, entity);
        return persistenciaMapper.paraDominio(jpaAgendamentoRepository.save(entity));
    }

    @Override
    public Optional<Agendamento> buscarPorId(UUID agendamentoId) {
        return jpaAgendamentoRepository.findById(agendamentoId)
                .map(persistenciaMapper::paraDominio);
    }

    @Override
    public List<Agendamento> buscarPorProfissionalEData(UUID profissionalId, LocalDate data) {
        return jpaAgendamentoRepository.findByProfissionalIdAndDataHoraInicioBetween(
                        profissionalId,
                        data.atStartOfDay(),
                        data.plusDays(1).atStartOfDay().minusNanos(1)
                ).stream()
                .map(persistenciaMapper::paraDominio)
                .toList();
    }

    @Override
    public List<Agendamento> listarPorCliente(UUID clienteId) {
        return jpaAgendamentoRepository.findByClienteId(clienteId).stream()
                .map(persistenciaMapper::paraDominio)
                .toList();
    }

    @Override
    public List<Agendamento> listarPorProfissional(UUID profissionalId) {
        return jpaAgendamentoRepository.findByProfissionalId(profissionalId).stream()
                .map(persistenciaMapper::paraDominio)
                .toList();
    }

    @Override
    public void deletar(UUID agendamentoId) {
        jpaAgendamentoRepository.deleteById(agendamentoId);
    }

    @Override
    public boolean existePorServicoId(UUID servicoId) {
        return jpaAgendamentoRepository.existsByServicoId(servicoId);
    }
}