package com.fiap.agendamento_servico.domain.exceptions;

public abstract class DominioException extends RuntimeException {
    private static final long serialVersionUID = -1L;

    public DominioException(String mensagem) {
        super(mensagem);
    }

    public DominioException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }

    public abstract String getCode();

    public abstract int getHttpStatus();
}
