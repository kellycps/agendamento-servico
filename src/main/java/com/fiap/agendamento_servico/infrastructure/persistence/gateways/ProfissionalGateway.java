package com.fiap.agendamento_servico.infrastructure.persistence.gateways;

import com.fiap.agendamento_servico.application.ports.out.ProfissionalRepositorioPort;
import com.fiap.agendamento_servico.domain.entities.Profissional;
import com.fiap.agendamento_servico.infrastructure.persistence.mappers.PersistenciaMapper;
import com.fiap.agendamento_servico.infrastructure.persistence.entities.EstabelecimentoEntity;
import com.fiap.agendamento_servico.infrastructure.persistence.entities.ProfissionalEntity;
import com.fiap.agendamento_servico.infrastructure.persistence.repositories.JpaEstabelecimentoRepository;
import com.fiap.agendamento_servico.infrastructure.persistence.repositories.JpaProfissionalRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class ProfissionalGateway implements ProfissionalRepositorioPort {

    private final JpaProfissionalRepository jpaProfissionalRepository;
    private final JpaEstabelecimentoRepository jpaEstabelecimentoRepository;
    private final PersistenciaMapper persistenciaMapper;

    public ProfissionalGateway(
            JpaProfissionalRepository jpaProfissionalRepository,
            JpaEstabelecimentoRepository jpaEstabelecimentoRepository,
            PersistenciaMapper persistenciaMapper)
    {
        this.jpaProfissionalRepository = jpaProfissionalRepository;
        this.jpaEstabelecimentoRepository = jpaEstabelecimentoRepository;
        this.persistenciaMapper = persistenciaMapper;
    }

    @Override
    public Profissional salvar(Profissional profissional) {
        ProfissionalEntity entity = jpaProfissionalRepository.findById(profissional.getId()).orElseGet(ProfissionalEntity::new);
        EstabelecimentoEntity estabelecimento = jpaEstabelecimentoRepository.getReferenceById(profissional.getEstabelecimentoId());
        
        persistenciaMapper.copiarParaEntity(profissional, entity, estabelecimento);
        
        return persistenciaMapper.paraDominio(jpaProfissionalRepository.save(entity));
    }

    @Override
    public Optional<Profissional> buscarPorId(UUID profissionalId) {
        return jpaProfissionalRepository.findById(profissionalId).map(persistenciaMapper::paraDominio);
    }

    @Override
    public List<Profissional> listarPorEstabelecimento(UUID estabelecimentoId) {
        return jpaProfissionalRepository.findByEstabelecimentoId(estabelecimentoId).stream()
                .map(persistenciaMapper::paraDominio)
                .toList();
    }

    @Override
    public List<Profissional> listarPorEstabelecimentoENotaMinima(UUID estabelecimentoId, double notaMinima) {
        return jpaProfissionalRepository
                .findByEstabelecimentoIdAndNotaMediaGreaterThanEqual(estabelecimentoId, notaMinima).stream()
                .map(persistenciaMapper::paraDominio)
                .toList();
    }

    @Override
    public void remover(UUID profissionalId) {
        jpaProfissionalRepository.deleteById(profissionalId);
    }
}