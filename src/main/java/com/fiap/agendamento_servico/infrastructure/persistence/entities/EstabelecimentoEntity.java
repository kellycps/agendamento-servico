package com.fiap.agendamento_servico.infrastructure.persistence.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "estabelecimentos")
public class EstabelecimentoEntity {

    @Id
    @Column(name = "id_estabelecimento", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "nome_estabelecimento", nullable = false)
    private String nome;

    @Embedded
    private EnderecoEmbeddable endereco;

    @Column(name = "hora_inicio_funcionamento", nullable = false)
    private LocalTime horaInicioFuncionamento;

    @Column(name = "hora_fim_funcionamento", nullable = false)
    private LocalTime horaFimFuncionamento;

    @Column(name = "intervalo_minutos_padrao", nullable = false)
    private int intervaloMinutosPadrao;

    @Column(name = "nota_media")
    private double notaMedia;

    @Column(name = "url_foto_principal", length = 500)
    private String fotoPrincipalUrl;

    @ElementCollection
    @CollectionTable(name = "estabelecimento_galeria", joinColumns = @JoinColumn(name = "id_estabelecimento"))
    @Column(name = "url_foto", length = 500)
    private List<String> galeriaUrls = new ArrayList<>();

    @OneToMany(mappedBy = "estabelecimento", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ServicoEntity> servicos = new ArrayList<>();
    
    @OneToMany(mappedBy = "estabelecimento", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProfissionalEntity> profissionais = new ArrayList<>();
    
    public EstabelecimentoEntity() {}

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

    public EnderecoEmbeddable getEndereco() {
        return endereco;
    }

    public void setEndereco(EnderecoEmbeddable endereco) {
        this.endereco = endereco;
    }

    public LocalTime getHoraInicioFuncionamento() {
        return horaInicioFuncionamento;
    }

    public void setHoraInicioFuncionamento(LocalTime horaInicioFuncionamento) {
        this.horaInicioFuncionamento = horaInicioFuncionamento;
    }

    public LocalTime getHoraFimFuncionamento() {
        return horaFimFuncionamento;
    }

    public void setHoraFimFuncionamento(LocalTime horaFimFuncionamento) {
        this.horaFimFuncionamento = horaFimFuncionamento;
    }

    public int getIntervaloMinutosPadrao() {
        return intervaloMinutosPadrao;
    }

    public void setIntervaloMinutosPadrao(int intervaloMinutosPadrao) {
        this.intervaloMinutosPadrao = intervaloMinutosPadrao;
    }

    public double getNotaMedia() {
        return notaMedia;
    }

    public void setNotaMedia(double notaMedia) {
        this.notaMedia = notaMedia;
    }

    public String getFotoPrincipalUrl() {
        return fotoPrincipalUrl;
    }

    public void setFotoPrincipalUrl(String fotoPrincipalUrl) {
        this.fotoPrincipalUrl = fotoPrincipalUrl;
    }

    public List<String> getGaleriaUrls() {
        return galeriaUrls;
    }

    public void setGaleriaUrls(List<String> galeriaUrls) {
        this.galeriaUrls = galeriaUrls;
    }

    public List<ServicoEntity> getServicos() {
        return servicos;
    }

    public void setServicos(List<ServicoEntity> servicos) {
        this.servicos = servicos;
    }

    public List<ProfissionalEntity> getProfissionais() {
        return profissionais;
    }

    public void setProfissionais(List<ProfissionalEntity> profissionais) {
        this.profissionais = profissionais;
    }

    public void adicionarServico(ServicoEntity servico) {
        servicos.add(servico);
        servico.setEstabelecimento(this);
    }

    public void removerServico(ServicoEntity servico) {
        servicos.remove(servico);
        servico.setEstabelecimento(null);
    }
}