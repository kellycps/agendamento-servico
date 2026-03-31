package com.fiap.agendamento_servico.infrastructure.persistence.gateways;

import com.fiap.agendamento_servico.application.ports.out.AvaliacaoRepositorioPort;
import com.fiap.agendamento_servico.domain.entities.Avaliacao;
import com.fiap.agendamento_servico.infrastructure.persistence.mappers.PersistenciaMapper;
import com.fiap.agendamento_servico.infrastructure.persistence.entities.AgendamentoEntity;
import com.fiap.agendamento_servico.infrastructure.persistence.entities.AvaliacaoEntity;
import com.fiap.agendamento_servico.infrastructure.persistence.repositories.JpaAgendamentoRepository;
import com.fiap.agendamento_servico.infrastructure.persistence.repositories.JpaAvaliacaoRepository;
import com.fiap.agendamento_servico.infrastructure.persistence.repositories.JpaServicoRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class AvaliacaoGateway implements AvaliacaoRepositorioPort {

    private final JpaAvaliacaoRepository jpaAvaliacaoRepository;
    private final JpaAgendamentoRepository jpaAgendamentoRepository;
    private final JpaServicoRepository jpaServicoRepository;
    private final PersistenciaMapper persistenciaMapper;

    public AvaliacaoGateway(
            JpaAvaliacaoRepository jpaAvaliacaoRepository,
            JpaAgendamentoRepository jpaAgendamentoRepository,
            JpaServicoRepository jpaServicoRepository,
            PersistenciaMapper persistenciaMapper
    ) {
        this.jpaAvaliacaoRepository = jpaAvaliacaoRepository;
        this.jpaAgendamentoRepository = jpaAgendamentoRepository;
        this.jpaServicoRepository = jpaServicoRepository;
        this.persistenciaMapper = persistenciaMapper;
    }

    @Override
    public boolean existePorAgendamentoId(UUID agendamentoId) {
        return jpaAvaliacaoRepository.existsByAgendamentoId(agendamentoId);
    }

    @Override
    public void deletarPorAgendamentoId(UUID agendamentoId) {
        jpaAvaliacaoRepository.deleteByAgendamentoId(agendamentoId);
    }

    @Override
    public Avaliacao salvar(Avaliacao avaliacao) {
        AvaliacaoEntity entity = jpaAvaliacaoRepository.findById(avaliacao.id()).orElseGet(AvaliacaoEntity::new);
        AgendamentoEntity agendamento = jpaAgendamentoRepository.getReferenceById(avaliacao.agendamentoId());
        persistenciaMapper.copiarParaEntity(avaliacao, entity, agendamento);
        return persistenciaMapper.paraDominio(jpaAvaliacaoRepository.save(entity));
    }

    @Override
    public List<Avaliacao> listarPorProfissional(UUID profissionalId) {
        return jpaAvaliacaoRepository.findByAgendamentoProfissionalId(profissionalId).stream()
                .map(persistenciaMapper::paraDominio)
                .toList();
    }

    @Override
    public List<Avaliacao> listarPorEstabelecimento(UUID estabelecimentoId) {
        List<UUID> servicosIds = jpaServicoRepository.findByEstabelecimentoId(estabelecimentoId).stream()
                .map(item -> item.getId())
                .toList();

        if (servicosIds.isEmpty()) {
            return List.of();
        }

        return jpaAvaliacaoRepository.findByAgendamentoServicoIdIn(servicosIds).stream()
                .map(persistenciaMapper::paraDominio)
                .toList();
    }
}