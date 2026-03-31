package com.fiap.agendamento_servico.infrastructure.persistence.entities;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "profissionais")
public class ProfissionalEntity {

    @Id
    @Column(name = "id_profissional", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "nome_profissional", nullable = false)
    private String nome;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_estabelecimento", nullable = false)
    private EstabelecimentoEntity estabelecimento;

    @ElementCollection
    @CollectionTable(name = "profissional_especialidades", joinColumns = @JoinColumn(name = "id_profissional"))
    @Column(name = "especialidade")
    private List<String> especialidades = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "profissional_servicos", joinColumns = @JoinColumn(name = "id_profissional"))
    @Column(name = "id_servico")
    private List<UUID> servicosIds = new ArrayList<>();

    @Column(name = "hora_inicio_trabalho")
    private LocalTime horaInicioTrabalho;

    @Column(name = "hora_fim_trabalho")
    private LocalTime horaFimTrabalho;

    @Column(name = "nota_media")
    private double notaMedia;

    @Column(name = "email_profissional")
    private String email;

    public ProfissionalEntity() {}

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

    public EstabelecimentoEntity getEstabelecimento() {
        return estabelecimento;
    }

    public void setEstabelecimento(EstabelecimentoEntity estabelecimento) {
        this.estabelecimento = estabelecimento;
    }

    public List<String> getEspecialidades() {
        return especialidades;
    }

    public void setEspecialidades(List<String> especialidades) {
        this.especialidades = especialidades;
    }

    public List<UUID> getServicosIds() {
        return servicosIds;
    }

    public void setServicosIds(List<UUID> servicosIds) {
        this.servicosIds = servicosIds;
    }

    public LocalTime getHoraInicioTrabalho() {
        return horaInicioTrabalho;
    }

    public void setHoraInicioTrabalho(LocalTime horaInicioTrabalho) {
        this.horaInicioTrabalho = horaInicioTrabalho;
    }

    public LocalTime getHoraFimTrabalho() {
        return horaFimTrabalho;
    }

    public void setHoraFimTrabalho(LocalTime horaFimTrabalho) {
        this.horaFimTrabalho = horaFimTrabalho;
    }

    public double getNotaMedia() {
        return notaMedia;
    }

    public void setNotaMedia(double notaMedia) {
        this.notaMedia = notaMedia;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}