package com.fiap.agendamento_servico.interface_adapters.presenters;

import com.fiap.agendamento_servico.application.dto.DetalhesProfissionalDTO;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Component;

@Component
public class ProfissionalPresenter {

        private static final Locale LOCALE_BR = Locale.of("pt", "BR");
        private static final DateTimeFormatter FORMATADOR_HORA = DateTimeFormatter.ofPattern("HH:mm", LOCALE_BR);

        public RespostaApi<DetalhesProfissionalResposta> apresentarVinculo(DetalhesProfissionalDTO dto) {
                return RespostaApi.sucesso("Profissional vinculado com sucesso ao estabelecimento.", paraResposta(dto));
        }

        public RespostaApi<DetalhesProfissionalResposta> apresentarEspecialidades(DetalhesProfissionalDTO dto) {
                return RespostaApi.sucesso("Especialidades do profissional atualizadas com sucesso.", paraResposta(dto));
        }

        public RespostaApi<JornadaProfissionalResposta> apresentarHorarios(LocalTime horaInicio, LocalTime horaFim) {
                return RespostaApi.sucesso("Horários de trabalho definidos com sucesso.",
                        new JornadaProfissionalResposta(
                                horaInicio.format(FORMATADOR_HORA),
                                horaFim.format(FORMATADOR_HORA)
                        )
                );
        }

        public RespostaApi<List<DetalhesProfissionalResposta>> apresentarListagem(List<DetalhesProfissionalDTO> dtos) {
                return RespostaApi.sucesso("Profissionais do estabelecimento consultados com sucesso.", dtos.stream().map(this::paraResposta).toList());
        }

        private DetalhesProfissionalResposta paraResposta(DetalhesProfissionalDTO dto) {
                return new DetalhesProfissionalResposta(
                        dto.id().toString(),
                        dto.nome(),
                        dto.email(),
                        dto.especialidades(),
                        String.format(LOCALE_BR, "%.1f", dto.notaMedia()),
                        dto.horaInicioTrabalho() != null ? dto.horaInicioTrabalho().format(FORMATADOR_HORA) : null,
                        dto.horaFimTrabalho() != null ? dto.horaFimTrabalho().format(FORMATADOR_HORA) : null);
        }

        public record DetalhesProfissionalResposta(
                String id,
                String nome,
                String email,
                List<String> especialidades,
                String notaMedia,
                String horaInicioTrabalho,
                String horaFimTrabalho) 
        {}

        public record JornadaProfissionalResposta(
                String horaInicio,
                String horaFim)
        {}
}