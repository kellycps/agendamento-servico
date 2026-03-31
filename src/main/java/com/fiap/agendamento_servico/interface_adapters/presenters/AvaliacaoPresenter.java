package com.fiap.agendamento_servico.interface_adapters.presenters;

import com.fiap.agendamento_servico.application.dto.DetalhesAvaliacaoDTO;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class AvaliacaoPresenter {

        private static final Locale LOCALE_BR = Locale.of("pt", "BR");
        private static final DateTimeFormatter FORMATADOR_DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", LOCALE_BR);

        public RespostaApi<RegistroAvaliacaoResposta> apresentarRegistro(UUID agendamentoId, int nota) {
                return RespostaApi.sucesso(
                        "Avaliação registrada com sucesso.",
                        new RegistroAvaliacaoResposta(
                                agendamentoId.toString(),
                                nota)
                );
        }

    public RespostaApi<List<DetalhesAvaliacaoResposta>> apresentarListagem(List<DetalhesAvaliacaoDTO> dtos) {
        return RespostaApi.sucesso(
                "Avaliações consultadas com sucesso.",
                dtos.stream().map(dto -> new DetalhesAvaliacaoResposta(
                        dto.id().toString(),
                        dto.agendamentoId().toString(),
                        dto.nota(),
                        dto.comentario(),
                        dto.dataCriacao() != null ? dto.dataCriacao().format(FORMATADOR_DATA_HORA) : null,
                        dto.nomeProfissional()
                )).toList());
    }

    public record RegistroAvaliacaoResposta(String agendamentoId, int nota) {}

    public record DetalhesAvaliacaoResposta(
            String id,
            String agendamentoId,
            int nota,
            String comentario,
            String dataCriacao,
            String nomeProfissional)
    {}
}