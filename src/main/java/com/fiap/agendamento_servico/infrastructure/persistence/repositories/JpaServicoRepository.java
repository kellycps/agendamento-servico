package com.fiap.agendamento_servico.infrastructure.persistence.repositories;

import com.fiap.agendamento_servico.infrastructure.persistence.entities.ServicoEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaServicoRepository extends JpaRepository<ServicoEntity, UUID> {

	List<ServicoEntity> findByEstabelecimentoId(UUID estabelecimentoId);
}