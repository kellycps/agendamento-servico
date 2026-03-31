package com.fiap.agendamento_servico.integration;

import com.fiap.agendamento_servico.application.ports.out.NotificacaoPort;
import com.fiap.agendamento_servico.domain.enums.StatusAgendamento;
import com.fiap.agendamento_servico.infrastructure.persistence.entities.AgendamentoEntity;
import com.fiap.agendamento_servico.infrastructure.persistence.entities.EnderecoEmbeddable;
import com.fiap.agendamento_servico.infrastructure.persistence.entities.EstabelecimentoEntity;
import com.fiap.agendamento_servico.infrastructure.persistence.entities.ProfissionalEntity;
import com.fiap.agendamento_servico.infrastructure.persistence.entities.ServicoEntity;
import com.fiap.agendamento_servico.infrastructure.persistence.repositories.JpaAgendamentoRepository;
import com.fiap.agendamento_servico.infrastructure.persistence.repositories.JpaClienteRepository;
import com.fiap.agendamento_servico.infrastructure.persistence.repositories.JpaEstabelecimentoRepository;
import com.fiap.agendamento_servico.infrastructure.persistence.repositories.JpaProfissionalRepository;
import com.fiap.agendamento_servico.infrastructure.persistence.repositories.JpaServicoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Integração: Fluxo de Agendamento")
class AgendamentoFluxoIT {

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

    @MockitoBean 
    private ConnectionFactory connectionFactory;

    @MockitoBean 
    private NotificacaoPort notificacaoPort;

    private UUID estabelecimentoId;
    private UUID profissionalId;
    private UUID servicoId;
    private UUID clienteId;

    @BeforeEach
    void prepararCenario() {
        agendamentoRepository.deleteAll();
        profissionalRepository.deleteAll();
        servicoRepository.deleteAll();
        clienteRepository.deleteAll();
        estabelecimentoRepository.deleteAll();

        estabelecimentoId = UUID.randomUUID();
        profissionalId = UUID.randomUUID();
        servicoId = UUID.randomUUID();

        EstabelecimentoEntity estab = new EstabelecimentoEntity();
        estab.setId(estabelecimentoId);
        estab.setNome("Salão TDD");
        estab.setEndereco(new EnderecoEmbeddable("Av Ana Costa", "100", null, "Gonzaga", "01001-000", "Santos"));
        estab.setHoraInicioFuncionamento(LocalTime.of(8, 0));
        estab.setHoraFimFuncionamento(LocalTime.of(20, 0));
        estab.setIntervaloMinutosPadrao(30);
        estabelecimentoRepository.save(estab);

        ServicoEntity servico = new ServicoEntity();
        servico.setId(servicoId);
        servico.setNome("Corte Feminino");
        servico.setDescricao("Corte e escova");
        servico.setPreco(80.0);
        servico.setDuracaoMinutos(30);
        servico.setEstabelecimento(estab);
        servicoRepository.save(servico);

        ProfissionalEntity profissional = new ProfissionalEntity();
        profissional.setId(profissionalId);
        profissional.setNome("Ana Silva");
        profissional.setEmail("ana@salao.com");
        profissional.setEstabelecimento(estab);
        profissional.setHoraInicioTrabalho(LocalTime.of(8, 0));
        profissional.setHoraFimTrabalho(LocalTime.of(18, 0));
        profissional.setServicosIds(List.of(servicoId));
        profissional.setEspecialidades(List.of("Coloração", "Corte"));
        profissionalRepository.save(profissional);

        String respostaCliente;

        try {
            respostaCliente = mockMvc.perform(post("/clientes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"nome":"Julia Alves","telefone":"11988880001","email":"julia@email.com"}
                                    """))
                    .andReturn().getResponse().getContentAsString();
            
                    clienteId = UUID.fromString(
                    com.jayway.jsonpath.JsonPath.read(respostaCliente, "$.dados.id"));
        } catch (Exception e) {
            throw new RuntimeException("Falha ao criar cliente no cenário de teste", e);
        }
    }

    @Test
    @DisplayName("deve reservar agendamento e persistir com status PENDENTE")
    void deveReservarAgendamentoEPersistirComStatusPendente() throws Exception {
        String corpo = String.format("""
                {
                  "clienteId": "%s",
                  "profissionalId": "%s",
                  "servicoId": "%s",
                  "dataHoraInicio": "2026-10-15T10:00:00"
                }
                """, clienteId, profissionalId, servicoId);

        mockMvc.perform(post("/agendamento/reservar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sucesso").value(true))
                .andExpect(jsonPath("$.dados.status").value("PENDENTE"));

        org.assertj.core.api.Assertions.assertThat(agendamentoRepository.count()).isEqualTo(1);
        org.assertj.core.api.Assertions.assertThat(agendamentoRepository.findAll().getFirst().getStatus()).isEqualTo(StatusAgendamento.PENDENTE);
    }

    @Test
    @DisplayName("deve reservar agendamento e confirmar — status deve ser CONFIRMADO")
    void deveReservarAgendamentoEConfirmar() throws Exception {
        String agId = reservar("2026-10-15T11:00:00");

        mockMvc.perform(patch("/gerenciamento/agendamento/confirmar/{id}", agId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true))
                .andExpect(jsonPath("$.dados.status").value("CONFIRMADO"));

        UUID uuid = UUID.fromString(agId);
        org.assertj.core.api.Assertions.assertThat(agendamentoRepository.findById(uuid).orElseThrow().getStatus()).isEqualTo(StatusAgendamento.CONFIRMADO);
    }

    @Test
    @DisplayName("deve reservar agendamento e cancelar — status deve ser CANCELADO")
    void deveReservarAgendamentoECancelar() throws Exception {
        String agId = reservar("2026-10-15T14:00:00");

        mockMvc.perform(patch("/gerenciamento/agendamento/cancelar/{id}", agId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dados.status").value("CANCELADO"));

        UUID uuid = UUID.fromString(agId);
        org.assertj.core.api.Assertions.assertThat(agendamentoRepository.findById(uuid).orElseThrow().getStatus()).isEqualTo(StatusAgendamento.CANCELADO);
    }

    @Test
    @DisplayName("deve reservar agendamento, confirmar e concluir — status deve ser CONCLUIDO")
    void deveReservarAgendamentoConfirmarEConcluir() throws Exception {
        String agId = reservar("2026-10-15T15:00:00");

        mockMvc.perform(patch("/gerenciamento/agendamento/confirmar/{id}", agId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dados.status").value("CONFIRMADO"));

        mockMvc.perform(patch("/gerenciamento/agendamento/concluir/{id}", agId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dados.status").value("CONCLUIDO"));

        UUID uuid = UUID.fromString(agId);
        org.assertj.core.api.Assertions.assertThat(agendamentoRepository.findById(uuid).orElseThrow().getStatus()).isEqualTo(StatusAgendamento.CONCLUIDO);
    }

    @Test
    @DisplayName("deve retornar 422 ao tentar reservar horário já ocupado pelo profissional")
    void deveRetornarErroAoReservarHorarioOcupado() throws Exception {
        reservar("2026-10-15T10:00:00");

        String corpo = String.format("""
                {
                  "clienteId": "%s",
                  "profissionalId": "%s",
                  "servicoId": "%s",
                  "dataHoraInicio": "2026-10-15T10:00:00"
                }
                """, clienteId, profissionalId, servicoId);

        mockMvc.perform(post("/agendamento/reservar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.sucesso").value(false));
    }

    @Test
    @DisplayName("deve listar agendamentos do cliente após reserva")
    void deveListarAgendamentosDoClienteAposReserva() throws Exception {
        reservar("2026-10-15T09:00:00");
        reservar("2026-10-15T16:00:00");

        mockMvc.perform(get("/gerenciamento/agendamento/cliente/{clienteId}", clienteId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true))
                .andExpect(jsonPath("$.dados.length()", is(2)));
    }

    @Test
    @DisplayName("deve deletar agendamento PENDENTE e retornar 204")
    void deveDeletarAgendamentoPendente() throws Exception {
        String agId = reservar("2026-10-15T17:00:00");

        mockMvc.perform(delete("/gerenciamento/agendamento/deletar/{id}", agId)).andExpect(status().isNoContent());

        org.assertj.core.api.Assertions.assertThat(agendamentoRepository.count()).isZero();
    }

    @Test
    @DisplayName("deve retornar 404 ao reservar com profissional inexistente")
    void deveRetornarErroAoReservarComProfissionalInexistente() throws Exception {
        String corpo = String.format("""
                {
                  "clienteId": "%s",
                  "profissionalId": "%s",
                  "servicoId": "%s",
                  "dataHoraInicio": "2026-10-15T10:00:00"
                }
                """, clienteId, UUID.randomUUID(), servicoId);

        mockMvc.perform(post("/agendamento/reservar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.sucesso").value(false));
    }

    @Test
    @DisplayName("deve reagendar agendamento PENDENTE para novo horário")
    void deveReagendarAgendamentoPendenteParaNovoHorario() throws Exception {
        String agId = reservar("2026-10-15T10:00:00");

        mockMvc.perform(patch("/gerenciamento/agendamento/reagendar/{id}", agId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"novaDataHoraInicio\":\"2026-10-15T16:00:00\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true));

        Optional<AgendamentoEntity> agendamento = agendamentoRepository.findById(UUID.fromString(agId));
        
        org.assertj.core.api.Assertions.assertThat(agendamento).isPresent();
        org.assertj.core.api.Assertions.assertThat(agendamento.get().getDataHoraInicio().getHour()).isEqualTo(16);
    }

    private String reservar(String dataHoraInicio) throws Exception {
        String corpo = String.format("""
                {
                  "clienteId": "%s",
                  "profissionalId": "%s",
                  "servicoId": "%s",
                  "dataHoraInicio": "%s"
                }
                """, clienteId, profissionalId, servicoId, dataHoraInicio);

        String resposta = mockMvc.perform(post("/agendamento/reservar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return com.jayway.jsonpath.JsonPath.read(resposta, "$.dados.id");
    }
}
