package com.fiap.agendamento_servico.application.usecases;

import com.fiap.agendamento_servico.application.dto.DetalhesAvaliacaoDTO;
import com.fiap.agendamento_servico.application.dto.NovaAvaliacaoDTO;
import com.fiap.agendamento_servico.application.ports.in.ListarAvaliacoesUseCasePort;
import com.fiap.agendamento_servico.application.ports.in.RegistrarAvaliacaoUseCasePort;
import com.fiap.agendamento_servico.application.ports.out.AgendamentoRepositorioPort;
import com.fiap.agendamento_servico.application.ports.out.AvaliacaoRepositorioPort;
import com.fiap.agendamento_servico.application.ports.out.EstabelecimentoRepositorioPort;
import com.fiap.agendamento_servico.application.ports.out.ProfissionalRepositorioPort;
import com.fiap.agendamento_servico.domain.entities.Agendamento;
import com.fiap.agendamento_servico.domain.entities.Avaliacao;
import com.fiap.agendamento_servico.domain.entities.Estabelecimento;
import com.fiap.agendamento_servico.domain.entities.Profissional;
import com.fiap.agendamento_servico.domain.enums.StatusAgendamento;
import com.fiap.agendamento_servico.domain.exceptions.BusinessException;
import com.fiap.agendamento_servico.domain.exceptions.EntidadeNaoEncontradaException;
import com.fiap.agendamento_servico.domain.services.CalculadoraDeMediaAvaliacao;
import java.util.List;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

public class RegistrarAvaliacaoUseCase implements RegistrarAvaliacaoUseCasePort, ListarAvaliacoesUseCasePort {

    private final AgendamentoRepositorioPort agendamentoRepositorioPort;
    private final AvaliacaoRepositorioPort avaliacaoRepositorioPort;
    private final ProfissionalRepositorioPort profissionalRepositorioPort;
    private final EstabelecimentoRepositorioPort estabelecimentoRepositorioPort;
    private final CalculadoraDeMediaAvaliacao calculadoraDeMediaAvaliacao;

    public RegistrarAvaliacaoUseCase(
            AgendamentoRepositorioPort agendamentoRepositorioPort,
            AvaliacaoRepositorioPort avaliacaoRepositorioPort,
            ProfissionalRepositorioPort profissionalRepositorioPort,
            EstabelecimentoRepositorioPort estabelecimentoRepositorioPort,
            CalculadoraDeMediaAvaliacao calculadoraDeMediaAvaliacao
    ) {
        this.agendamentoRepositorioPort = agendamentoRepositorioPort;
        this.avaliacaoRepositorioPort = avaliacaoRepositorioPort;
        this.profissionalRepositorioPort = profissionalRepositorioPort;
        this.estabelecimentoRepositorioPort = estabelecimentoRepositorioPort;
        this.calculadoraDeMediaAvaliacao = calculadoraDeMediaAvaliacao;
    }

    @Override
    @Transactional
    public void executar(NovaAvaliacaoDTO novaAvaliacaoDTO) {
        Agendamento agendamento = agendamentoRepositorioPort
                .buscarPorId(novaAvaliacaoDTO.agendamentoId())
                .orElseThrow(() -> EntidadeNaoEncontradaException.para("Agendamento", novaAvaliacaoDTO.agendamentoId()));

        if (agendamento.status() != StatusAgendamento.CONCLUIDO) {
            throw new BusinessException(String.format("Apenas agendamentos concluídos podem ser avaliados. Status atual: %s", agendamento.status()));
        }

        if (avaliacaoRepositorioPort.existePorAgendamentoId(novaAvaliacaoDTO.agendamentoId())) {
            throw new BusinessException("Agendamento já foi avaliado");
        }

        Avaliacao avaliacao = Avaliacao.criar(
                novaAvaliacaoDTO.agendamentoId(),
                novaAvaliacaoDTO.nota(),
                novaAvaliacaoDTO.comentario()
        );

        avaliacaoRepositorioPort.salvar(avaliacao);

        Profissional profissional = profissionalRepositorioPort
                .buscarPorId(agendamento.profissionalId())
                .orElseThrow(() -> EntidadeNaoEncontradaException.para("Profissional", agendamento.profissionalId()));

        Estabelecimento estabelecimento = estabelecimentoRepositorioPort
                .buscarPorId(profissional.getEstabelecimentoId())
                .orElseThrow(() -> EntidadeNaoEncontradaException.para("Estabelecimento", profissional.getEstabelecimentoId()));

        List<Avaliacao> avaliacoesDoProfissional = avaliacaoRepositorioPort
                .listarPorProfissional(agendamento.profissionalId());

        calculadoraDeMediaAvaliacao.atualizarNotaMedia(profissional, avaliacoesDoProfissional);

        List<Avaliacao> avaliacoesDoEstabelecimento = avaliacaoRepositorioPort
                .listarPorEstabelecimento(estabelecimento.getId());

        calculadoraDeMediaAvaliacao.atualizarNotaMedia(estabelecimento, avaliacoesDoEstabelecimento);

        profissionalRepositorioPort.salvar(profissional);
        
        estabelecimentoRepositorioPort.salvar(estabelecimento);
    }

    @Override
    public List<DetalhesAvaliacaoDTO> listarPorEstabelecimento(UUID estabelecimentoId) {
        estabelecimentoRepositorioPort.buscarPorId(estabelecimentoId).orElseThrow(() -> EntidadeNaoEncontradaException.para("Estabelecimento", estabelecimentoId));
        
        return avaliacaoRepositorioPort.listarPorEstabelecimento(estabelecimentoId).stream()
                .map(a -> {
                    String nomeProfissional = agendamentoRepositorioPort.buscarPorId(a.agendamentoId())
                            .flatMap(ag -> profissionalRepositorioPort.buscarPorId(ag.profissionalId()))
                            .map(Profissional::getNome)
                            .orElse(null);
                    return new DetalhesAvaliacaoDTO(a.id(), a.agendamentoId(), a.nota(), a.comentario(), a.dataCriacao(), nomeProfissional);
                }).toList();
    }
}
