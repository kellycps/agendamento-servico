package com.fiap.agendamento_servico.domain.entities;

import com.fiap.agendamento_servico.domain.valueobjects.Endereco;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Estabelecimento {
    private final UUID id;
    private final String nome;
    private final Endereco endereco;
    private final LocalTime horaInicioFuncionamento;
    private final LocalTime horaFimFuncionamento;
    private final int intervaloMinutosPadrao;
    private final List<UUID> servicosIds;
    private double notaMedia;
    private String fotoPrincipalUrl;
    private List<String> galeriaUrls = new ArrayList<>();

    public Estabelecimento(
            UUID id,
            String nome,
            Endereco endereco,
            LocalTime horaInicioFuncionamento,
            LocalTime horaFimFuncionamento,
            int intervaloMinutosPadrao,
            List<UUID> servicosIds) 
    {
        if (id == null) {
            throw new IllegalArgumentException("Id do estabelecimento não pode ser nulo");
        }
        
        validarCampos(nome, endereco, horaInicioFuncionamento, horaFimFuncionamento, intervaloMinutosPadrao);
        
        this.id = id;
        this.nome = nome;
        this.endereco = endereco;
        this.horaInicioFuncionamento = horaInicioFuncionamento;
        this.horaFimFuncionamento = horaFimFuncionamento;
        this.intervaloMinutosPadrao = intervaloMinutosPadrao;
        this.servicosIds = servicosIds != null ? new ArrayList<>(servicosIds) : new ArrayList<>();
        this.notaMedia = 0.0;
    }

    private static void validarCampos(
            String nome,
            Endereco endereco,
            LocalTime horaInicioFuncionamento,
            LocalTime horaFimFuncionamento,
            int intervaloMinutosPadrao) 
    {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome do estabelecimento não pode ser nulo ou vazio");
        }

        if (endereco == null) {
            throw new IllegalArgumentException("Endereço do estabelecimento não pode ser nulo");
        }

        if (horaInicioFuncionamento == null) {
            throw new IllegalArgumentException("Hora de início de funcionamento não pode ser nula");
        }

        if (horaFimFuncionamento == null) {
            throw new IllegalArgumentException("Hora de fim de funcionamento não pode ser nula");
        }

        if (!horaInicioFuncionamento.isBefore(horaFimFuncionamento)) {
            throw new IllegalArgumentException("Hora de início deve ser anterior à hora de fim");
        }

        if (intervaloMinutosPadrao <= 0) {
            throw new IllegalArgumentException("Intervalo padrão em minutos deve ser positivo");
        }
    }

    public static Estabelecimento criar(
        String nome,
        Endereco endereco,
        LocalTime horaInicioFuncionamento,
        LocalTime horaFimFuncionamento,
        int intervaloMinutosPadrao) 
    {
        return new Estabelecimento(
            UUID.randomUUID(),
            nome,
            endereco,
            horaInicioFuncionamento,
            horaFimFuncionamento,
            intervaloMinutosPadrao,
            new ArrayList<>()
        );
    }

    public void adicionarServico(UUID servicoId) {
        if (servicoId != null && !servicosIds.contains(servicoId)) {
            servicosIds.add(servicoId);
        }
    }

    public void removerServico(UUID servicoId) {
        servicosIds.remove(servicoId);
    }

    public UUID getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public Endereco getEndereco() {
        return endereco;
    }

    public LocalTime getHoraInicioFuncionamento() {
        return horaInicioFuncionamento;
    }

    public LocalTime getHoraFimFuncionamento() {
        return horaFimFuncionamento;
    }

    public int getIntervaloMinutosPadrao() {
        return intervaloMinutosPadrao;
    }

    public List<UUID> getServicosIds() {
        return new ArrayList<>(servicosIds);
    }

    public double getNotaMedia() {
        return notaMedia;
    }

    public void atualizarNotaMedia(double notaMedia) {
        if (notaMedia < 0 || notaMedia > 5) {
            throw new IllegalArgumentException("Nota média deve estar entre 0 e 5");
        }

        this.notaMedia = notaMedia;
    }

    public String getFotoPrincipalUrl() {
        return fotoPrincipalUrl;
    }

    public List<String> getGaleriaUrls() {
        return new ArrayList<>(galeriaUrls);
    }

    public void atualizarFotos(String fotoPrincipalUrl, List<String> galeriaUrls) {
        this.fotoPrincipalUrl = fotoPrincipalUrl;
        this.galeriaUrls = galeriaUrls != null ? new ArrayList<>(galeriaUrls) : new ArrayList<>();
    }

    @Override
    public String toString() {
        return String.format(
                "Estabelecimento{id=%s, nome='%s', endereco=%s, horaInicio=%s, horaFim=%s, intervaloMinutosPadrao=%d, totalServicos=%d}",
                id,
                nome,
                endereco,
                horaInicioFuncionamento,
                horaFimFuncionamento,
                intervaloMinutosPadrao,
                servicosIds.size()
        );
    }
}
