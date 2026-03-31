package com.fiap.agendamento_servico.domain.exceptions;

public class HorarioIndisponivelException extends BusinessException {
    private static final long serialVersionUID = -1L;

    public HorarioIndisponivelException(String mensagem) {
        super(mensagem);
    }

    public HorarioIndisponivelException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
