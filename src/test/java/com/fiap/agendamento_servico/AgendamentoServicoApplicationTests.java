package com.fiap.agendamento_servico;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
class AgendamentoServicoApplicationTests {

	@MockitoBean
	ConnectionFactory connectionFactory;

	@Test
	void contextLoads() {}

}
