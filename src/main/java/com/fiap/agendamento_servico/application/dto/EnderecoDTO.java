package com.fiap.agendamento_servico.application.dto;

public record EnderecoDTO(
        String rua,
        String numero,
        String complemento,
        String bairro,
        String cep,
        String cidade
) {
}
