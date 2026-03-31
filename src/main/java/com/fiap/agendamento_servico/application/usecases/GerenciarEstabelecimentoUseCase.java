package com.fiap.agendamento_servico.application.usecases;

import com.fiap.agendamento_servico.application.dto.CadastroEstabelecimentoDTO;
import com.fiap.agendamento_servico.application.dto.CadastroProfissionalEmbutidoDTO;
import com.fiap.agendamento_servico.application.dto.CadastroServicoEmbutidoDTO;
import com.fiap.agendamento_servico.application.dto.DetalhesEstabelecimentoDTO;
import com.fiap.agendamento_servico.application.dto.DetalhesProfissionalDTO;
import com.fiap.agendamento_servico.application.dto.DetalhesServicoDTO;
import com.fiap.agendamento_servico.application.dto.EnderecoDTO;
import com.fiap.agendamento_servico.application.dto.FiltroEstabelecimentoDTO;
import com.fiap.agendamento_servico.application.dto.ServicoComProfissionaisDTO;
import com.fiap.agendamento_servico.application.ports.in.AtualizarEstabelecimentoUseCasePort;
import com.fiap.agendamento_servico.application.ports.in.BuscarEstabelecimentoUseCasePort;
import com.fiap.agendamento_servico.application.ports.in.CadastrarEstabelecimentoUseCasePort;
import com.fiap.agendamento_servico.application.ports.out.AgendamentoRepositorioPort;
import com.fiap.agendamento_servico.application.ports.out.AvaliacaoRepositorioPort;
import com.fiap.agendamento_servico.application.ports.out.EstabelecimentoRepositorioPort;
import com.fiap.agendamento_servico.application.ports.out.ProfissionalRepositorioPort;
import com.fiap.agendamento_servico.application.ports.out.ServicoRepositorioPort;
import com.fiap.agendamento_servico.domain.entities.Estabelecimento;
import com.fiap.agendamento_servico.domain.entities.Profissional;
import com.fiap.agendamento_servico.domain.entities.Servico;
import com.fiap.agendamento_servico.domain.exceptions.EntidadeNaoEncontradaException;
import com.fiap.agendamento_servico.domain.valueobjects.Endereco;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public class GerenciarEstabelecimentoUseCase implements CadastrarEstabelecimentoUseCasePort, BuscarEstabelecimentoUseCasePort, AtualizarEstabelecimentoUseCasePort {

    private final EstabelecimentoRepositorioPort estabelecimentoRepositorioPort;
    private final ServicoRepositorioPort servicoRepositorioPort;
    private final ProfissionalRepositorioPort profissionalRepositorioPort;
    private final AgendamentoRepositorioPort agendamentoRepositorioPort;
    private final AvaliacaoRepositorioPort avaliacaoRepositorioPort;

    public GerenciarEstabelecimentoUseCase(
            EstabelecimentoRepositorioPort estabelecimentoRepositorioPort,
            ServicoRepositorioPort servicoRepositorioPort,
            ProfissionalRepositorioPort profissionalRepositorioPort,
            AgendamentoRepositorioPort agendamentoRepositorioPort,
            AvaliacaoRepositorioPort avaliacaoRepositorioPort
    ) {
        this.estabelecimentoRepositorioPort = estabelecimentoRepositorioPort;
        this.servicoRepositorioPort = servicoRepositorioPort;
        this.profissionalRepositorioPort = profissionalRepositorioPort;
        this.agendamentoRepositorioPort = agendamentoRepositorioPort;
        this.avaliacaoRepositorioPort = avaliacaoRepositorioPort;
    }

    @Override
    public DetalhesEstabelecimentoDTO executar(CadastroEstabelecimentoDTO cadastroEstabelecimentoDTO) {
        return cadastrar(cadastroEstabelecimentoDTO);
    }

    public DetalhesEstabelecimentoDTO cadastrar(CadastroEstabelecimentoDTO dto) {
        validarCadastro(dto);

        Estabelecimento estabelecimento = Estabelecimento.criar(
                dto.nome(),
                paraEndereco(dto.endereco()),
                dto.horaInicio(),
                dto.horaFim(),
                dto.intervaloMinutos()
        );

        estabelecimento.atualizarFotos(dto.fotoPrincipalUrl(), dto.galeriaUrls());
        
        Estabelecimento salvo = estabelecimentoRepositorioPort.salvar(estabelecimento);

        criarServicosEmbutidos(dto.servicos(), salvo.getId());
        criarProfissionaisEmbutidos(dto.profissionais(), salvo.getId());

        return paraDetalhesEstabelecimentoDTO(salvo);
    }

    @Override
    public List<DetalhesEstabelecimentoDTO> buscarPorCidade(String cidade, Double notaMinima) {
        List<Estabelecimento> estabelecimentos = cidade == null || cidade.isBlank()
                ? estabelecimentoRepositorioPort.listarTodos()
                : estabelecimentoRepositorioPort.buscarPorCidade(cidade);

        return estabelecimentos.stream()
                .filter(e -> notaMinima == null || e.getNotaMedia() >= notaMinima)
                .sorted((a, b) -> Double.compare(b.getNotaMedia(), a.getNotaMedia()))
                .map(this::paraDetalhesEstabelecimentoDTO)
                .toList();
    }

    @Override
    public List<DetalhesEstabelecimentoDTO> filtrar(FiltroEstabelecimentoDTO filtro) {
        
        if (filtro == null) {
            return estabelecimentoRepositorioPort.listarTodos().stream()
                    .map(this::paraDetalhesEstabelecimentoDTO)
                    .toList();
        }

        return estabelecimentoRepositorioPort.filtrar(filtro).stream()
                .map(this::paraDetalhesEstabelecimentoDTO)
                .toList();
    }

    @Override
    public List<DetalhesServicoDTO> listarServicosPorEstabelecimento(UUID estabelecimentoId) {
        buscarEstabelecimento(estabelecimentoId);
        
        return servicoRepositorioPort.listarPorEstabelecimento(estabelecimentoId).stream()
                .map(this::paraDetalhesServicoDTO)
                .toList();
    }

    @Override
    public List<DetalhesProfissionalDTO> listarProfissionaisPorEstabelecimento(UUID estabelecimentoId) {
        buscarEstabelecimento(estabelecimentoId);
        
        return profissionalRepositorioPort.listarPorEstabelecimento(estabelecimentoId).stream()
                .map(this::paraDetalhesProfissionalDTO)
                .toList();
    }

    @Override
    public DetalhesEstabelecimentoDTO atualizarDados(UUID estabelecimentoId, String nome, EnderecoDTO enderecoDTO,
            List<CadastroServicoEmbutidoDTO> novosServicos, List<CadastroProfissionalEmbutidoDTO> novosProfissionais) {

        if (estabelecimentoId == null) {
            throw new IllegalArgumentException("Id do estabelecimento não pode ser nulo");
        }
        
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome do estabelecimento não pode ser vazio");
        }
        
        if (enderecoDTO == null) {
            throw new IllegalArgumentException("Endereço não pode ser nulo");
        }

        Estabelecimento atual = buscarEstabelecimento(estabelecimentoId);

        Estabelecimento atualizado = new Estabelecimento(
                atual.getId(), nome, paraEndereco(enderecoDTO),
                atual.getHoraInicioFuncionamento(), atual.getHoraFimFuncionamento(),
                atual.getIntervaloMinutosPadrao(), atual.getServicosIds()
        );
        
        atualizado.atualizarNotaMedia(atual.getNotaMedia());
        
        atualizado.atualizarFotos(atual.getFotoPrincipalUrl(), atual.getGaleriaUrls());

        Estabelecimento salvo = estabelecimentoRepositorioPort.salvar(atualizado);

        criarServicosEmbutidos(novosServicos, salvo.getId());
        
        criarProfissionaisEmbutidos(novosProfissionais, salvo.getId());

        return paraDetalhesEstabelecimentoDTO(salvo);
    }

    @Override
    public DetalhesEstabelecimentoDTO configurarHorarios(
            UUID estabelecimentoId,
            LocalTime horaInicio,
            LocalTime horaFim,
            int intervaloMinutos
    ) {
        if (estabelecimentoId == null) {
            throw new IllegalArgumentException("Id do estabelecimento não pode ser nulo");
        }

        Estabelecimento atual = buscarEstabelecimento(estabelecimentoId);

        Estabelecimento atualizado = new Estabelecimento(
                atual.getId(), atual.getNome(), atual.getEndereco(),
                horaInicio, horaFim, intervaloMinutos, atual.getServicosIds()
        );
        
        atualizado.atualizarNotaMedia(atual.getNotaMedia());
        
        atualizado.atualizarFotos(atual.getFotoPrincipalUrl(), atual.getGaleriaUrls());

        Estabelecimento salvo = estabelecimentoRepositorioPort.salvar(atualizado);
        
        return paraDetalhesEstabelecimentoDTO(salvo);
    }

    @Override
    public void deletar(UUID estabelecimentoId) {
        buscarEstabelecimento(estabelecimentoId);
        
        profissionalRepositorioPort.listarPorEstabelecimento(estabelecimentoId)
                .forEach(profissional ->
                        agendamentoRepositorioPort.listarPorProfissional(profissional.getId())
                                .forEach(agendamento -> {
                                    avaliacaoRepositorioPort.deletarPorAgendamentoId(agendamento.id());
                                    agendamentoRepositorioPort.deletar(agendamento.id());
                                }));
    
        estabelecimentoRepositorioPort.deletar(estabelecimentoId);
    }

    @Override
    public List<ServicoComProfissionaisDTO> listarServicosComProfissionais(UUID estabelecimentoId) {
        buscarEstabelecimento(estabelecimentoId);
        
        List<Servico> servicos = servicoRepositorioPort.listarPorEstabelecimento(estabelecimentoId);
        
        List<Profissional> profissionais = profissionalRepositorioPort.listarPorEstabelecimento(estabelecimentoId);

        return servicos.stream()
                .map(servico -> {
                    List<DetalhesProfissionalDTO> profsDeste = profissionais.stream()
                            .filter(p -> p.getServicosIds().contains(servico.id()))
                            .map(this::paraDetalhesProfissionalDTO)
                            .toList();
                    return new ServicoComProfissionaisDTO(
                            servico.id(), servico.nome(), servico.descricao(),
                            servico.preco(), servico.duracaoMinutos(), profsDeste
                    );
                }).toList();
    }

    private Estabelecimento buscarEstabelecimento(UUID estabelecimentoId) {
        return estabelecimentoRepositorioPort
                .buscarPorId(estabelecimentoId)
                .orElseThrow(() -> EntidadeNaoEncontradaException.para("Estabelecimento", estabelecimentoId));
    }

    private void validarCadastro(CadastroEstabelecimentoDTO cadastroEstabelecimentoDTO) {
        if (cadastroEstabelecimentoDTO == null) {
            throw new IllegalArgumentException("Dados do estabelecimento não podem ser nulos");
        }

        if (cadastroEstabelecimentoDTO.nome() == null || cadastroEstabelecimentoDTO.nome().isBlank()) {
            throw new IllegalArgumentException("Nome do estabelecimento não pode ser vazio");
        }

        if (cadastroEstabelecimentoDTO.endereco() == null) {
            throw new IllegalArgumentException("Endereço do estabelecimento não pode ser nulo");
        }
    }

    private Endereco paraEndereco(EnderecoDTO enderecoDTO) {
        return new Endereco(
                enderecoDTO.rua(),
                enderecoDTO.numero(),
                enderecoDTO.complemento(),
                enderecoDTO.bairro(),
                enderecoDTO.cep(),
                enderecoDTO.cidade()
        );
    }

    private DetalhesEstabelecimentoDTO paraDetalhesEstabelecimentoDTO(Estabelecimento estabelecimento) {
        List<DetalhesServicoDTO> servicos = servicoRepositorioPort
                .listarPorEstabelecimento(estabelecimento.getId())
                .stream()
                .map(this::paraDetalhesServicoDTO)
                .toList();

        List<DetalhesProfissionalDTO> profissionais = profissionalRepositorioPort
                .listarPorEstabelecimento(estabelecimento.getId())
                .stream()
                .map(this::paraDetalhesProfissionalDTO)
                .toList();

        Endereco endereco = estabelecimento.getEndereco();
        
        EnderecoDTO enderecoDTO = new EnderecoDTO(
                endereco.rua(), endereco.numero(), endereco.complemento(),
                endereco.bairro(), endereco.cep(), endereco.cidade());

        return new DetalhesEstabelecimentoDTO(
                estabelecimento.getId(),
                estabelecimento.getNome(),
                enderecoDTO,
                estabelecimento.getHoraInicioFuncionamento(),
                estabelecimento.getHoraFimFuncionamento(),
                estabelecimento.getIntervaloMinutosPadrao(),
                estabelecimento.getNotaMedia(),
                servicos,
                profissionais,
                estabelecimento.getFotoPrincipalUrl(),
                estabelecimento.getGaleriaUrls()
        );
    }

    private DetalhesServicoDTO paraDetalhesServicoDTO(Servico servico) {
        return new DetalhesServicoDTO(servico.id(), servico.nome(), servico.preco(), servico.duracaoMinutos());
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

    private void criarServicosEmbutidos(List<CadastroServicoEmbutidoDTO> servicos, UUID estabelecimentoId) {
        if (servicos == null || servicos.isEmpty()) {
            return;
        }

        for (CadastroServicoEmbutidoDTO s : servicos) {
            servicoRepositorioPort.salvar(Servico.criar(s.nome(), s.descricao(), s.preco(), s.duracaoMinutos(), estabelecimentoId));
        }
    }

    private void criarProfissionaisEmbutidos(List<CadastroProfissionalEmbutidoDTO> profissionais, UUID estabelecimentoId) {
        if (profissionais == null || profissionais.isEmpty()) {
            return;
        }

        for (CadastroProfissionalEmbutidoDTO profissional : profissionais) {
            Profissional profissionalCriado = Profissional.criar(profissional.nome(), profissional.email(), estabelecimentoId);
            
            if (profissional.especialidades() != null) {
                profissionalCriado.atualizarEspecialidades(profissional.especialidades());
            }
            
            if (profissional.horaInicioTrabalho() != null && profissional.horaFimTrabalho() != null) {
                profissionalCriado.definirHorariosTrabalho(profissional.horaInicioTrabalho(), profissional.horaFimTrabalho());
            }
            
            profissionalRepositorioPort.salvar(profissionalCriado);
        }
    }
}
