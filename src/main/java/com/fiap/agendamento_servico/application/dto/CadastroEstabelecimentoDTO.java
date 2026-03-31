package com.fiap.agendamento_servico.application.dto;

import java.time.LocalTime;
import java.util.List;

public record CadastroEstabelecimentoDTO(
        String nome,
        EnderecoDTO endereco,
        LocalTime horaInicio,
        LocalTime horaFim,
        int intervaloMinutos,
        String fotoPrincipalUrl,
        List<String> galeriaUrls,
        List<CadastroServicoEmbutidoDTO> servicos,
        List<CadastroProfissionalEmbutidoDTO> profissionais
) {
}
