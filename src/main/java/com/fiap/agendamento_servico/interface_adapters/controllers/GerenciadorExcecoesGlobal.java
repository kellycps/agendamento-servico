package com.fiap.agendamento_servico.interface_adapters.controllers;

import com.fiap.agendamento_servico.domain.exceptions.BusinessException;
import com.fiap.agendamento_servico.domain.exceptions.EntidadeNaoEncontradaException;
import com.fiap.agendamento_servico.domain.exceptions.HorarioIndisponivelException;
import com.fiap.agendamento_servico.interface_adapters.presenters.RespostaApi;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GerenciadorExcecoesGlobal {

    @ExceptionHandler(HorarioIndisponivelException.class)
    public ResponseEntity<RespostaApi<Void>> tratarHorarioIndisponivel(HorarioIndisponivelException exception) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT).body(RespostaApi.erro(exception.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<RespostaApi<Void>> tratarArgumentoInvalido(IllegalArgumentException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(RespostaApi.erro(exception.getMessage()));
    }

    @ExceptionHandler(EntidadeNaoEncontradaException.class)
    public ResponseEntity<RespostaApi<Void>> tratarEntidadeNaoEncontrada(EntidadeNaoEncontradaException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(RespostaApi.erro(exception.getMessage()));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<RespostaApi<Void>> tratarNegocio(BusinessException exception) {
        return ResponseEntity.status(exception.getHttpStatus()).body(RespostaApi.erro(exception.getMessage()));
    }
}