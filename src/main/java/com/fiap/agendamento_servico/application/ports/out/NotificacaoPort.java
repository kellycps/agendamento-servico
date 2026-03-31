package com.fiap.agendamento_servico.application.ports.out;

import com.fiap.agendamento_servico.domain.entities.Agendamento;
import com.fiap.agendamento_servico.domain.entities.Profissional;

public interface NotificacaoPort {

    void enviarConfirmacaoAgendamento(Agendamento agendamento, Profissional profissional,
            String nomeCliente, String emailCliente, String nomeServico, String nomeEstabelecimento);

    void enviarLembrete(Agendamento agendamento);
}
