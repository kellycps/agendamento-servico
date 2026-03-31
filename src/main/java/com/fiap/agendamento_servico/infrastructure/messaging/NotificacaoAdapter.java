package com.fiap.agendamento_servico.infrastructure.messaging;

import com.fiap.agendamento_servico.application.ports.out.NotificacaoPort;
import com.fiap.agendamento_servico.domain.entities.Agendamento;
import com.fiap.agendamento_servico.domain.entities.Profissional;
import com.fiap.agendamento_servico.infrastructure.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class NotificacaoAdapter implements NotificacaoPort {

    private static final Logger log = LoggerFactory.getLogger(NotificacaoAdapter.class);

    private final RabbitTemplate rabbitTemplate;

    public NotificacaoAdapter(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void enviarConfirmacaoAgendamento(Agendamento agendamento, Profissional profissional, String nomeCliente, String emailCliente, String nomeServico, String nomeEstabelecimento) {
        NotificacaoAgendamentoEvento evento = new NotificacaoAgendamentoEvento(
                agendamento.id(),
                profissional.getNome(),
                profissional.getEmail(),
                nomeCliente,
                emailCliente,
                nomeServico,
                nomeEstabelecimento,
                agendamento.dataHoraInicio()
        );
        
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY_CONFIRMACAO, evento);
        
        log.info("[Notificação] Evento de confirmação publicado no RabbitMQ - agendamento id={} profissional={}", agendamento.id(), profissional.getNome());
    }

    @Override
    public void enviarLembrete(Agendamento agendamento) {
        log.info("[Notificação] Lembrete - id={} (ainda não implementado via fila)", agendamento.id());
    }
}

