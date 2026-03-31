package com.fiap.agendamento_servico.infrastructure.config;

import com.fiap.agendamento_servico.domain.services.CalculadoraDeMediaAvaliacao;
import com.fiap.agendamento_servico.domain.services.GeradorDeAgenda;
import com.fiap.agendamento_servico.domain.services.ValidadorAgendamento;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfiguracaoServicosDominio {

    @Bean
    public GeradorDeAgenda geradorDeAgenda() {
        return new GeradorDeAgenda();
    }

    @Bean
    public ValidadorAgendamento validadorAgendamento() {
        return new ValidadorAgendamento();
    }

    @Bean
    public CalculadoraDeMediaAvaliacao calculadoraDeMediaAvaliacao() {
        return new CalculadoraDeMediaAvaliacao();
    }
}
