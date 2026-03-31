package com.fiap.agendamento_servico.domain.entities;

import java.util.UUID;
import java.util.regex.Pattern;

public record Cliente(
        UUID id,
        String nome,
        String telefone,
        String email
) {
    private static final Pattern PADRAO_EMAIL = Pattern.compile("^[\\w.+\\-]+@[\\w\\-]+\\.[\\w.]+$");

    public Cliente {
        if (nome == null || nome.isBlank())
            throw new IllegalArgumentException("Nome do cliente não pode ser nulo ou vazio");
        
        if (telefone == null || telefone.isBlank())
            throw new IllegalArgumentException("Telefone do cliente não pode ser nulo ou vazio");
        
        if (email == null || email.isBlank())
            throw new IllegalArgumentException("E-mail do cliente não pode ser nulo ou vazio");
        
        if (!PADRAO_EMAIL.matcher(email).matches())
            throw new IllegalArgumentException("E-mail do cliente inválido: " + email);
    }

    public static Cliente criar(String nome, String telefone, String email) {
        return new Cliente(UUID.randomUUID(), nome, telefone, email);
    }
}
