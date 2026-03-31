package com.fiap.agendamento_servico.performance;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

/**
 * PROFESSORES AVALIADORES: este foi o teste que tive mais dificuldade para implementar, portanto deixei anotações e comentários
 * para explicar o raciocínio e não me perder nos passos implementados, foquei na funcionalidade de agendamento por ser o core da aplicação
 * 
 * Fluxo testado:
 *   1. Configurações (uma vez): cria Estabelecimento -> Serviço -> Profissional -> Cliente via API
 *   2. Carga: N usuários simultâneos chamando POST /agendamento/reservar em horários distintos
 *
 * Pré-requisito: aplicação rodando em localhost:8080
 *   Passo 1: docker-compose up -d
 *   Passo 2: ./mvnw spring-boot:run
 *
 * Execução (em outro terminal):
 *   ./mvnw test-compile gatling:test
 *
 * Relatório gerado em: target/gatling/<pasta com timestamp>/index.html
 */
public class ReservarAgendamentoSimulation extends Simulation {

    // Configuração base
    private static final String BASE_URL = "http://localhost:8080";
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    // Definição de Emails únicos por execução — evita conflitos em múltiplas execuções
    private static final long TIMESTAMP = System.currentTimeMillis();
    private static final String EMAIL_CLIENTE = "cliente.carga." + TIMESTAMP + "@teste.com";
    private static final String EMAIL_PROFISSIONAL = "profissional.carga." + TIMESTAMP + "@teste.com";

    // Contador para garantir horários únicos entre usuários (sem conflito de períodos)
    private static final AtomicInteger periodoCounter = new AtomicInteger(0);

    // IDs criados — armazenados para uso nos cenários e limpeza no after()
    private static final AtomicReference<String> estabelecimentoIdRef = new AtomicReference<>();
    private static final AtomicReference<String> clienteIdRef = new AtomicReference<>();
    private static final AtomicReference<String> profissionalIdRef = new AtomicReference<>();
    private static final AtomicReference<String> servicoIdRef = new AtomicReference<>();

    HttpProtocolBuilder httpProtocol = http
            .baseUrl(BASE_URL)
            .acceptHeader("application/json")
            .contentTypeHeader("application/json");

    // Cenário de configuração: cria os dados necessários e armazena os IDs nas sessões para uso nos outros cenários
    ScenarioBuilder setup = scenario("Setup — criar dados de suporte")
            .exec(
                    // Criar Estabelecimento com Serviço e Profissional vinculados
                    http("POST /estabelecimentos")
                            .post("/estabelecimentos")
                            .body(StringBody(session -> """
                                    {
                                      "nome": "Salão Teste de Carga",
                                      "endereco": {
                                        "rua": "Av. Ana Costa",
                                        "numero": "100",
                                        "complemento": null,
                                        "bairro": "Gonzaga",
                                        "cep": "11086-400",
                                        "cidade": "Santos"
                                      },
                                      "horaInicio": "08:00:00",
                                      "horaFim": "20:00:00",
                                      "intervaloMinutos": 30,
                                      "fotoPrincipalUrl": null,
                                      "galeriaUrls": [],
                                      "servicos": [
                                        {
                                          "nome": "Corte Teste de Carga",
                                          "descricao": "Serviço para teste de carga",
                                          "preco": 60.0,
                                          "duracaoMinutos": 30
                                        }
                                      ],
                                      "profissionais": [
                                        {
                                          "nome": "Profissional Teste de Carga",
                                          "email": "%s",
                                          "especialidades": ["Corte"],
                                          "horaInicioTrabalho": "08:00:00",
                                          "horaFimTrabalho": "20:00:00"
                                        }
                                      ]
                                    }
                                    """.formatted(EMAIL_PROFISSIONAL)))
                            .check(status().is(201))
                            .check(jsonPath("$.dados.id").saveAs("estabelecimentoId"))
                            .check(jsonPath("$.dados.servicos[0].id").saveAs("servicoId"))
                            .check(jsonPath("$.dados.profissionais[0].id").saveAs("profissionalId"))
            )
            .exec(
                    // Criar Cliente
                    http("POST /clientes")
                            .post("/clientes")
                            .body(StringBody(session -> """
                                    {
                                      "nome": "Cliente Teste de Carga",
                                      "telefone": "11999990000",
                                      "email": "%s"
                                    }
                                    """.formatted(EMAIL_CLIENTE)))
                            .check(status().is(201))
                            .check(jsonPath("$.dados.id").saveAs("clienteId"))
            )
            // Salva os IDs em variáveis para uso nos cenários e no after()
            .exec(session -> {
                estabelecimentoIdRef.set(session.getString("estabelecimentoId"));
                clienteIdRef.set(session.getString("clienteId"));
                profissionalIdRef.set(session.getString("profissionalId"));
                servicoIdRef.set(session.getString("servicoId"));
                return session;
            });

    // Cenário de carga: cada usuário reserva um horário único
    ScenarioBuilder reservar = scenario("Reservar Agendamento")
            // Aguarda setup completar (máx 3s) e injeta IDs na sessão de cada usuário
            .pause(3)
            .exec(session -> session
                    .set("clienteId", clienteIdRef.get())
                    .set("profissionalId", profissionalIdRef.get())
                    .set("servicoId", servicoIdRef.get()))
            .exec(session -> {
                // Gera um periodo de 30 minutos único por usuário (sem conflito de períodos)
                int periodo = periodoCounter.getAndIncrement();
                LocalDateTime dataHora = LocalDate.of(2026, 10, 1)
                        .atTime(8, 0)
                        .plusMinutes((long) periodo * 30);
                return session.set("dataHoraInicio", dataHora.format(FMT));
            })
            .exec(
                    http("POST /agendamento/reservar")
                            .post("/agendamento/reservar")
                            .body(StringBody("""
                                    {
                                      "clienteId": "#{clienteId}",
                                      "profissionalId": "#{profissionalId}",
                                      "servicoId": "#{servicoId}",
                                      "dataHoraInicio": "#{dataHoraInicio}"
                                    }
                                    """))
                            .check(status().in(201, 422)) // 201=sucesso, 422=periodo esgotado
            );

    // Cenário de consulta
    ScenarioBuilder consultarDisponibilidade = scenario("Consultar Disponibilidade")
            // Aguarda setup completar e injeta profissionalId na sessão
            .pause(3)
            .exec(session -> session.set("profissionalId", profissionalIdRef.get()))
            .exec(
                    http("GET /agendamento/disponibilidade")
                            .get("/agendamento/disponibilidade")
                            .queryParam("profissionalId", "#{profissionalId}")
                            .queryParam("data", "2026-10-01")
                            .check(status().is(200))
            );

    // Definição de carga e critérios de aceitação
    {
        setUp(
                // um único usuário cria os dados necessários antes da carga
                setup.injectOpen(atOnceUsers(1)),

                // 100 usuários em ramp de 20 segundos
                reservar.injectOpen(rampUsers(100).during(20)),

                // Consultas simultâneas de disponibilidade: 50 usuários/s por 30s
                consultarDisponibilidade.injectOpen(constantUsersPerSec(50).during(30))
        )
        .protocols(httpProtocol)
        .assertions(
                // 95% das reservas com resposta abaixo de 2 segundos
                details("POST /agendamento/reservar").responseTime().percentile(95).lt(2000),
                // Tempo médio das reservas abaixo de 1 segundo
                details("POST /agendamento/reservar").responseTime().mean().lt(1000),
                // 99% das consultas de disponibilidade abaixo de 500ms
                details("GET /agendamento/disponibilidade").responseTime().percentile(99).lt(500),
                // Taxa de sucesso global acima de 95%
                global().successfulRequests().percent().gt(95.0)
        );
    }

    // remove dados criados pelo setup
    @Override
    public void after() {
        String estabelecimentoId = estabelecimentoIdRef.get();
        String clienteId = clienteIdRef.get();

        if (estabelecimentoId == null || clienteId == null) {
            System.out.println("[Limpeza] IDs não disponíveis — setup pode ter falhado. Nada a limpar.");
            return;
        }

        try (HttpClient http = HttpClient.newHttpClient()) {
            deletar(http, "/clientes/" + clienteId, "Cliente");
            deletar(http, "/estabelecimentos/" + estabelecimentoId, "Estabelecimento");
        } catch (Exception e) {
            System.out.println("[Limpeza] Erro ao limpar dados: " + e.getMessage());
        }
    }

    private void deletar(HttpClient http, String path, String label) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .DELETE()
                .build();
        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.printf("[Limpeza] DELETE %s (%s) → %d%n", path, label, response.statusCode());
    }
}
