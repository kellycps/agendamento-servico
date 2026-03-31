package com.fiap.agendamento_servico.domain.exceptions;

public class EntidadeNaoEncontradaException extends BusinessException {
    private static final long serialVersionUID = -1L;

    public EntidadeNaoEncontradaException(String mensagem) {
        super(mensagem);
    }

    public EntidadeNaoEncontradaException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }

    public static EntidadeNaoEncontradaException para(String nomeEntidade, Object id) {
        return new EntidadeNaoEncontradaException(String.format("%s de id %s não foi encontrado", nomeEntidade, id));
    }
}
