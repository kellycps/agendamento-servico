package com.fiap.agendamento_servico.infrastructure.messaging;

import com.fiap.agendamento_servico.infrastructure.config.RabbitMQConfig;
import java.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class EmailNotificacaoConsumer {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificacaoConsumer.class);
    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm");
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String remetente;

    public EmailNotificacaoConsumer(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_EMAIL_CONFIRMACAO)
    public void receberConfirmacaoAgendamento(NotificacaoAgendamentoEvento evento) {
        if (remetente == null || remetente.isBlank()) {
            log.warn("[E-mail] GMAIL_USERNAME nao configurado - e-mail nao enviado para agendamento id={}", evento.agendamentoId());
            
            return;
        }

        if (evento.emailProfissional() == null || evento.emailProfissional().isBlank()) {
            log.warn("[E-mail] Profissional sem e-mail cadastrado - agendamento id={} profissional={}",evento.agendamentoId(), evento.nomeProfissional());
            
            return;
        }

        String data = evento.dataHoraInicio().format(FORMATO_DATA);
        String hora = evento.dataHoraInicio().format(FORMATO_HORA);

        SimpleMailMessage mensagem = new SimpleMailMessage();
        mensagem.setFrom(remetente);
        mensagem.setTo(evento.emailProfissional());
        mensagem.setSubject("Novo agendamento confirmado");
        mensagem.setText(String.format(
                "Ol\u00e1 %s,\n\nVoc\u00ea tem um agendamento para o servi\u00e7o %s marcado para o dia %s no hor\u00e1rio %s.\n\nAtt,\n%s",
                evento.nomeProfissional(), evento.nomeServico(), data, hora, evento.nomeEstabelecimento()
        ));

        try {
            mailSender.send(mensagem);

            log.info("[E-mail] Confirmação enviada para {} - agendamento id={}", evento.emailProfissional(), evento.agendamentoId());
        } catch (Exception e) {
            log.error("[E-mail] Falha ao enviar confirmacao para {} - agendamento id={}: {}", evento.emailProfissional(), evento.agendamentoId(), e.getMessage());
        }

        if (evento.emailCliente() == null || evento.emailCliente().isBlank()) {
            log.warn("[E-mail] E-mail do cliente não informado - agendamento id={}", evento.agendamentoId());
            
            return;
        }

        SimpleMailMessage mensagemCliente = new SimpleMailMessage();
        
        mensagemCliente.setFrom(remetente);
        mensagemCliente.setTo(evento.emailCliente());
        mensagemCliente.setSubject("Agendamento confirmado!");
        mensagemCliente.setText(String.format(
                "Ol\u00e1 %s,\n\nVoc\u00ea tem um agendamento para o servi\u00e7o %s marcado para o dia %s no hor\u00e1rio %s.\n\nAtt,\n%s",
                evento.nomeCliente(), evento.nomeServico(), data, hora, evento.nomeEstabelecimento()
        ));

        try {
            mailSender.send(mensagemCliente);
            log.info("[E-mail] Confirmação enviada para cliente {} - agendamento id={}",evento.emailCliente(), evento.agendamentoId());
        } catch (Exception e) {
            log.error("[E-mail] Falha ao enviar confirmacao para cliente {} - agendamento id={}: {}",evento.emailCliente(), evento.agendamentoId(), e.getMessage());
        }
    }
}
