package com.fiap.agendamento_servico.infrastructure.persistence.gateways;

import com.fiap.agendamento_servico.application.dto.FiltroEstabelecimentoDTO;
import com.fiap.agendamento_servico.application.ports.out.EstabelecimentoRepositorioPort;
import com.fiap.agendamento_servico.domain.entities.Estabelecimento;
import com.fiap.agendamento_servico.infrastructure.persistence.mappers.PersistenciaMapper;
import com.fiap.agendamento_servico.infrastructure.persistence.entities.EstabelecimentoEntity;
import com.fiap.agendamento_servico.infrastructure.persistence.repositories.JpaEstabelecimentoRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class EstabelecimentoGateway implements EstabelecimentoRepositorioPort {

    private final JpaEstabelecimentoRepository jpaEstabelecimentoRepository;
    private final PersistenciaMapper persistenciaMapper;

    public EstabelecimentoGateway(
            JpaEstabelecimentoRepository jpaEstabelecimentoRepository,
            PersistenciaMapper persistenciaMapper)
    {
        this.jpaEstabelecimentoRepository = jpaEstabelecimentoRepository;
        this.persistenciaMapper = persistenciaMapper;
    }

    @Override
    public Estabelecimento salvar(Estabelecimento estabelecimento) {
        EstabelecimentoEntity entity = jpaEstabelecimentoRepository.findById(estabelecimento.getId()).orElseGet(EstabelecimentoEntity::new);
        
        persistenciaMapper.copiarParaEntity(estabelecimento, entity);
        
        return persistenciaMapper.paraDominio(jpaEstabelecimentoRepository.save(entity));
    }

    @Override
    public Optional<Estabelecimento> buscarPorId(UUID estabelecimentoId) {
        return jpaEstabelecimentoRepository.findById(estabelecimentoId).map(persistenciaMapper::paraDominio);
    }

    @Override
    public List<Estabelecimento> listarTodos() {
        return jpaEstabelecimentoRepository.findAll().stream()
                .map(persistenciaMapper::paraDominio)
                .toList();
    }

    @Override
    public List<Estabelecimento> buscarPorCidade(String cidade) {
        return jpaEstabelecimentoRepository.findByEnderecoCidadeIgnoreCase(cidade).stream()
                .map(persistenciaMapper::paraDominio)
                .toList();
    }

    @Override
    public List<Estabelecimento> filtrar(FiltroEstabelecimentoDTO filtro) {
        return jpaEstabelecimentoRepository.filtrar(
                filtro.nome(),
                filtro.cidade(),
                filtro.avaliacaoMinima(),
                filtro.precoMinimo(),
                filtro.precoMaximo()
        ).stream()
                .map(persistenciaMapper::paraDominio)
                .toList();
    }

    @Override
    public void deletar(UUID id) {
        jpaEstabelecimentoRepository.deleteById(id);
    }
}