package com.fiap.agendamento_servico.infrastructure.persistence.repositories;

import com.fiap.agendamento_servico.infrastructure.persistence.entities.AgendamentoEntity;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaAgendamentoRepository extends JpaRepository<AgendamentoEntity, UUID> {

    List<AgendamentoEntity> findByProfissionalIdAndDataHoraInicioBetween(
            UUID profissionalId,
            LocalDateTime dataHoraInicio,
            LocalDateTime dataHoraFim
    );

    List<AgendamentoEntity> findByClienteId(UUID clienteId);

    List<AgendamentoEntity> findByProfissionalId(UUID profissionalId);

    boolean existsByServicoId(UUID servicoId);
}