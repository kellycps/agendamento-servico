package com.fiap.agendamento_servico.bdd;

import com.fiap.agendamento_servico.application.ports.out.NotificacaoPort;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@CucumberContextConfiguration
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class CucumberSpringConfig {

    @MockitoBean
    ConnectionFactory connectionFactory;

    @MockitoBean
    NotificacaoPort notificacaoPort;
}
