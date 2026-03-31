package com.fiap.agendamento_servico.application.usecases;

import com.fiap.agendamento_servico.application.dto.DetalhesProfissionalDTO;
import com.fiap.agendamento_servico.application.ports.in.GerenciarProfissionalUseCasePort;
import com.fiap.agendamento_servico.application.ports.out.EstabelecimentoRepositorioPort;
import com.fiap.agendamento_servico.application.ports.out.AgendamentoRepositorioPort;
import com.fiap.agendamento_servico.application.ports.out.ProfissionalRepositorioPort;
import com.fiap.agendamento_servico.application.ports.out.ServicoRepositorioPort;
import com.fiap.agendamento_servico.domain.entities.Estabelecimento;
import com.fiap.agendamento_servico.domain.entities.Profissional;
import com.fiap.agendamento_servico.domain.entities.Servico;
import com.fiap.agendamento_servico.domain.exceptions.BusinessException;
import com.fiap.agendamento_servico.domain.exceptions.EntidadeNaoEncontradaException;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public class GerenciarProfissionalUseCase implements GerenciarProfissionalUseCasePort {

    private final ProfissionalRepositorioPort profissionalRepositorioPort;
    private final EstabelecimentoRepositorioPort estabelecimentoRepositorioPort;
    private final ServicoRepositorioPort servicoRepositorioPort;
    private final AgendamentoRepositorioPort agendamentoRepositorioPort;

    public GerenciarProfissionalUseCase(
            ProfissionalRepositorioPort profissionalRepositorioPort,
            EstabelecimentoRepositorioPort estabelecimentoRepositorioPort,
            ServicoRepositorioPort servicoRepositorioPort,
            AgendamentoRepositorioPort agendamentoRepositorioPort
    ) {
        this.profissionalRepositorioPort = profissionalRepositorioPort;
        this.estabelecimentoRepositorioPort = estabelecimentoRepositorioPort;
        this.servicoRepositorioPort = servicoRepositorioPort;
        this.agendamentoRepositorioPort = agendamentoRepositorioPort;
    }

    @Override
    public DetalhesProfissionalDTO cadastrar(String nome, String email, UUID estabelecimentoId, List<String> especialidades, LocalTime horaInicioTrabalho, LocalTime horaFimTrabalho) {
        buscarEstabelecimento(estabelecimentoId);
        
        Profissional profissional = Profissional.criar(nome, email, estabelecimentoId);
        
        if (especialidades != null) {
            profissional.atualizarEspecialidades(especialidades);
        }
        
        if (horaInicioTrabalho != null && horaFimTrabalho != null) {
            profissional.definirHorariosTrabalho(horaInicioTrabalho, horaFimTrabalho);
        }
        
        return paraDetalhesProfissionalDTO(profissionalRepositorioPort.salvar(profissional));
    }

    @Override
    public DetalhesProfissionalDTO atualizar(UUID profissionalId, String nome, String email, List<String> especialidades, LocalTime horaInicioTrabalho, LocalTime horaFimTrabalho) {
        Profissional profissionalAtual = buscarProfissional(profissionalId);
        
        Profissional atualizado = new Profissional(
                profissionalAtual.getId(), nome,
                email != null ? email.trim() : profissionalAtual.getEmail(),
                profissionalAtual.getEstabelecimentoId(),
                profissionalAtual.getServicosIds(), especialidades != null ? especialidades : profissionalAtual.getEspecialidades(),
                horaInicioTrabalho != null ? horaInicioTrabalho : profissionalAtual.getHoraInicioTrabalho(),
                horaFimTrabalho != null ? horaFimTrabalho : profissionalAtual.getHoraFimTrabalho());
        
        return paraDetalhesProfissionalDTO(profissionalRepositorioPort.salvar(atualizado));
    }

    @Override
    public void deletar(UUID profissionalId) {
        buscarProfissional(profissionalId);
        
        if (!agendamentoRepositorioPort.listarPorProfissional(profissionalId).isEmpty()) {
            throw new BusinessException("Profissional não pode ser removido pois possui agendamentos vinculados");
        }
        
        profissionalRepositorioPort.remover(profissionalId);
    }

    @Override
    public DetalhesProfissionalDTO vincularProfissional(String nome, UUID estabelecimentoId) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome do profissional não pode ser nulo ou vazio");
        }

        Estabelecimento estabelecimento = buscarEstabelecimento(estabelecimentoId);

        Profissional profissional = Profissional.vincular(nome, estabelecimento.getId());
        
        Profissional profissionalSalvo = profissionalRepositorioPort.salvar(profissional);

        return paraDetalhesProfissionalDTO(profissionalSalvo);
    }

    @Override
    public DetalhesProfissionalDTO atualizarEspecialidades(UUID profissionalId, List<String> especialidades) {
        Profissional profissional = buscarProfissional(profissionalId);

        profissional.atualizarEspecialidades(especialidades);
        
        Profissional profissionalSalvo = profissionalRepositorioPort.salvar(profissional);

        return paraDetalhesProfissionalDTO(profissionalSalvo);
    }

    @Override
    public void definirHorariosTrabalho(UUID profissionalId, LocalTime horaInicio, LocalTime horaFim) {
        Profissional profissional = buscarProfissional(profissionalId);
        
        Estabelecimento estabelecimento = buscarEstabelecimento(profissional.getEstabelecimentoId());

        validarJornadaDentroDoEstabelecimento(horaInicio, horaFim, estabelecimento);

        profissional.definirHorariosTrabalho(horaInicio, horaFim);
        
        profissionalRepositorioPort.salvar(profissional);
    }

    private Profissional buscarProfissional(UUID profissionalId) {
        if (profissionalId == null) {
            throw new IllegalArgumentException("Id do profissional não pode ser nulo");
        }

        return profissionalRepositorioPort
                .buscarPorId(profissionalId)
                .orElseThrow(() -> EntidadeNaoEncontradaException.para("Profissional", profissionalId));
    }

    private Estabelecimento buscarEstabelecimento(UUID estabelecimentoId) {
        if (estabelecimentoId == null) {
            throw new IllegalArgumentException("Id do estabelecimento não pode ser nulo");
        }

        return estabelecimentoRepositorioPort
                .buscarPorId(estabelecimentoId)
                .orElseThrow(() -> EntidadeNaoEncontradaException.para("Estabelecimento", estabelecimentoId));
    }

    @Override
    public List<DetalhesProfissionalDTO> listarPorEstabelecimento(UUID estabelecimentoId) {
        if (estabelecimentoId == null) {
            throw new IllegalArgumentException("Id do estabelecimento não pode ser nulo");
        }
        
        return profissionalRepositorioPort.listarPorEstabelecimento(estabelecimentoId).stream()
                .map(this::paraDetalhesProfissionalDTO)
                .toList();
    }

    @Override
    public List<DetalhesProfissionalDTO> listarPorEstabelecimentoENotaMinima(UUID estabelecimentoId, double notaMinima) {
        if (estabelecimentoId == null) {
            throw new IllegalArgumentException("Id do estabelecimento não pode ser nulo");
        }
        
        if (notaMinima < 0 || notaMinima > 5) {
            throw new BusinessException("Nota mínima deve estar entre 0 e 5");
        }
        
        buscarEstabelecimento(estabelecimentoId);
        
        return profissionalRepositorioPort.listarPorEstabelecimentoENotaMinima(estabelecimentoId, notaMinima).stream()
                .map(this::paraDetalhesProfissionalDTO)
                .toList();
    }

    @Override
    public void adicionarServico(UUID profissionalId, UUID servicoId) {
        Profissional profissional = buscarProfissional(profissionalId);
        
        Servico servico = servicoRepositorioPort.buscarPorId(servicoId).orElseThrow(() -> EntidadeNaoEncontradaException.para("Serviço", servicoId));
        
        if (!servico.estabelecimentoId().equals(profissional.getEstabelecimentoId())) {
            throw new BusinessException("O serviço não pertence ao estabelecimento do profissional");
        }
        
        profissional.adicionarServico(servicoId);
        
        profissionalRepositorioPort.salvar(profissional);
    }

    @Override
    public void removerServico(UUID profissionalId, UUID servicoId) {
        Profissional profissional = buscarProfissional(profissionalId);
        
        profissional.removerServico(servicoId);
        
        profissionalRepositorioPort.salvar(profissional);
    }

    private void validarJornadaDentroDoEstabelecimento(
            LocalTime horaInicio,
            LocalTime horaFim,
            Estabelecimento estabelecimento
    ) {
        if (horaInicio == null || horaFim == null) {
            throw new IllegalArgumentException("Hora de início e fim da jornada não podem ser nulas");
        }

        if (!horaInicio.isBefore(horaFim)) {
            throw new BusinessException("Hora de início da jornada deve ser anterior à hora de fim");
        }

        if (horaInicio.isBefore(estabelecimento.getHoraInicioFuncionamento())
                || horaFim.isAfter(estabelecimento.getHoraFimFuncionamento())) {
            throw new BusinessException("A jornada do profissional deve estar dentro do horário de funcionamento do estabelecimento");
        }
    }

    private DetalhesProfissionalDTO paraDetalhesProfissionalDTO(Profissional profissional) {
        return new DetalhesProfissionalDTO(
                profissional.getId(),
                profissional.getNome(),
                profissional.getEmail(),
                profissional.getEspecialidades(),
                profissional.getNotaMedia(),
                profissional.getHoraInicioTrabalho(),
                profissional.getHoraFimTrabalho()
        );
    }
}
