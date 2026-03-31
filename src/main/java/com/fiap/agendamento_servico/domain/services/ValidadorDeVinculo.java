package com.fiap.agendamento_servico.domain.services;

import com.fiap.agendamento_servico.domain.entities.Estabelecimento;
import com.fiap.agendamento_servico.domain.entities.Profissional;
import com.fiap.agendamento_servico.domain.exceptions.BusinessException;

public class ValidadorDeVinculo {

    public void validarProfissionalNoEstabelecimento(Profissional profissional, Estabelecimento estabelecimento) {
        if (profissional == null) {
            throw new IllegalArgumentException("Profissional não pode ser nulo");
        }

        if (estabelecimento == null) {
            throw new IllegalArgumentException("Estabelecimento não pode ser nulo");
        }

        if (!profissional.getEstabelecimentoId().equals(estabelecimento.getId())) {
            throw new BusinessException("Profissional não pertence ao estabelecimento informado");
        }
    }
}
