package com.fiap.agendamento_servico.interface_adapters.presenters;

import com.fiap.agendamento_servico.application.dto.DetalhesEstabelecimentoDTO;
import com.fiap.agendamento_servico.application.dto.DetalhesProfissionalDTO;
import com.fiap.agendamento_servico.application.dto.DetalhesServicoDTO;
import com.fiap.agendamento_servico.application.dto.ServicoComProfissionaisDTO;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Component;

@Component
public class EstabelecimentoPresenter {

        private static final Locale LOCALE_BR = Locale.of("pt", "BR");
        private static final DateTimeFormatter FORMATADOR_HORA = DateTimeFormatter.ofPattern("HH:mm", LOCALE_BR);

        public RespostaApi<DetalhesEstabelecimentoResposta> apresentarCadastro(DetalhesEstabelecimentoDTO dto) {
                return RespostaApi.sucesso("Estabelecimento cadastrado com sucesso.", apresentarDetalhesInterno(dto));
        }

        public RespostaApi<DetalhesEstabelecimentoResposta> apresentarDetalhes(DetalhesEstabelecimentoDTO dto) {
                return RespostaApi.sucesso("Estabelecimento consultado com sucesso.", apresentarDetalhesInterno(dto));
        }

        public RespostaApi<List<DetalhesEstabelecimentoResposta>> apresentarListaDetalhes(List<DetalhesEstabelecimentoDTO> dtos) {
                return RespostaApi.sucesso(
                        "Estabelecimentos localizados com sucesso.",
                        dtos.stream().map(this::apresentarDetalhesInterno).toList());
        }

        public RespostaApi<List<ServicoResposta>> apresentarServicosDoEstabelecimento(List<DetalhesServicoDTO> dtos) {
                return RespostaApi.sucesso(
                        "Serviços do estabelecimento consultados com sucesso.",
                        dtos.stream().map(this::apresentarServico).toList());
        }

        public RespostaApi<List<ServicoComProfissionaisResposta>> apresentarServicosComProfissionais(List<ServicoComProfissionaisDTO> dtos) {
                return RespostaApi.sucesso(
                        "Serviços com profissionais consultados com sucesso.",
                        dtos.stream().map(this::apresentarServicoComProfissionais).toList());
        }

        private DetalhesEstabelecimentoResposta apresentarDetalhesInterno(DetalhesEstabelecimentoDTO dto) {
                List<ServicoResposta> servicos = dto.servicos().stream().map(this::apresentarServico).toList();
                
                List<ProfissionalEmbutidoResposta> profissionais = dto.profissionais().stream()
                        .map(this::apresentarProfissionalEmbutido)
                        .toList();

                return new DetalhesEstabelecimentoResposta(
                        dto.id().toString(), dto.nome(),
                        dto.endereco().rua(), dto.endereco().numero(), dto.endereco().cep(), dto.endereco().cidade(),
                        dto.endereco().bairro(), dto.endereco().complemento(),
                        dto.horaInicio().format(FORMATADOR_HORA), dto.horaFim().format(FORMATADOR_HORA), dto.intervaloMinutos(),
                        formatarNota(dto.notaMedia()),
                        servicos, profissionais, dto.fotoPrincipalUrl(), dto.galeriaUrls()
                );
        }

        private ServicoResposta apresentarServico(DetalhesServicoDTO dto) {
                return new ServicoResposta(
                        dto.id().toString(), dto.nome(),
                        formatarMoeda(dto.preco()), dto.duracaoMinutos(),
                        String.format("Serviço com duração de %d minutos.", dto.duracaoMinutos())
                );
        }

        private ServicoComProfissionaisResposta apresentarServicoComProfissionais(ServicoComProfissionaisDTO dto) {
                List<ProfissionalEmbutidoResposta> profissionais = dto.profissionais().stream().map(this::apresentarProfissionalEmbutido).toList();
                
                return new ServicoComProfissionaisResposta(
                        dto.id().toString(), dto.nome(), dto.descricao(),
                        formatarMoeda(dto.preco()), dto.duracaoMinutos(), profissionais);
        }

        private ProfissionalEmbutidoResposta apresentarProfissionalEmbutido(DetalhesProfissionalDTO dto) {
                return new ProfissionalEmbutidoResposta(
                        dto.id().toString(), dto.nome(), dto.especialidades(),
                        dto.horaInicioTrabalho() != null ? dto.horaInicioTrabalho().format(FORMATADOR_HORA) : null,
                        dto.horaFimTrabalho() != null ? dto.horaFimTrabalho().format(FORMATADOR_HORA) : null
                );
        }

        private String formatarMoeda(double valor) {
                return NumberFormat.getCurrencyInstance(LOCALE_BR).format(valor);
        }

        private String formatarNota(double notaMedia) {
                return String.format(LOCALE_BR, "%.1f", notaMedia);
        }

        public record DetalhesEstabelecimentoResposta(
                String id, String nome,
                String rua, String numero, String cep, String cidade, String bairro, String complemento,
                String horaInicio, String horaFim, int intervaloMinutos,
                String notaMedia,
                List<ServicoResposta> servicos,
                List<ProfissionalEmbutidoResposta> profissionais,
                String fotoPrincipalUrl, List<String> galeriaUrls) {}

        public record ServicoResposta(String id, String nome, String preco, int duracaoMinutos, String mensagem) {}

        public record ProfissionalEmbutidoResposta(
                String id, String nome, List<String> especialidades,
                String horaInicioTrabalho, String horaFimTrabalho) {}

        public record ServicoComProfissionaisResposta(
                String id, String nome, String descricao,
                String preco, int duracaoMinutos,
                List<ProfissionalEmbutidoResposta> profissionais) {}
}
