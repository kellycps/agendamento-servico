package com.fiap.agendamento_servico.application.dto;

import java.util.List;
import java.util.UUID;

public record ProfissionalNoPeriodoDTO(UUID id, String nome, List<ServicoResumoDTO> servicos) {
}
