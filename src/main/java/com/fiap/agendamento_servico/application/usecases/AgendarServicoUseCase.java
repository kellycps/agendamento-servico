package com.fiap.agendamento_servico.application.usecases;

import com.fiap.agendamento_servico.application.dto.DetalhesAgendamentoDTO;
import com.fiap.agendamento_servico.application.dto.NovoAgendamentoDTO;
import com.fiap.agendamento_servico.application.mappers.AgendamentoMapper;
import com.fiap.agendamento_servico.application.ports.in.AgendarServicoUseCasePort;
import com.fiap.agendamento_servico.application.ports.out.AgendamentoRepositorioPort;
import com.fiap.agendamento_servico.application.ports.out.ClienteRepositorioPort;
import com.fiap.agendamento_servico.application.ports.out.EstabelecimentoRepositorioPort;
import com.fiap.agendamento_servico.application.ports.out.ProfissionalRepositorioPort;
import com.fiap.agendamento_servico.application.ports.out.ServicoRepositorioPort;
import com.fiap.agendamento_servico.domain.entities.Agendamento;
import com.fiap.agendamento_servico.domain.entities.Estabelecimento;
import com.fiap.agendamento_servico.domain.entities.Profissional;
import com.fiap.agendamento_servico.domain.entities.Servico;
import com.fiap.agendamento_servico.domain.enums.StatusAgendamento;
import com.fiap.agendamento_servico.domain.exceptions.EntidadeNaoEncontradaException;
import com.fiap.agendamento_servico.domain.services.ValidadorAgendamento;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;

public class AgendarServicoUseCase implements AgendarServicoUseCasePort {

    private final ProfissionalRepositorioPort profissionalRepositorioPort;
    private final ServicoRepositorioPort servicoRepositorioPort;
    private final AgendamentoRepositorioPort agendamentoRepositorioPort;
    private final ClienteRepositorioPort clienteRepositorioPort;
    private final EstabelecimentoRepositorioPort estabelecimentoRepositorioPort;
    private final ValidadorAgendamento validadorAgendamento;

    public AgendarServicoUseCase(
            ProfissionalRepositorioPort profissionalRepositorioPort,
            ServicoRepositorioPort servicoRepositorioPort,
            AgendamentoRepositorioPort agendamentoRepositorioPort,
            ClienteRepositorioPort clienteRepositorioPort,
            EstabelecimentoRepositorioPort estabelecimentoRepositorioPort,
            ValidadorAgendamento validadorAgendamento
    ) {
        this.profissionalRepositorioPort = profissionalRepositorioPort;
        this.servicoRepositorioPort = servicoRepositorioPort;
        this.agendamentoRepositorioPort = agendamentoRepositorioPort;
        this.clienteRepositorioPort = clienteRepositorioPort;
        this.estabelecimentoRepositorioPort = estabelecimentoRepositorioPort;
        this.validadorAgendamento = validadorAgendamento;
    }

    @Override
    @Transactional
    public DetalhesAgendamentoDTO executar(NovoAgendamentoDTO novoAgendamentoDTO) {
        validarEntrada(novoAgendamentoDTO);

        clienteRepositorioPort
                .buscarPorId(novoAgendamentoDTO.clienteId())
                .orElseThrow(() -> EntidadeNaoEncontradaException.para("Cliente", novoAgendamentoDTO.clienteId()));

        Profissional profissional = profissionalRepositorioPort
                .buscarPorId(novoAgendamentoDTO.profissionalId())
                .orElseThrow(() -> EntidadeNaoEncontradaException.para("Profissional", novoAgendamentoDTO.profissionalId()));

        Servico servico = servicoRepositorioPort
                .buscarPorId(novoAgendamentoDTO.servicoId())
                .orElseThrow(() -> EntidadeNaoEncontradaException.para("Serviço", novoAgendamentoDTO.servicoId()));

        Agendamento novoAgendamento = AgendamentoMapper.paraEntidade(novoAgendamentoDTO, servico.duracaoMinutos());

        validadorAgendamento.validarHorarioTrabalho(novoAgendamento, profissional);

        Estabelecimento estabelecimento = estabelecimentoRepositorioPort
                .buscarPorId(profissional.getEstabelecimentoId())
                .orElseThrow(() -> EntidadeNaoEncontradaException.para("Estabelecimento", profissional.getEstabelecimentoId()));

        validadorAgendamento.validarAlinhamentoComGrade(novoAgendamento, estabelecimento);

        List<Agendamento> agendamentosDoDia = agendamentoRepositorioPort
                .buscarPorProfissionalEData(novoAgendamentoDTO.profissionalId(), novoAgendamentoDTO.dataHoraInicio().toLocalDate())
                .stream()
                .filter(agendamento -> agendamento.status() != StatusAgendamento.CANCELADO)
                .toList();

        validadorAgendamento.validarSemSobreposicao(novoAgendamento, agendamentosDoDia);

        Agendamento agendamentoSalvo = agendamentoRepositorioPort.salvar(novoAgendamento);

        return AgendamentoMapper.paraDetalhesAgendamentoDTO(
            agendamentoSalvo,
            novoAgendamentoDTO.clienteId().toString(),
            profissional.getNome(),
            servico.nome()
        );
    }

    private void validarEntrada(NovoAgendamentoDTO novoAgendamentoDTO) {
        if (novoAgendamentoDTO == null) {
            throw new IllegalArgumentException("Dados do novo agendamento não podem ser nulos");
        }

        if (novoAgendamentoDTO.clienteId() == null) {
            throw new IllegalArgumentException("Id do cliente não pode ser nulo");
        }

        if (novoAgendamentoDTO.profissionalId() == null) {
            throw new IllegalArgumentException("Id do profissional não pode ser nulo");
        }

        if (novoAgendamentoDTO.servicoId() == null) {
            throw new IllegalArgumentException("Id do serviço não pode ser nulo");
        }

        if (novoAgendamentoDTO.dataHoraInicio() == null) {
            throw new IllegalArgumentException("Data e hora de início não podem ser nulas");
        }
    }
}
