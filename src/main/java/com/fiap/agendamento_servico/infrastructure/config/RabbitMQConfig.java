package com.fiap.agendamento_servico.infrastructure.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "agendamento.exchange";
    public static final String QUEUE_EMAIL_CONFIRMACAO = "agendamento.email.confirmacao";
    public static final String ROUTING_KEY_CONFIRMACAO = "agendamento.confirmacao";

    @Bean
    public TopicExchange agendamentoExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue filaEmailConfirmacao() {
        return new Queue(QUEUE_EMAIL_CONFIRMACAO, true);
    }

    @Bean
    public Binding bindingConfirmacao(Queue filaEmailConfirmacao, TopicExchange agendamentoExchange) {
        return BindingBuilder.bind(filaEmailConfirmacao)
                .to(agendamentoExchange)
                .with(ROUTING_KEY_CONFIRMACAO);
    }

    @Bean
    public MessageConverter messageConverter() {
        JacksonJsonMessageConverter converter = new JacksonJsonMessageConverter();
        converter.setAlwaysConvertToInferredType(true);
        return converter;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }
}
