package com.fiap.agendamento_servico.application.usecases;

import com.fiap.agendamento_servico.application.dto.DetalhesAgendamentoDTO;
import com.fiap.agendamento_servico.application.dto.ReagendarAgendamentoDTO;
import com.fiap.agendamento_servico.application.mappers.AgendamentoMapper;
import com.fiap.agendamento_servico.application.ports.in.GerenciarAgendamentoUseCasePort;
import com.fiap.agendamento_servico.application.ports.out.AgendamentoRepositorioPort;
import com.fiap.agendamento_servico.application.ports.out.ClienteRepositorioPort;
import com.fiap.agendamento_servico.application.ports.out.EstabelecimentoRepositorioPort;
import com.fiap.agendamento_servico.application.ports.out.NotificacaoPort;
import com.fiap.agendamento_servico.application.ports.out.ProfissionalRepositorioPort;
import com.fiap.agendamento_servico.application.ports.out.ServicoRepositorioPort;
import com.fiap.agendamento_servico.domain.entities.Agendamento;
import com.fiap.agendamento_servico.domain.entities.Cliente;
import com.fiap.agendamento_servico.domain.entities.Estabelecimento;
import com.fiap.agendamento_servico.domain.entities.Profissional;
import com.fiap.agendamento_servico.domain.entities.Servico;
import com.fiap.agendamento_servico.domain.enums.StatusAgendamento;
import com.fiap.agendamento_servico.domain.exceptions.BusinessException;
import com.fiap.agendamento_servico.domain.exceptions.EntidadeNaoEncontradaException;
import com.fiap.agendamento_servico.domain.services.ValidadorAgendamento;
import java.util.List;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

public class GerenciarAgendamentoUseCase implements GerenciarAgendamentoUseCasePort {

    private final AgendamentoRepositorioPort agendamentoRepositorioPort;
    private final ServicoRepositorioPort servicoRepositorioPort;
    private final ProfissionalRepositorioPort profissionalRepositorioPort;
    private final ClienteRepositorioPort clienteRepositorioPort;
    private final EstabelecimentoRepositorioPort estabelecimentoRepositorioPort;
    private final NotificacaoPort notificacaoPort;
    private final ValidadorAgendamento validadorAgendamento;

    public GerenciarAgendamentoUseCase(
            AgendamentoRepositorioPort agendamentoRepositorioPort,
            ServicoRepositorioPort servicoRepositorioPort,
            ProfissionalRepositorioPort profissionalRepositorioPort,
            ClienteRepositorioPort clienteRepositorioPort,
            EstabelecimentoRepositorioPort estabelecimentoRepositorioPort,
            NotificacaoPort notificacaoPort,
            ValidadorAgendamento validadorAgendamento
    ) {
        this.agendamentoRepositorioPort = agendamentoRepositorioPort;
        this.servicoRepositorioPort = servicoRepositorioPort;
        this.profissionalRepositorioPort = profissionalRepositorioPort;
        this.clienteRepositorioPort = clienteRepositorioPort;
        this.estabelecimentoRepositorioPort = estabelecimentoRepositorioPort;
        this.notificacaoPort = notificacaoPort;
        this.validadorAgendamento = validadorAgendamento;
    }

    @Override
    @Transactional
    public void confirmar(UUID id) {
        Agendamento agendamento = buscarAgendamentoPorId(id);
        Agendamento agendamentoConfirmado = agendamento.confirmar();
        agendamentoRepositorioPort.salvar(agendamentoConfirmado);

        Profissional profissional = profissionalRepositorioPort
                .buscarPorId(agendamento.profissionalId())
                .orElseThrow(() -> EntidadeNaoEncontradaException.para("Profissional", agendamento.profissionalId()));

        Cliente cliente = clienteRepositorioPort
                .buscarPorId(agendamento.clienteId())
                .orElseThrow(() -> EntidadeNaoEncontradaException.para("Cliente", agendamento.clienteId()));

        Servico servico = servicoRepositorioPort
                .buscarPorId(agendamento.servicoId())
                .orElseThrow(() -> EntidadeNaoEncontradaException.para("Serviço", agendamento.servicoId()));

        Estabelecimento estabelecimento = estabelecimentoRepositorioPort
                .buscarPorId(profissional.getEstabelecimentoId())
                .orElseThrow(() -> EntidadeNaoEncontradaException.para("Estabelecimento", profissional.getEstabelecimentoId()));

        notificacaoPort.enviarConfirmacaoAgendamento(
                agendamentoConfirmado, profissional,
                cliente.nome(), cliente.email(),
                servico.nome(), estabelecimento.getNome());
    }

    @Override
    @Transactional
    public void cancelar(UUID id) {
        Agendamento agendamento = buscarAgendamentoPorId(id);
        Agendamento agendamentoCancelado = agendamento.cancelar();
        agendamentoRepositorioPort.salvar(agendamentoCancelado);
    }

    @Override
    @Transactional
    public void concluir(UUID id) {
        Agendamento agendamento = buscarAgendamentoPorId(id);
        Agendamento agendamentoConcluido = agendamento.concluir();
        agendamentoRepositorioPort.salvar(agendamentoConcluido);
    }

    @Override
    @Transactional
    public void marcarNaoComparecimento(UUID id) {
        Agendamento agendamento = buscarAgendamentoPorId(id);
        Agendamento marcado = agendamento.marcarNaoComparecimento();
        agendamentoRepositorioPort.salvar(marcado);
    }

    @Override
    @Transactional
    public void reagendar(ReagendarAgendamentoDTO dto) {
        if (dto == null || dto.agendamentoId() == null || dto.novaDataHoraInicio() == null) {
            throw new IllegalArgumentException("Dados de reagendamento não podem ser nulos");
        }

        Agendamento agendamento = buscarAgendamentoPorId(dto.agendamentoId());

        Servico servico = servicoRepositorioPort.buscarPorId(agendamento.servicoId())
                .orElseThrow(() -> EntidadeNaoEncontradaException.para("Serviço", agendamento.servicoId()));

        List<Agendamento> agendamentosDoDia = agendamentoRepositorioPort
                .buscarPorProfissionalEData(agendamento.profissionalId(), dto.novaDataHoraInicio().toLocalDate())
                .stream()
                .filter(a -> !a.id().equals(agendamento.id()))
                .filter(a -> a.status() != StatusAgendamento.CANCELADO && a.status() != StatusAgendamento.NAO_COMPARECEU)
                .toList();

        Agendamento reagendado = agendamento.reagendar(dto.novaDataHoraInicio(), servico.duracaoMinutos());
        
        validadorAgendamento.validarSemSobreposicao(reagendado, agendamentosDoDia);

        agendamentoRepositorioPort.salvar(reagendado);
    }

    @Override
    public List<DetalhesAgendamentoDTO> listarPorCliente(UUID clienteId) {
        if (clienteId == null) {
            throw new IllegalArgumentException("Id do cliente não pode ser nulo");
        }

        clienteRepositorioPort.buscarPorId(clienteId).orElseThrow(() -> EntidadeNaoEncontradaException.para("Cliente", clienteId));
        
        return agendamentoRepositorioPort.listarPorCliente(clienteId).stream()
                .map(agendamento -> {
                        String profissionalNome = profissionalRepositorioPort
                            .buscarPorId(agendamento.profissionalId())
                            .map(Profissional::getNome)
                            .orElse(agendamento.profissionalId().toString());
                        String servicoNome = servicoRepositorioPort
                            .buscarPorId(agendamento.servicoId())
                            .map(Servico::nome)
                            .orElse(agendamento.servicoId().toString());
                    
                        return AgendamentoMapper.paraDetalhesAgendamentoDTO(
                            agendamento,
                            agendamento.clienteId().toString(),
                            profissionalNome,
                            servicoNome
                    );
                }).toList();
    }

    @Override
    @Transactional
    public void deletar(UUID id) {
        Agendamento agendamento = buscarAgendamentoPorId(id);

        if (agendamento.status() != StatusAgendamento.PENDENTE && agendamento.status() != StatusAgendamento.CANCELADO) {
                throw new BusinessException(
                        "Exclusão física permitida apenas para agendamentos PENDENTE ou CANCELADO. " +
                        "Status atual: " + agendamento.status() + ". " +
                        "Use os endpoints de transição de status para outros casos.");
        }
        
        agendamentoRepositorioPort.deletar(id);
    }

    private Agendamento buscarAgendamentoPorId(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("Id do agendamento não pode ser nulo");
        }

        return agendamentoRepositorioPort
                .buscarPorId(id)
                .orElseThrow(() -> EntidadeNaoEncontradaException.para("Agendamento", id));
    }
}
