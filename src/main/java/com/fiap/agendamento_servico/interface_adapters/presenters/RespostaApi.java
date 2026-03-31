package com.fiap.agendamento_servico.interface_adapters.presenters;

public record RespostaApi<T>(
        boolean sucesso,
        String mensagem,
        T dados
) {
    public static <T> RespostaApi<T> sucesso(String mensagem, T dados) {
        return new RespostaApi<>(true, mensagem, dados);
    }

    public static <T> RespostaApi<T> erro(String mensagem) {
        return new RespostaApi<>(false, mensagem, null);
    }
}