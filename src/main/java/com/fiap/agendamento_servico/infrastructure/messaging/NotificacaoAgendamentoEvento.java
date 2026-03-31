package com.fiap.agendamento_servico.infrastructure.messaging;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificacaoAgendamentoEvento(
        UUID agendamentoId,
        String nomeProfissional,
        String emailProfissional,
        String nomeCliente,
        String emailCliente,
        String nomeServico,
        String nomeEstabelecimento,
        LocalDateTime dataHoraInicio) 
{}
