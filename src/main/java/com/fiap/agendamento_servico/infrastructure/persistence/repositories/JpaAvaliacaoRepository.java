package com.fiap.agendamento_servico.infrastructure.persistence.repositories;

import com.fiap.agendamento_servico.infrastructure.persistence.entities.AvaliacaoEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface JpaAvaliacaoRepository extends JpaRepository<AvaliacaoEntity, UUID> {

    boolean existsByAgendamentoId(UUID agendamentoId);

    @Transactional
    void deleteByAgendamentoId(UUID agendamentoId);

    List<AvaliacaoEntity> findByAgendamentoServicoIdIn(List<UUID> servicosIds);

    List<AvaliacaoEntity> findByAgendamentoProfissionalId(UUID profissionalId);
}