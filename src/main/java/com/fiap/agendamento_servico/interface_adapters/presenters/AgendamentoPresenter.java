package com.fiap.agendamento_servico.interface_adapters.presenters;

import com.fiap.agendamento_servico.application.dto.DetalhesAgendamentoDTO;
import com.fiap.agendamento_servico.application.dto.GradeEstabelecimentoDTO;
import com.fiap.agendamento_servico.application.dto.GradeHorariosDTO;
import com.fiap.agendamento_servico.application.dto.PeriodoAgendaDTO;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Component;

@Component
public class AgendamentoPresenter {

    private static final Locale LOCALE_BR = Locale.of("pt", "BR");
    private static final DateTimeFormatter FORMATADOR_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy", LOCALE_BR);
    private static final DateTimeFormatter FORMATADOR_HORA = DateTimeFormatter.ofPattern("HH:mm", LOCALE_BR);
    private static final DateTimeFormatter FORMATADOR_DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", LOCALE_BR);

    public RespostaApi<DetalhesAgendamentoResposta> apresentarDetalhes(DetalhesAgendamentoDTO dto) {
        return RespostaApi.sucesso(
            "Agendamento processado com sucesso.",
            new DetalhesAgendamentoResposta(
                dto.id().toString(),
                dto.clienteNome(),
                dto.profissionalNome(),
                dto.servicoNome(),
                formatarDataHora(dto.dataHoraInicio()),
                formatarDataHora(dto.dataHoraFim()),
                dto.status().name())
            );
    }

    public RespostaApi<DetalhesAgendamentoResposta> apresentarReservaCriada(DetalhesAgendamentoDTO dto) {
        return RespostaApi.sucesso(
            "Agendamento realizado com sucesso.",
            new DetalhesAgendamentoResposta(
                dto.id().toString(),
                dto.clienteNome(),
                dto.profissionalNome(),
                dto.servicoNome(),
                formatarDataHora(dto.dataHoraInicio()),
                formatarDataHora(dto.dataHoraFim()),
                dto.status().name())
        );
    }

    public RespostaApi<GradeHorariosResposta> apresentarGrade(GradeHorariosDTO dto) {
        List<PeriodoAgendaResposta> periodos = dto.periodos().stream().map(this::apresentarPeriodo).toList();

        return RespostaApi.sucesso(
            "Disponibilidade consultada com sucesso.",
            new GradeHorariosResposta(
                formatarData(dto.data()),
                dto.profissionalNome(),
                periodos)
        );
    }

    public RespostaApi<AcaoAgendamentoResposta> apresentarConfirmacao(String id) {
        return RespostaApi.sucesso("Agendamento confirmado com sucesso.", new AcaoAgendamentoResposta(id, "CONFIRMADO"));
    }

    public RespostaApi<AcaoAgendamentoResposta> apresentarCancelamento(String id) {
        return RespostaApi.sucesso("Agendamento cancelado com sucesso.", new AcaoAgendamentoResposta(id, "CANCELADO"));
    }

    public RespostaApi<AcaoAgendamentoResposta> apresentarConclusao(String id) {
        return RespostaApi.sucesso("Agendamento concluído com sucesso.", new AcaoAgendamentoResposta(id, "CONCLUIDO"));
    }

    public RespostaApi<AcaoAgendamentoResposta> apresentarNaoComparecimento(String id) {
        return RespostaApi.sucesso("Não comparecimento registrado com sucesso.", new AcaoAgendamentoResposta(id, "NAO_COMPARECEU"));
    }

    public RespostaApi<AcaoReagendarResposta> apresentarReagendamento(String id, LocalDateTime novaDataHoraInicio) {
        return RespostaApi.sucesso("Agendamento reagendado com sucesso.", new AcaoReagendarResposta(id, formatarDataHora(novaDataHoraInicio), "PENDENTE"));
    }

    public RespostaApi<List<DetalhesAgendamentoResposta>> apresentarListaDoCliente(List<DetalhesAgendamentoDTO> dtos) {
        return RespostaApi.sucesso(
            "Agendamentos consultados com sucesso.",
            dtos.stream().map(dto -> new DetalhesAgendamentoResposta(
                dto.id().toString(),
                dto.clienteNome(),
                dto.profissionalNome(),
                dto.servicoNome(),
                formatarDataHora(dto.dataHoraInicio()),
                formatarDataHora(dto.dataHoraFim()),
                dto.status().name()
            )).toList()
        );
    }

    private PeriodoAgendaResposta apresentarPeriodo(PeriodoAgendaDTO dto) {
        String mensagem = dto.disponivel() ? "Horário disponível" : "Horário indisponível";
        
        return new PeriodoAgendaResposta(formatarHora(dto.horaInicio()), formatarHora(dto.horaFim()), dto.disponivel(), mensagem);
    }

    public RespostaApi<GradeEstabelecimentoResposta> apresentarGradeEstabelecimento(GradeEstabelecimentoDTO dto) {
        return RespostaApi.sucesso(
            "Grade de horários consultada com sucesso.",
            new GradeEstabelecimentoResposta(
                formatarData(dto.data()),
                dto.periodos().stream().map(periodo -> new PeriodoComProfissionaisResposta(
                    formatarHora(periodo.horaInicio()),
                    formatarHora(periodo.horaFim()),
                    periodo.disponivel(),
                    periodo.disponivel() ? "Horário disponível" : "Horário indisponível",
                    periodo.profissionaisDisponiveis().stream().map(p -> new ProfissionalNoPeriodoResposta(
                        p.id().toString(),
                        p.nome(),
                        p.servicos().stream()
                            .map(s -> new ServicoPeriodoResposta(s.id().toString(), s.nome()))
                            .toList()
                    )).toList()
                )).toList()
            )
        );
    }

    private String formatarData(LocalDate data) {
        return data.format(FORMATADOR_DATA);
    }

    private String formatarHora(LocalTime hora) {
        return hora.format(FORMATADOR_HORA);
    }

    private String formatarDataHora(LocalDateTime dataHora) {
        return dataHora.format(FORMATADOR_DATA_HORA);
    }

    public record DetalhesAgendamentoResposta(
            String id,
            String cliente,
            String profissional,
            String servico,
            String dataHoraInicio,
            String dataHoraFim,
            String status) 
    {}

    public record GradeHorariosResposta(
            String data,
            String profissional,
            List<PeriodoAgendaResposta> periodos) 
    {}

    public record PeriodoAgendaResposta(
            String horaInicio,
            String horaFim,
            boolean disponivel,
            String mensagem) 
    {}

    public record AcaoAgendamentoResposta(
            String id,
            String status) 
    {}

    public record AcaoReagendarResposta(
            String id,
            String novaDataHoraInicio,
            String status) 
    {}

    public record GradeEstabelecimentoResposta(String data, List<PeriodoComProfissionaisResposta> periodos) {}

    public record PeriodoComProfissionaisResposta(
            String horaInicio,
            String horaFim,
            boolean disponivel,
            String mensagem,
            List<ProfissionalNoPeriodoResposta> profissionaisDisponiveis) 
    {}

    public record ProfissionalNoPeriodoResposta(String id, String nome, List<ServicoPeriodoResposta> servicos) {}

    public record ServicoPeriodoResposta(String id, String nome) {}
}