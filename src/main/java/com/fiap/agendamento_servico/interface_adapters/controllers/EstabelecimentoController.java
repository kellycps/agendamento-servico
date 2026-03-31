package com.fiap.agendamento_servico.interface_adapters.controllers;

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
import com.fiap.agendamento_servico.interface_adapters.presenters.EstabelecimentoPresenter;
import com.fiap.agendamento_servico.interface_adapters.presenters.ProfissionalPresenter;
import com.fiap.agendamento_servico.interface_adapters.presenters.RespostaApi;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Estabelecimentos", description = "Cadastro e gerenciamento de estabelecimentos")
@RestController
@RequestMapping("/estabelecimentos")
public class EstabelecimentoController {

        private final CadastrarEstabelecimentoUseCasePort cadastrarEstabelecimentoUseCasePort;
        private final BuscarEstabelecimentoUseCasePort buscarEstabelecimentoUseCasePort;
        private final AtualizarEstabelecimentoUseCasePort atualizarEstabelecimentoUseCasePort;
        private final EstabelecimentoPresenter estabelecimentoPresenter;
        private final ProfissionalPresenter profissionalPresenter;

        public EstabelecimentoController(
                CadastrarEstabelecimentoUseCasePort cadastrarEstabelecimentoUseCasePort,
                BuscarEstabelecimentoUseCasePort buscarEstabelecimentoUseCasePort,
                AtualizarEstabelecimentoUseCasePort atualizarEstabelecimentoUseCasePort,
                EstabelecimentoPresenter estabelecimentoPresenter,
                ProfissionalPresenter profissionalPresenter)
        {
                this.cadastrarEstabelecimentoUseCasePort = cadastrarEstabelecimentoUseCasePort;
                this.buscarEstabelecimentoUseCasePort = buscarEstabelecimentoUseCasePort;
                this.atualizarEstabelecimentoUseCasePort = atualizarEstabelecimentoUseCasePort;
                this.estabelecimentoPresenter = estabelecimentoPresenter;
                this.profissionalPresenter = profissionalPresenter;
        }

        @Operation(summary = "Cadastrar estabelecimento", description = "Cria um estabelecimento com serviços e profissionais opcionais")
        @ApiResponses({
                @ApiResponse(responseCode = "201", description = "Estabelecimento criado com sucesso")
        })
        @PostMapping
        @ResponseStatus(HttpStatus.CREATED)
        public RespostaApi<EstabelecimentoPresenter.DetalhesEstabelecimentoResposta> cadastrar(@RequestBody CadastroEstabelecimentoRequest request) {
                CadastroEstabelecimentoDTO dto = new CadastroEstabelecimentoDTO(
                request.nome(),
                        new EnderecoDTO(
                                request.endereco().rua(),
                                request.endereco().numero(),
                                request.endereco().complemento(),
                                request.endereco().bairro(),
                                request.endereco().cep(),
                                request.endereco().cidade()),
                request.horaInicio(),
                request.horaFim(),
                request.intervaloMinutos(),
                request.fotoPrincipalUrl(),
                request.galeriaUrls(),
                request.servicos() == null ? null : request.servicos().stream()
                        .map(s -> new CadastroServicoEmbutidoDTO(s.nome(), s.descricao(), s.preco(), s.duracaoMinutos()))
                        .toList(),
                request.profissionais() == null ? null : request.profissionais().stream()
                        .map(p -> new CadastroProfissionalEmbutidoDTO(p.nome(), p.email(), p.especialidades(), p.horaInicioTrabalho(), p.horaFimTrabalho()))
                        .toList());

                return estabelecimentoPresenter.apresentarCadastro(cadastrarEstabelecimentoUseCasePort.executar(dto));
        }

        @Operation(summary = "Buscar estabelecimentos por cidade", description = "Filtra estabelecimentos por cidade e/ou nota mínima")
        @ApiResponses({
                @ApiResponse(responseCode = "200", description = "Lista de estabelecimentos"),
                @ApiResponse(responseCode = "204", description = "Nenhum resultado encontrado")
        })
        @GetMapping
        public ResponseEntity<RespostaApi<List<EstabelecimentoPresenter.DetalhesEstabelecimentoResposta>>> buscarPorCidade(@RequestParam(required = false) String cidade, @RequestParam(required = false) Double notaMinima) {
        List<DetalhesEstabelecimentoDTO> resultado = buscarEstabelecimentoUseCasePort.buscarPorCidade(cidade, notaMinima);

                if (resultado.isEmpty()) {
                        return ResponseEntity.noContent().build();
                }

                return ResponseEntity.ok(estabelecimentoPresenter.apresentarListaDetalhes(resultado));
        }

        @Operation(summary = "Buscar com filtros avançados", description = "Filtra por nome, cidade, avaliação e faixa de preço")
        @ApiResponses({
                @ApiResponse(responseCode = "200", description = "Lista de estabelecimentos"),
                @ApiResponse(responseCode = "204", description = "Nenhum resultado encontrado")
        })
        @GetMapping("/busca")
        public ResponseEntity<RespostaApi<List<EstabelecimentoPresenter.DetalhesEstabelecimentoResposta>>> buscarComFiltros(
                @RequestParam(required = false) String nome,
                @RequestParam(required = false) String cidade,
                @RequestParam(required = false) Double avaliacaoMinima,
                @RequestParam(required = false) Double precoMinimo,
                @RequestParam(required = false) Double precoMaximo)
        {
                FiltroEstabelecimentoDTO filtro = new FiltroEstabelecimentoDTO(cidade, nome, avaliacaoMinima, precoMinimo, precoMaximo);

                List<DetalhesEstabelecimentoDTO> resultado = buscarEstabelecimentoUseCasePort.filtrar(filtro);

                if (resultado.isEmpty()) {
                        return ResponseEntity.noContent().build();
                }

                return ResponseEntity.ok(estabelecimentoPresenter.apresentarListaDetalhes(resultado));
        }

        @Operation(summary = "Listar serviços do estabelecimento")
        @ApiResponses({
                @ApiResponse(responseCode = "200", description = "Lista de serviços"),
                @ApiResponse(responseCode = "204", description = "Nenhum serviço cadastrado")
        })
        @GetMapping("/{id}/servicos")
        public ResponseEntity<RespostaApi<List<EstabelecimentoPresenter.ServicoResposta>>> listarServicos(@PathVariable UUID id) {
                List<DetalhesServicoDTO> resultado = buscarEstabelecimentoUseCasePort.listarServicosPorEstabelecimento(id);
                
                if (resultado.isEmpty()) {
                        return ResponseEntity.noContent().build();
                }

                return ResponseEntity.ok(estabelecimentoPresenter.apresentarServicosDoEstabelecimento(resultado));
        }

        @Operation(summary = "Listar profissionais do estabelecimento")
        @ApiResponses({
                @ApiResponse(responseCode = "200", description = "Lista de profissionais"),
                @ApiResponse(responseCode = "204", description = "Nenhum profissional cadastrado")
        })
        @GetMapping("/{id}/profissionais")
        public ResponseEntity<RespostaApi<List<ProfissionalPresenter.DetalhesProfissionalResposta>>> listarProfissionais(@PathVariable UUID id) {
                List<DetalhesProfissionalDTO> resultado = buscarEstabelecimentoUseCasePort.listarProfissionaisPorEstabelecimento(id);

                if (resultado.isEmpty()) {
                        return ResponseEntity.noContent().build();
                }

                return ResponseEntity.ok(profissionalPresenter.apresentarListagem(resultado));
        }

        @Operation(summary = "Atualizar dados do estabelecimento")
        @ApiResponses({
                @ApiResponse(responseCode = "200", description = "Estabelecimento atualizado"),
                @ApiResponse(responseCode = "404", description = "Estabelecimento não encontrado")
        })
        @PutMapping("/{id}")
        public RespostaApi<EstabelecimentoPresenter.DetalhesEstabelecimentoResposta> atualizarDados(
                @PathVariable UUID id,
                @RequestBody AtualizarEstabelecimentoRequest request)
        {
                List<CadastroServicoEmbutidoDTO> novosServicos = request.servicos() == null ? null : request.servicos().stream()
                        .map(s -> new CadastroServicoEmbutidoDTO(s.nome(), s.descricao(), s.preco(), s.duracaoMinutos()))
                        .toList();

                List<CadastroProfissionalEmbutidoDTO> novosProfissionais = request.profissionais() == null ? null : request.profissionais().stream()
                        .map(p -> new CadastroProfissionalEmbutidoDTO(p.nome(), p.email(), p.especialidades(), p.horaInicioTrabalho(), p.horaFimTrabalho()))
                        .toList();

                return estabelecimentoPresenter.apresentarDetalhes(
                        atualizarEstabelecimentoUseCasePort.atualizarDados(id, request.nome(),
                                new EnderecoDTO(request.endereco().rua(), request.endereco().numero(),
                                        request.endereco().complemento(), request.endereco().bairro(),
                                        request.endereco().cep(), request.endereco().cidade()),
                                novosServicos, novosProfissionais));
        }

        @Operation(summary = "Remover estabelecimento")
        @ApiResponses({
                @ApiResponse(responseCode = "204", description = "Estabelecimento removido"),
                @ApiResponse(responseCode = "404", description = "Estabelecimento não encontrado")
        })
        @DeleteMapping("/{id}")
        @ResponseStatus(HttpStatus.NO_CONTENT)
        public void deletar(@PathVariable UUID id) {
                atualizarEstabelecimentoUseCasePort.deletar(id);
        }

        @Operation(summary = "Listar serviços com profissionais", description = "Retorna cada serviço do estabelecimento com os profissionais que o realizam")
        @ApiResponses({
                @ApiResponse(responseCode = "200", description = "Lista de serviços com profissionais"),
                @ApiResponse(responseCode = "204", description = "Nenhum dado encontrado")
        })
        @GetMapping("/{id}/servicos/completo")
        public ResponseEntity<RespostaApi<List<EstabelecimentoPresenter.ServicoComProfissionaisResposta>>> listarServicosComProfissionais(@PathVariable UUID id) {
                List<ServicoComProfissionaisDTO> resultado = buscarEstabelecimentoUseCasePort.listarServicosComProfissionais(id);

                if (resultado.isEmpty()) {
                        return ResponseEntity.noContent().build();
                }
                        return ResponseEntity.ok(estabelecimentoPresenter.apresentarServicosComProfissionais(resultado));
                }

        @Operation(summary = "Configurar horários de funcionamento")
        @ApiResponses({
                @ApiResponse(responseCode = "200", description = "Horários configurados com sucesso"),
                @ApiResponse(responseCode = "404", description = "Estabelecimento não encontrado")
        })
        @PutMapping("/{id}/horarios")
        public RespostaApi<EstabelecimentoPresenter.DetalhesEstabelecimentoResposta> configurarHorarios(@PathVariable UUID id, @RequestBody ConfigurarHorariosRequest request){
                return estabelecimentoPresenter.apresentarDetalhes(atualizarEstabelecimentoUseCasePort.configurarHorarios(id, request.horaInicio(), request.horaFim(), request.intervaloMinutos()));
        }

        public record CadastroEstabelecimentoRequest(
                String nome,
                EnderecoRequest endereco,
                LocalTime horaInicio,
                LocalTime horaFim,
                int intervaloMinutos,
                String fotoPrincipalUrl,
                List<String> galeriaUrls,
                List<ServicoEmbutidoRequest> servicos,
                List<ProfissionalEmbutidoRequest> profissionais)
        {}

        public record EnderecoRequest(
                String rua,
                String numero,
                String complemento,
                String bairro,
                String cep,
                String cidade)
        {}

        public record ServicoEmbutidoRequest(
                String nome,
                String descricao,
                double preco,
                int duracaoMinutos)
        {}

        public record ProfissionalEmbutidoRequest(
                String nome,
                String email,
                List<String> especialidades,
                LocalTime horaInicioTrabalho,
                LocalTime horaFimTrabalho)
        {}

        public record AtualizarEstabelecimentoRequest(
                String nome,
                EnderecoRequest endereco,
                List<ServicoEmbutidoRequest> servicos,
                List<ProfissionalEmbutidoRequest> profissionais)
        {}

        public record ConfigurarHorariosRequest(
                LocalTime horaInicio,
                LocalTime horaFim,
                int intervaloMinutos)
        {}
}