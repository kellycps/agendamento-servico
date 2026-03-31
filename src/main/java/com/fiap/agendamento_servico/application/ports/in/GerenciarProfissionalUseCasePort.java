package com.fiap.agendamento_servico.application.ports.in;

import com.fiap.agendamento_servico.application.dto.DetalhesProfissionalDTO;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public interface GerenciarProfissionalUseCasePort {
    DetalhesProfissionalDTO vincularProfissional(String nome, UUID estabelecimentoId);

    DetalhesProfissionalDTO cadastrar(String nome, String email, UUID estabelecimentoId, List<String> especialidades, LocalTime horaInicioTrabalho, LocalTime horaFimTrabalho);

    DetalhesProfissionalDTO atualizarEspecialidades(UUID profissionalId, List<String> especialidades);

    DetalhesProfissionalDTO atualizar(UUID profissionalId, String nome, String email, List<String> especialidades, LocalTime horaInicioTrabalho, LocalTime horaFimTrabalho);

    void definirHorariosTrabalho(UUID profissionalId, LocalTime horaInicio, LocalTime horaFim);

    void deletar(UUID profissionalId);

    List<DetalhesProfissionalDTO> listarPorEstabelecimento(UUID estabelecimentoId);

    List<DetalhesProfissionalDTO> listarPorEstabelecimentoENotaMinima(UUID estabelecimentoId, double notaMinima);

    void adicionarServico(UUID profissionalId, UUID servicoId);

    void removerServico(UUID profissionalId, UUID servicoId);
}