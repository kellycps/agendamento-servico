package com.fiap.agendamento_servico.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfiguracaoSwagger {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Agendamentos de Serviços")
                        .version("1.0.0")
                        .description("Sistema responsável por gerenciar estabelecimentos, profissionais e grades de horários para agendamento de serviços."));
    }
}
