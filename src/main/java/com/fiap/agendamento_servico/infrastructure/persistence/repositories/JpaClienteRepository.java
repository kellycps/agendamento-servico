package com.fiap.agendamento_servico.infrastructure.persistence.repositories;

import com.fiap.agendamento_servico.infrastructure.persistence.entities.ClienteEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaClienteRepository extends JpaRepository<ClienteEntity, UUID> {}
