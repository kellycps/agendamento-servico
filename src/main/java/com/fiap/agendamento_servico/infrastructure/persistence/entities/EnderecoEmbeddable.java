package com.fiap.agendamento_servico.infrastructure.persistence.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class EnderecoEmbeddable {

    @Column(name = "rua_endereco", nullable = false)
    private String rua;

    @Column(name = "numero_endereco", nullable = false)
    private String numero;

    @Column(name = "complemento_endereco")
    private String complemento;

    @Column(name = "bairro_endereco", nullable = false)
    private String bairro;

    @Column(name = "cep_endereco", nullable = false)
    private String cep;

    @Column(name = "cidade_endereco", nullable = false)
    private String cidade;

    protected EnderecoEmbeddable() {}

    public EnderecoEmbeddable(
            String rua,
            String numero,
            String complemento,
            String bairro,
            String cep,
            String cidade
    ) {
        this.rua = rua;
        this.numero = numero;
        this.complemento = complemento;
        this.bairro = bairro;
        this.cep = cep;
        this.cidade = cidade;
    }

    public String getRua() {
        return rua;
    }

    public void setRua(String rua) {
        this.rua = rua;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getComplemento() {
        return complemento;
    }

    public void setComplemento(String complemento) {
        this.complemento = complemento;
    }

    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public String getCep() {
        return cep;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }
}