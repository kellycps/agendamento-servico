package com.fiap.agendamento_servico.domain.exceptions;

public class BusinessException extends DominioException {
    private static final long serialVersionUID = -1L;
    private static final String CODE = "BUSINESS_ERROR";
    private static final int HTTP_STATUS = 400;

    public BusinessException(String mensagem) {
        super(mensagem);
    }

    public BusinessException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }

    @Override
    public String getCode() {
        return CODE;
    }

    @Override
    public int getHttpStatus() {
        return HTTP_STATUS;
    }
}
