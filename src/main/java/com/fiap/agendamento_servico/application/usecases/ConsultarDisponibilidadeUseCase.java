package com.fiap.agendamento_servico.application.usecases;

import com.fiap.agendamento_servico.application.dto.GradeHorariosDTO;
import com.fiap.agendamento_servico.application.mappers.AgendamentoMapper;
import com.fiap.agendamento_servico.application.ports.in.ConsultarDisponibilidadeUseCasePort;
import com.fiap.agendamento_servico.application.ports.out.AgendamentoRepositorioPort;
import com.fiap.agendamento_servico.application.ports.out.EstabelecimentoRepositorioPort;
import com.fiap.agendamento_servico.application.ports.out.ProfissionalRepositorioPort;
import com.fiap.agendamento_servico.domain.entities.Agendamento;
import com.fiap.agendamento_servico.domain.entities.Estabelecimento;
import com.fiap.agendamento_servico.domain.entities.Profissional;
import com.fiap.agendamento_servico.domain.exceptions.EntidadeNaoEncontradaException;
import com.fiap.agendamento_servico.domain.services.GeradorDeAgenda;
import com.fiap.agendamento_servico.domain.valueobjects.PeriodoAgenda;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class ConsultarDisponibilidadeUseCase implements ConsultarDisponibilidadeUseCasePort {

    private final ProfissionalRepositorioPort profissionalRepositorioPort;
    private final EstabelecimentoRepositorioPort estabelecimentoRepositorioPort;
    private final AgendamentoRepositorioPort agendamentoRepositorioPort;
    private final GeradorDeAgenda geradorDeAgenda;

    public ConsultarDisponibilidadeUseCase(
            ProfissionalRepositorioPort profissionalRepositorioPort,
            EstabelecimentoRepositorioPort estabelecimentoRepositorioPort,
            AgendamentoRepositorioPort agendamentoRepositorioPort,
            GeradorDeAgenda geradorDeAgenda
    ) {
        this.profissionalRepositorioPort = profissionalRepositorioPort;
        this.estabelecimentoRepositorioPort = estabelecimentoRepositorioPort;
        this.agendamentoRepositorioPort = agendamentoRepositorioPort;
        this.geradorDeAgenda = geradorDeAgenda;
    }

    @Override
    public GradeHorariosDTO executar(UUID profissionalId, LocalDate data) {
        validarEntrada(profissionalId, data);

        Profissional profissional = profissionalRepositorioPort
                .buscarPorId(profissionalId)
                .orElseThrow(() -> EntidadeNaoEncontradaException.para("Profissional", profissionalId));

        Estabelecimento estabelecimento = estabelecimentoRepositorioPort
                .buscarPorId(profissional.getEstabelecimentoId())
                .orElseThrow(() -> EntidadeNaoEncontradaException.para("Estabelecimento", profissional.getEstabelecimentoId()));

        List<PeriodoAgenda> gradeVazia = geradorDeAgenda.gerarGradeVazia(
                estabelecimento.getHoraInicioFuncionamento(),
                estabelecimento.getHoraFimFuncionamento(),
                estabelecimento.getIntervaloMinutosPadrao()
        );

        List<Agendamento> agendamentosDoDia = agendamentoRepositorioPort.buscarPorProfissionalEData(profissionalId, data);

        List<PeriodoAgenda> gradeComDisponibilidade = geradorDeAgenda.mapearDisponibilidade(gradeVazia, agendamentosDoDia);

        return AgendamentoMapper.paraGradeHorariosDTO(data, profissional.getNome(), gradeComDisponibilidade);
    }

    private void validarEntrada(UUID profissionalId, LocalDate data) {
        if (profissionalId == null) {
            throw new IllegalArgumentException("Id do profissional não pode ser nulo");
        }

        if (data == null) {
            throw new IllegalArgumentException("Data não pode ser nula");
        }
    }
}
