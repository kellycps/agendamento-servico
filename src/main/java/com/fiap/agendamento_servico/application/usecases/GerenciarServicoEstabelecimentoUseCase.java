package com.fiap.agendamento_servico.application.usecases;

import com.fiap.agendamento_servico.application.dto.DetalhesServicoDTO;
import com.fiap.agendamento_servico.application.ports.in.GerenciarServicoUseCasePort;
import com.fiap.agendamento_servico.application.ports.out.AgendamentoRepositorioPort;
import com.fiap.agendamento_servico.application.ports.out.EstabelecimentoRepositorioPort;
import com.fiap.agendamento_servico.application.ports.out.ProfissionalRepositorioPort;
import com.fiap.agendamento_servico.application.ports.out.ServicoRepositorioPort;
import com.fiap.agendamento_servico.domain.entities.Estabelecimento;
import com.fiap.agendamento_servico.domain.entities.Profissional;
import com.fiap.agendamento_servico.domain.entities.Servico;
import com.fiap.agendamento_servico.domain.exceptions.BusinessException;
import com.fiap.agendamento_servico.domain.exceptions.EntidadeNaoEncontradaException;
import java.util.List;
import java.util.UUID;

public class GerenciarServicoEstabelecimentoUseCase implements GerenciarServicoUseCasePort {

    private final EstabelecimentoRepositorioPort estabelecimentoRepositorioPort;
    private final ServicoRepositorioPort servicoRepositorioPort;
    private final ProfissionalRepositorioPort profissionalRepositorioPort;
    private final AgendamentoRepositorioPort agendamentoRepositorioPort;

    public GerenciarServicoEstabelecimentoUseCase(
            EstabelecimentoRepositorioPort estabelecimentoRepositorioPort,
            ServicoRepositorioPort servicoRepositorioPort,
            ProfissionalRepositorioPort profissionalRepositorioPort,
            AgendamentoRepositorioPort agendamentoRepositorioPort
    ) {
        this.estabelecimentoRepositorioPort = estabelecimentoRepositorioPort;
        this.servicoRepositorioPort = servicoRepositorioPort;
        this.profissionalRepositorioPort = profissionalRepositorioPort;
        this.agendamentoRepositorioPort = agendamentoRepositorioPort;
    }

    @Override
    public DetalhesServicoDTO cadastrar(UUID estabelecimentoId, String nome, String descricao, double preco, int duracaoMinutos) {
        return adicionarServico(estabelecimentoId, nome, descricao, preco, duracaoMinutos);
    }

    @Override
    public DetalhesServicoDTO atualizar(UUID servicoId, String nome, String descricao, double preco, int duracaoMinutos) {
        Servico servicoAtual = buscarServico(servicoId);
        
        Servico atualizado = new Servico(servicoAtual.id(), nome, descricao, preco, duracaoMinutos, servicoAtual.estabelecimentoId());
        
        return paraDetalhesServicoDTO(servicoRepositorioPort.salvar(atualizado));
    }

    @Override
    public void deletar(UUID servicoId) {
        Servico servico = buscarServico(servicoId);
        
        if (agendamentoRepositorioPort.existePorServicoId(servicoId)) {
            throw new BusinessException("Serviço não pode ser excluído pois está vinculado a um agendamento");
        }
        
        List<Profissional> profissionais = profissionalRepositorioPort.listarPorEstabelecimento(servico.estabelecimentoId());
        
        for (Profissional profissional : profissionais) {
            if (profissional.getServicosIds().contains(servicoId)) {
                profissional.removerServico(servicoId);
                profissionalRepositorioPort.salvar(profissional);
            }
        }
        
        servicoRepositorioPort.remover(servicoId);
    }

    public DetalhesServicoDTO adicionarServico(
            UUID estabelecimentoId,
            String nome,
            String descricao,
            double preco,
            int duracaoMinutos
    ) {
        Estabelecimento estabelecimento = buscarEstabelecimento(estabelecimentoId);

        Servico novoServico = Servico.criar(nome, descricao, preco, duracaoMinutos, estabelecimento.getId());
        
        Servico servicoSalvo = servicoRepositorioPort.salvar(novoServico);

        estabelecimento.adicionarServico(servicoSalvo.id());
        
        estabelecimentoRepositorioPort.salvar(estabelecimento);

        return paraDetalhesServicoDTO(servicoSalvo);
    }

    public DetalhesServicoDTO atualizarPreco(UUID estabelecimentoId, UUID servicoId, double novoPreco) {
        if (novoPreco < 0) {
            throw new IllegalArgumentException("Novo preço não pode ser negativo");
        }

        Estabelecimento estabelecimento = buscarEstabelecimento(estabelecimentoId);
        
        Servico servicoAtual = buscarServico(servicoId);

        validarServicoNoEstabelecimento(estabelecimento, servicoAtual);

        Servico servicoAtualizado = new Servico(
                servicoAtual.id(),
                servicoAtual.nome(),
                servicoAtual.descricao(),
                novoPreco,
                servicoAtual.duracaoMinutos(),
                servicoAtual.estabelecimentoId()
        );

        Servico salvo = servicoRepositorioPort.salvar(servicoAtualizado);
        
        return paraDetalhesServicoDTO(salvo);
    }

    public void removerServico(UUID estabelecimentoId, UUID servicoId) {
        Estabelecimento estabelecimento = buscarEstabelecimento(estabelecimentoId);
        
        Servico servicoAtual = buscarServico(servicoId);

        validarServicoNoEstabelecimento(estabelecimento, servicoAtual);

        servicoRepositorioPort.remover(servicoId);
        
        estabelecimento.removerServico(servicoId);
        
        estabelecimentoRepositorioPort.salvar(estabelecimento);
    }

    private Estabelecimento buscarEstabelecimento(UUID estabelecimentoId) {
        if (estabelecimentoId == null) {
            throw new IllegalArgumentException("Id do estabelecimento não pode ser nulo");
        }

        return estabelecimentoRepositorioPort
                .buscarPorId(estabelecimentoId)
                .orElseThrow(() -> EntidadeNaoEncontradaException.para("Estabelecimento", estabelecimentoId));
    }

    private Servico buscarServico(UUID servicoId) {
        if (servicoId == null) {
            throw new IllegalArgumentException("Id do serviço não pode ser nulo");
        }

        return servicoRepositorioPort
                .buscarPorId(servicoId)
                .orElseThrow(() -> EntidadeNaoEncontradaException.para("Serviço", servicoId));
    }

    private void validarServicoNoEstabelecimento(Estabelecimento estabelecimento, Servico servico) {
        if (!servico.estabelecimentoId().equals(estabelecimento.getId())) {
            throw new BusinessException("O serviço não pertence ao estabelecimento informado");
        }
    }

    private DetalhesServicoDTO paraDetalhesServicoDTO(Servico servico) {
        return new DetalhesServicoDTO(servico.id(), servico.nome(), servico.preco(), servico.duracaoMinutos());
    }
}
