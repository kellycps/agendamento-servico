package com.fiap.agendamento_servico.interface_adapters.presenters;

import com.fiap.agendamento_servico.application.dto.DetalhesServicoDTO;
import java.text.NumberFormat;
import java.util.Locale;
import org.springframework.stereotype.Component;

@Component
public class ServicoPresenter {

    private static final Locale LOCALE_BR = Locale.of("pt", "BR");

    public RespostaApi<ServicoResposta> apresentarCadastro(DetalhesServicoDTO dto) {
        return RespostaApi.sucesso("Serviço cadastrado com sucesso.", paraResposta(dto));
    }

    public RespostaApi<ServicoResposta> apresentarAtualizacao(DetalhesServicoDTO dto) {
        return RespostaApi.sucesso("Serviço atualizado com sucesso.", paraResposta(dto));
    }

    public RespostaApi<Void> apresentarExclusao() {
        return RespostaApi.sucesso("Serviço excluído com sucesso.", null);
    }

    private ServicoResposta paraResposta(DetalhesServicoDTO dto) {
        return new ServicoResposta(
                dto.id().toString(),
                dto.nome(),
                NumberFormat.getCurrencyInstance(LOCALE_BR).format(dto.preco()),
                dto.duracaoMinutos());
    }

    public record ServicoResposta(String id, String nome, String preco, int duracaoMinutos) {}
}
