package com.fiap.agendamento_servico.domain.entities;

import java.util.UUID;

public record Servico(
        UUID id,
        String nome,
        String descricao,
        double preco,
        int duracaoMinutos,
        UUID estabelecimentoId
) {

    public Servico {
        validarCampos(nome, descricao, preco, duracaoMinutos);
    }

    private static void validarCampos(String nome, String descricao, double preco, int duracaoMinutos) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome do serviço não pode ser nulo ou vazio");
        }

        if (descricao == null || descricao.isBlank()) {
            throw new IllegalArgumentException("Descrição do serviço não pode ser nula ou vazia");
        }

        if (preco < 0) {
            throw new IllegalArgumentException("Preço do serviço não pode ser negativo");
        }

        if (duracaoMinutos <= 0) {
            throw new IllegalArgumentException("Duração em minutos deve ser maior que zero");
        }
    }

    public static Servico criar(String nome, String descricao, double preco, int duracaoMinutos, UUID estabelecimentoId) {
        return new Servico(UUID.randomUUID(), nome, descricao, preco, duracaoMinutos, estabelecimentoId);
    }

    @Override
    public String toString() {
        return String.format("Servico{id=%s, nome='%s', preco=R$ %.2f, duracao=%d min}", id, nome, preco, duracaoMinutos);
    }
}
