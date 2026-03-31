package com.fiap.agendamento_servico.interface_adapters.controllers;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReservarAgendamentoRequest(
        UUID clienteId,
        UUID profissionalId,
        UUID servicoId,
        LocalDateTime dataHoraInicio) {}
