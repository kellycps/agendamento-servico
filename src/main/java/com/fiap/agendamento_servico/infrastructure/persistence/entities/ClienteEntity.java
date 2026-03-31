package com.fiap.agendamento_servico.infrastructure.persistence.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "clientes")
public class ClienteEntity {
    
    @Id
    @Column(name = "id_cliente", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "nome_cliente", nullable = false)
    private String nome;

    @Column(name = "telefone_cliente", nullable = false)
    private String telefone;

    @Column(name = "email_cliente", nullable = false)
    private String email;

    public ClienteEntity() {
    }

    public UUID getId() { 
        return id; 
    }
    
    public void setId(UUID id) { 
        this.id = id; 
    }

    public String getNome() { 
        return nome; 
    }
    
    public void setNome(String nome) { 
        this.nome = nome; 
    }

    public String getTelefone() { 
        return telefone; 
    }
    
    public void setTelefone(String telefone) { 
        this.telefone = telefone; 
    }

    public String getEmail() { 
        return email; 
    }
    
    public void setEmail(String email) { 
        this.email = email; 
    }
}
