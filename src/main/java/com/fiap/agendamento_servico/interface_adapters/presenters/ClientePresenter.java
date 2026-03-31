package com.fiap.agendamento_servico.interface_adapters.presenters;

import com.fiap.agendamento_servico.application.dto.DetalhesClienteDTO;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ClientePresenter {

    public RespostaApi<ClienteResposta> apresentarCadastro(DetalhesClienteDTO dto) {
        return RespostaApi.sucesso("Cliente cadastrado com sucesso.", paraResposta(dto));
    }

    public RespostaApi<ClienteResposta> apresentarAtualizacao(DetalhesClienteDTO dto) {
        return RespostaApi.sucesso("Cliente atualizado com sucesso.", paraResposta(dto));
    }

    public RespostaApi<ClienteResposta> apresentarDetalhes(DetalhesClienteDTO dto) {
        return RespostaApi.sucesso("Cliente encontrado.", paraResposta(dto));
    }

    public RespostaApi<List<ClienteResposta>> apresentarLista(List<DetalhesClienteDTO> dtos) {
        return RespostaApi.sucesso("Clientes listados com sucesso.", dtos.stream().map(this::paraResposta).toList());
    }

    private ClienteResposta paraResposta(DetalhesClienteDTO dto) {
        return new ClienteResposta(dto.id().toString(), dto.nome(), dto.telefone(), dto.email());
    }

    public record ClienteResposta(String id, String nome, String telefone, String email) {}
}
