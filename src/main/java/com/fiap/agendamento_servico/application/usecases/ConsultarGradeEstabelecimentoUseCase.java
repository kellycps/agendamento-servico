package com.fiap.agendamento_servico.application.usecases;

import com.fiap.agendamento_servico.application.dto.GradeEstabelecimentoDTO;
import com.fiap.agendamento_servico.application.dto.ProfissionalNoPeriodoDTO;
import com.fiap.agendamento_servico.application.dto.ServicoResumoDTO;
import com.fiap.agendamento_servico.application.dto.PeriodoComProfissionaisDTO;
import com.fiap.agendamento_servico.application.ports.in.ConsultarGradeEstabelecimentoUseCasePort;
import com.fiap.agendamento_servico.application.ports.out.AgendamentoRepositorioPort;
import com.fiap.agendamento_servico.application.ports.out.EstabelecimentoRepositorioPort;
import com.fiap.agendamento_servico.application.ports.out.ProfissionalRepositorioPort;
import com.fiap.agendamento_servico.application.ports.out.ServicoRepositorioPort;
import com.fiap.agendamento_servico.domain.entities.Agendamento;
import com.fiap.agendamento_servico.domain.entities.Estabelecimento;
import com.fiap.agendamento_servico.domain.entities.Profissional;
import com.fiap.agendamento_servico.domain.enums.StatusAgendamento;
import com.fiap.agendamento_servico.domain.exceptions.EntidadeNaoEncontradaException;
import com.fiap.agendamento_servico.domain.services.GeradorDeAgenda;
import com.fiap.agendamento_servico.domain.valueobjects.PeriodoAgenda;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ConsultarGradeEstabelecimentoUseCase implements ConsultarGradeEstabelecimentoUseCasePort {

    private final EstabelecimentoRepositorioPort estabelecimentoRepositorioPort;
    private final ProfissionalRepositorioPort profissionalRepositorioPort;
    private final AgendamentoRepositorioPort agendamentoRepositorioPort;
    private final ServicoRepositorioPort servicoRepositorioPort;
    private final GeradorDeAgenda geradorDeAgenda;

    public ConsultarGradeEstabelecimentoUseCase(
            EstabelecimentoRepositorioPort estabelecimentoRepositorioPort,
            ProfissionalRepositorioPort profissionalRepositorioPort,
            AgendamentoRepositorioPort agendamentoRepositorioPort,
            ServicoRepositorioPort servicoRepositorioPort,
            GeradorDeAgenda geradorDeAgenda
    ) {
        this.estabelecimentoRepositorioPort = estabelecimentoRepositorioPort;
        this.profissionalRepositorioPort = profissionalRepositorioPort;
        this.agendamentoRepositorioPort = agendamentoRepositorioPort;
        this.servicoRepositorioPort = servicoRepositorioPort;
        this.geradorDeAgenda = geradorDeAgenda;
    }

    @Override
    public GradeEstabelecimentoDTO consultarGradeCompleta(UUID estabelecimentoId, LocalDate data) {
        validarEntrada(estabelecimentoId, data);
        return montarGrade(estabelecimentoId, data, null);
    }

    @Override
    public GradeEstabelecimentoDTO consultarGradeDisponivel(UUID estabelecimentoId, LocalDate data) {
        validarEntrada(estabelecimentoId, data);
        GradeEstabelecimentoDTO gradeCompleta = montarGrade(estabelecimentoId, data, null);
        List<PeriodoComProfissionaisDTO> periodosDisponiveis = gradeCompleta.periodos().stream()
                .filter(PeriodoComProfissionaisDTO::disponivel)
                .toList();
        
        return new GradeEstabelecimentoDTO(gradeCompleta.data(), periodosDisponiveis);
    }

    @Override
    public GradeEstabelecimentoDTO consultarGradePorServico(UUID estabelecimentoId, LocalDate data, UUID servicoId) {
        validarEntrada(estabelecimentoId, data);
        if (servicoId == null) throw new IllegalArgumentException("Id do serviço não pode ser nulo");
        GradeEstabelecimentoDTO gradeCompleta = montarGrade(estabelecimentoId, data, servicoId);
        List<PeriodoComProfissionaisDTO> periodosDisponiveis = gradeCompleta.periodos().stream()
                .filter(PeriodoComProfissionaisDTO::disponivel)
                .toList();
        
        return new GradeEstabelecimentoDTO(gradeCompleta.data(), periodosDisponiveis);
    }

    private GradeEstabelecimentoDTO montarGrade(UUID estabelecimentoId, LocalDate data, UUID servicoIdFiltro) {
        Estabelecimento estabelecimento = estabelecimentoRepositorioPort.buscarPorId(estabelecimentoId)
                .orElseThrow(() -> EntidadeNaoEncontradaException.para("Estabelecimento", estabelecimentoId));

        List<Profissional> profissionais = profissionalRepositorioPort
                .listarPorEstabelecimento(estabelecimentoId);

        if (servicoIdFiltro != null) {
            final UUID filtro = servicoIdFiltro;
            profissionais = profissionais.stream()
                    .filter(p -> p.getServicosIds().contains(filtro))
                    .toList();
        }

        List<PeriodoAgenda> gradeVazia = geradorDeAgenda.gerarGradeVazia(
                estabelecimento.getHoraInicioFuncionamento(),
                estabelecimento.getHoraFimFuncionamento(),
                estabelecimento.getIntervaloMinutosPadrao()
        );

        Map<UUID, List<Agendamento>> agendamentosPorProfissional = new HashMap<>();
        for (Profissional profissional : profissionais) {
            agendamentosPorProfissional.put(
                    profissional.getId(),
                    agendamentoRepositorioPort.buscarPorProfissionalEData(profissional.getId(), data)
            );
        }

        Map<UUID, String> nomeServicos = carregarNomesServicos(profissionais, servicoIdFiltro);

        List<PeriodoComProfissionaisDTO> periodos = new ArrayList<>();

        for (PeriodoAgenda periodo : gradeVazia) {
            List<ProfissionalNoPeriodoDTO> profissionaisDisponiveis = new ArrayList<>();

            for (Profissional profissional : profissionais) {
                if (profissional.getHoraInicioTrabalho() == null || profissional.getHoraFimTrabalho() == null) {
                    continue;
                }

                if (!periodoDentroJornada(periodo, profissional.getHoraInicioTrabalho(), profissional.getHoraFimTrabalho())) {
                    continue;
                }
                
                if (profissionalOcupado(periodo, agendamentosPorProfissional.get(profissional.getId()))) {
                    continue;
                }

                List<ServicoResumoDTO> servicos = resolverServicos(profissional, servicoIdFiltro, nomeServicos);
                
                profissionaisDisponiveis.add(new ProfissionalNoPeriodoDTO(profissional.getId(), profissional.getNome(), servicos));
            }

            periodos.add(new PeriodoComProfissionaisDTO(
                    periodo.horaInicio(),
                    periodo.horaFim(),
                    !profissionaisDisponiveis.isEmpty(),
                    profissionaisDisponiveis
            ));
        }

        return new GradeEstabelecimentoDTO(data, periodos);
    }

    private boolean periodoDentroJornada(PeriodoAgenda periodo, LocalTime inicioJornada, LocalTime fimJornada) {
        return !periodo.horaInicio().isBefore(inicioJornada) && !periodo.horaFim().isAfter(fimJornada);
    }

    private boolean profissionalOcupado(PeriodoAgenda periodo, List<Agendamento> agendamentos) {
        if (agendamentos == null || agendamentos.isEmpty()) return false;
        
        return agendamentos.stream()
                .filter(a -> a.status() != StatusAgendamento.CANCELADO)
                .anyMatch(a -> periodo.horaInicio().isBefore(a.horaFim()) && periodo.horaFim().isAfter(a.horaInicio()));
    }

    private Map<UUID, String> carregarNomesServicos(List<Profissional> profissionais, UUID servicoIdFiltro) {
        Map<UUID, String> nomes = new LinkedHashMap<>();
        
        if (servicoIdFiltro != null) {
            servicoRepositorioPort.buscarPorId(servicoIdFiltro).ifPresent(s -> nomes.put(s.id(), s.nome()));
        } else {
            List<UUID> ids = profissionais.stream()
                    .flatMap(p -> p.getServicosIds().stream())
                    .distinct()
                    .toList();
            
                servicoRepositorioPort.buscarPorIds(ids).forEach(s -> nomes.put(s.id(), s.nome()));
        }

        return nomes;
    }

    private List<ServicoResumoDTO> resolverServicos(Profissional profissional, UUID servicoIdFiltro, Map<UUID, String> nomeServicos) {
        if (servicoIdFiltro != null) {
            String nome = nomeServicos.get(servicoIdFiltro);
            
            if (nome == null) {
                return List.of();
            }

            return List.of(new ServicoResumoDTO(servicoIdFiltro, nome));
        }

        return profissional.getServicosIds().stream()
                .filter(nomeServicos::containsKey)
                .map(id -> new ServicoResumoDTO(id, nomeServicos.get(id)))
                .toList();
    }

    private void validarEntrada(UUID estabelecimentoId, LocalDate data) {
        if (estabelecimentoId == null) {
            throw new IllegalArgumentException("Id do estabelecimento não pode ser nulo");
        }
        
        if (data == null) {
            throw new IllegalArgumentException("Data não pode ser nula");
        }   
    }
}
