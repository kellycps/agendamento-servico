package com.fiap.agendamento_servico.bdd.steps;

import com.fiap.agendamento_servico.infrastructure.persistence.entities.EnderecoEmbeddable;
import com.fiap.agendamento_servico.infrastructure.persistence.entities.EstabelecimentoEntity;
import com.fiap.agendamento_servico.infrastructure.persistence.entities.ProfissionalEntity;
import com.fiap.agendamento_servico.infrastructure.persistence.entities.ServicoEntity;
import com.fiap.agendamento_servico.infrastructure.persistence.repositories.JpaAgendamentoRepository;
import com.fiap.agendamento_servico.infrastructure.persistence.repositories.JpaClienteRepository;
import com.fiap.agendamento_servico.infrastructure.persistence.repositories.JpaEstabelecimentoRepository;
import com.fiap.agendamento_servico.infrastructure.persistence.repositories.JpaProfissionalRepository;
import com.fiap.agendamento_servico.infrastructure.persistence.repositories.JpaServicoRepository;
import com.jayway.jsonpath.JsonPath;
import io.cucumber.java.Before;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class AgendamentoSteps {

    @Autowired 
    private MockMvc mockMvc;
    
    @Autowired 
    private JpaAgendamentoRepository agendamentoRepository;
    
    @Autowired 
    private JpaClienteRepository clienteRepository;
    
    @Autowired 
    private JpaEstabelecimentoRepository estabelecimentoRepository;
    
    @Autowired 
    private JpaProfissionalRepository profissionalRepository;
    
    @Autowired 
    private JpaServicoRepository servicoRepository;

    private UUID clienteId;
    private UUID profissionalId;
    private UUID servicoId;
    private String ultimoAgendamentoId;
    private MvcResult ultimoResultado;

    @Before
    public void limparBanco() {
        agendamentoRepository.deleteAll();
        profissionalRepository.deleteAll();
        servicoRepository.deleteAll();
        clienteRepository.deleteAll();
        estabelecimentoRepository.deleteAll();
    }


    @Dado("que existe um estabelecimento com profissional disponível")
    public void existeEstabelecimentoComProfissional() {
        UUID estabelecimentoId = UUID.randomUUID();
        servicoId      = UUID.randomUUID();
        profissionalId = UUID.randomUUID();

        EstabelecimentoEntity estab = new EstabelecimentoEntity();
        estab.setId(estabelecimentoId);
        estab.setNome("Salão BDD");
        estab.setEndereco(new EnderecoEmbeddable("Av Ana Costa", "100", null, "Gonzaga", "01001-000", "Santos"));
        estab.setHoraInicioFuncionamento(LocalTime.of(8, 0));
        estab.setHoraFimFuncionamento(LocalTime.of(20, 0));
        estab.setIntervaloMinutosPadrao(30);
        estabelecimentoRepository.save(estab);

        ServicoEntity servico = new ServicoEntity();
        servico.setId(servicoId);
        servico.setNome("Corte BDD");
        servico.setDescricao("Serviço para teste BDD");
        servico.setPreco(80.0);
        servico.setDuracaoMinutos(30);
        servico.setEstabelecimento(estab);
        servicoRepository.save(servico);

        ProfissionalEntity profissional = new ProfissionalEntity();
        profissional.setId(profissionalId);
        profissional.setNome("Profissional BDD");
        profissional.setEmail("profissional@email.com");
        profissional.setEstabelecimento(estab);
        profissional.setHoraInicioTrabalho(LocalTime.of(8, 0));
        profissional.setHoraFimTrabalho(LocalTime.of(18, 0));
        profissional.setServicosIds(List.of(servicoId));
        profissional.setEspecialidades(List.of("Corte"));
        profissionalRepository.save(profissional);
    }

    @E("que existe um cliente cadastrado no sistema")
    public void existeClienteCadastrado() throws Exception {
        String resposta = mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Cliente BDD","telefone":"11999990099","email":"cliente@email.com"}
                                """)).andReturn().getResponse().getContentAsString();
        
        clienteId = UUID.fromString(JsonPath.read(resposta, "$.dados.id"));
    }

    @Dado("que já existe uma reserva para o horário {string}")
    public void existeReservaParaHorario(String dataHoraInicio) throws Exception {
        mockMvc.perform(post("/agendamento/reservar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequisicaoReserva(dataHoraInicio)));
    }

    @Dado("que existe um agendamento pendente para o horário {string}")
    public void existeAgendamentoPendente(String dataHoraInicio) throws Exception {
        String resposta = mockMvc.perform(post("/agendamento/reservar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequisicaoReserva(dataHoraInicio)))
                .andReturn().getResponse().getContentAsString();
        
        ultimoAgendamentoId = JsonPath.read(resposta, "$.dados.id");
    }

    @Quando("o cliente solicita o agendamento para {string}")
    public void solicitaAgendamento(String dataHoraInicio) throws Exception {
        ultimoResultado = mockMvc.perform(post("/agendamento/reservar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequisicaoReserva(dataHoraInicio)))
                .andReturn();

        if (ultimoResultado.getResponse().getStatus() == 201) {
            ultimoAgendamentoId = JsonPath.read(
                    ultimoResultado.getResponse().getContentAsString(), "$.dados.id");
        }
    }

    @Quando("o estabelecimento confirma o agendamento")
    public void confirmaAgendamento() throws Exception {
        ultimoResultado = mockMvc.perform(
                patch("/gerenciamento/agendamento/confirmar/{id}", ultimoAgendamentoId))
                .andReturn();
    }

    @Quando("o cliente cancela o agendamento")
    public void cancelaAgendamento() throws Exception {
        ultimoResultado = mockMvc.perform(
                patch("/gerenciamento/agendamento/cancelar/{id}", ultimoAgendamentoId))
                .andReturn();
    }

    @Então("o sistema retorna status {int}")
    public void verificaStatusHttp(int statusEsperado) {
        assertThat(ultimoResultado.getResponse().getStatus()).isEqualTo(statusEsperado);
    }

    @E("o agendamento é persistido com status {string}")
    public void agendamentoEstaComStatus(String statusEsperado) {
        assertThat(agendamentoRepository.count()).isEqualTo(1);
        assertThat(agendamentoRepository.findAll().getFirst().getStatus().name())
                .isEqualTo(statusEsperado);
    }

    @Então("o status do agendamento é {string}")
    public void verificaStatusDoAgendamento(String statusEsperado) {
        UUID id = UUID.fromString(ultimoAgendamentoId);
        assertThat(agendamentoRepository.findById(id).orElseThrow().getStatus().name())
                .isEqualTo(statusEsperado);
    }

    private String jsonRequisicaoReserva(String dataHoraInicio) {
        return String.format("""
                {
                  "clienteId": "%s",
                  "profissionalId": "%s",
                  "servicoId": "%s",
                  "dataHoraInicio": "%s"
                }
                """, clienteId, profissionalId, servicoId, dataHoraInicio);
    }
}
