package com.fiap.agendamento_servico.domain.services;

import com.fiap.agendamento_servico.domain.entities.Avaliacao;
import com.fiap.agendamento_servico.domain.entities.Estabelecimento;
import com.fiap.agendamento_servico.domain.entities.Profissional;
import java.util.Collections;
import java.util.List;

public class CalculadoraDeMediaAvaliacao {

    public double calcularNotaMedia(List<Avaliacao> avaliacoes) {
        List<Avaliacao> lista = avaliacoes == null ? Collections.emptyList() : avaliacoes;

        if (lista.isEmpty()) {
            return 0.0;
        }

        double somaNotas = lista.stream()
                .mapToInt(Avaliacao::nota)
                .sum();

        return somaNotas / lista.size();
    }

    public void atualizarNotaMedia(Estabelecimento estabelecimento, List<Avaliacao> avaliacoes) {
        if (estabelecimento == null) {
            throw new IllegalArgumentException("Estabelecimento não pode ser nulo");
        }

        estabelecimento.atualizarNotaMedia(calcularNotaMedia(avaliacoes));
    }

    public void atualizarNotaMedia(Profissional profissional, List<Avaliacao> avaliacoes) {
        if (profissional == null) {
            throw new IllegalArgumentException("Profissional não pode ser nulo");
        }

        profissional.atualizarNotaMedia(calcularNotaMedia(avaliacoes));
    }
}
