package com.fiap.agendamento_servico.infrastructure.persistence.mappers;

import com.fiap.agendamento_servico.domain.entities.Agendamento;
import com.fiap.agendamento_servico.domain.entities.Avaliacao;
import com.fiap.agendamento_servico.domain.entities.Estabelecimento;
import com.fiap.agendamento_servico.domain.entities.Profissional;
import com.fiap.agendamento_servico.domain.entities.Servico;
import com.fiap.agendamento_servico.domain.valueobjects.Endereco;
import com.fiap.agendamento_servico.infrastructure.persistence.entities.AgendamentoEntity;
import com.fiap.agendamento_servico.infrastructure.persistence.entities.AvaliacaoEntity;
import com.fiap.agendamento_servico.infrastructure.persistence.entities.EnderecoEmbeddable;
import com.fiap.agendamento_servico.infrastructure.persistence.entities.EstabelecimentoEntity;
import com.fiap.agendamento_servico.infrastructure.persistence.entities.ProfissionalEntity;
import com.fiap.agendamento_servico.infrastructure.persistence.entities.ServicoEntity;
import java.util.ArrayList;
import org.springframework.stereotype.Component;

@Component
public class PersistenciaMapper {

    public Estabelecimento paraDominio(EstabelecimentoEntity entity) {
        Estabelecimento estabelecimento = new Estabelecimento(
                entity.getId(),
                entity.getNome(),
                paraEndereco(entity.getEndereco()),
                entity.getHoraInicioFuncionamento(),
                entity.getHoraFimFuncionamento(),
                entity.getIntervaloMinutosPadrao(),
                entity.getServicos().stream().map(ServicoEntity::getId).toList());
        
        estabelecimento.atualizarNotaMedia(entity.getNotaMedia());
        
        estabelecimento.atualizarFotos(entity.getFotoPrincipalUrl(), entity.getGaleriaUrls());
        
        return estabelecimento;
    }

    public void copiarParaEntity(Estabelecimento dominio, EstabelecimentoEntity entity) {
        entity.setId(dominio.getId());
        entity.setNome(dominio.getNome());
        entity.setEndereco(paraEmbeddable(dominio.getEndereco()));
        entity.setHoraInicioFuncionamento(dominio.getHoraInicioFuncionamento());
        entity.setHoraFimFuncionamento(dominio.getHoraFimFuncionamento());
        entity.setIntervaloMinutosPadrao(dominio.getIntervaloMinutosPadrao());
        entity.setNotaMedia(dominio.getNotaMedia());
        entity.setFotoPrincipalUrl(dominio.getFotoPrincipalUrl());
        entity.setGaleriaUrls(new ArrayList<>(dominio.getGaleriaUrls()));
    }

    public Profissional paraDominio(ProfissionalEntity entity) {
        Profissional profissional = new Profissional(
                entity.getId(),
                entity.getNome(),
                entity.getEmail(),
                entity.getEstabelecimento().getId(),
                entity.getServicosIds(),
                entity.getEspecialidades(),
                entity.getHoraInicioTrabalho(),
                entity.getHoraFimTrabalho()
        );
        
        profissional.atualizarNotaMedia(entity.getNotaMedia());
        
        return profissional;
    }

    public void copiarParaEntity(Profissional dominio, ProfissionalEntity entity, EstabelecimentoEntity estabelecimento) {
        entity.setId(dominio.getId());
        entity.setNome(dominio.getNome());
        entity.setEmail(dominio.getEmail());
        entity.setEstabelecimento(estabelecimento);
        entity.setServicosIds(new ArrayList<>(dominio.getServicosIds()));
        entity.setEspecialidades(new ArrayList<>(dominio.getEspecialidades()));
        entity.setHoraInicioTrabalho(dominio.getHoraInicioTrabalho());
        entity.setHoraFimTrabalho(dominio.getHoraFimTrabalho());
        entity.setNotaMedia(dominio.getNotaMedia());
    }

    public Servico paraDominio(ServicoEntity entity) {
        return new Servico(
                entity.getId(),
                entity.getNome(),
                entity.getDescricao(),
                entity.getPreco(),
                entity.getDuracaoMinutos(),
                entity.getEstabelecimento().getId());
    }

    public void copiarParaEntity(Servico dominio, ServicoEntity entity, EstabelecimentoEntity estabelecimento) {
        entity.setId(dominio.id());
        entity.setNome(dominio.nome());
        entity.setDescricao(dominio.descricao());
        entity.setPreco(dominio.preco());
        entity.setDuracaoMinutos(dominio.duracaoMinutos());
        entity.setEstabelecimento(estabelecimento);
    }

    public Agendamento paraDominio(AgendamentoEntity entity) {
        return new Agendamento(
                entity.getId(),
                entity.getClienteId(),
                entity.getProfissionalId(),
                entity.getServicoId(),
                entity.getDataHoraInicio(),
                entity.getDataHoraFim(),
                entity.getStatus());
    }

    public void copiarParaEntity(Agendamento dominio, AgendamentoEntity entity) {
        entity.setId(dominio.id());
        entity.setClienteId(dominio.clienteId());
        entity.setProfissionalId(dominio.profissionalId());
        entity.setServicoId(dominio.servicoId());
        entity.setDataHoraInicio(dominio.dataHoraInicio());
        entity.setDataHoraFim(dominio.dataHoraFim());
        entity.setStatus(dominio.status());
    }

    public Avaliacao paraDominio(AvaliacaoEntity entity) {
        return new Avaliacao(
                entity.getId(),
                entity.getAgendamento().getId(),
                entity.getNota(),
                entity.getComentario(),
                entity.getDataCriacao());
    }

    public void copiarParaEntity(Avaliacao dominio, AvaliacaoEntity entity, AgendamentoEntity agendamento) {
        entity.setId(dominio.id());
        entity.setAgendamento(agendamento);
        entity.setNota(dominio.nota());
        entity.setComentario(dominio.comentario());
        entity.setDataCriacao(dominio.dataCriacao());
    }

    private Endereco paraEndereco(EnderecoEmbeddable embeddable) {
        return new Endereco(
                embeddable.getRua(),
                embeddable.getNumero(),
                embeddable.getComplemento(),
                embeddable.getBairro(),
                embeddable.getCep(),
                embeddable.getCidade());
    }

    private EnderecoEmbeddable paraEmbeddable(Endereco endereco) {
        return new EnderecoEmbeddable(
                endereco.rua(),
                endereco.numero(),
                endereco.complemento(),
                endereco.bairro(),
                endereco.cep(),
                endereco.cidade());
    }
}
