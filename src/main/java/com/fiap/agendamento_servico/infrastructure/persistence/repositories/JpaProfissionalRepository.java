package com.fiap.agendamento_servico.infrastructure.persistence.repositories;

import com.fiap.agendamento_servico.infrastructure.persistence.entities.ProfissionalEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaProfissionalRepository extends JpaRepository<ProfissionalEntity, UUID> {

	List<ProfissionalEntity> findByEstabelecimentoId(UUID estabelecimentoId);
    
    List<ProfissionalEntity> findByEstabelecimentoIdAndNotaMediaGreaterThanEqual(UUID estabelecimentoId, double notaMinima);
}