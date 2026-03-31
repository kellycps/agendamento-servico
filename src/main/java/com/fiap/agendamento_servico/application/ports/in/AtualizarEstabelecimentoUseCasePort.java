package com.fiap.agendamento_servico.application.ports.in;

import com.fiap.agendamento_servico.application.dto.CadastroProfissionalEmbutidoDTO;
import com.fiap.agendamento_servico.application.dto.CadastroServicoEmbutidoDTO;
import com.fiap.agendamento_servico.application.dto.DetalhesEstabelecimentoDTO;
import com.fiap.agendamento_servico.application.dto.EnderecoDTO;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public interface AtualizarEstabelecimentoUseCasePort {
    DetalhesEstabelecimentoDTO atualizarDados(UUID estabelecimentoId, String nome, EnderecoDTO enderecoDTO,
            List<CadastroServicoEmbutidoDTO> novosServicos,
            List<CadastroProfissionalEmbutidoDTO> novosProfissionais);

    DetalhesEstabelecimentoDTO configurarHorarios(UUID estabelecimentoId, LocalTime horaInicio, LocalTime horaFim, int intervaloMinutos);

    void deletar(UUID estabelecimentoId);
}
