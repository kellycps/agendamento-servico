package com.fiap.agendamento_servico.domain.entities;

import com.fiap.agendamento_servico.domain.exceptions.BusinessException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

public class Profissional {

    private static final Pattern PADRAO_EMAIL = Pattern.compile("^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$");

    private final UUID id;
    private final String nome;
    private final String email;
    private final UUID estabelecimentoId;
    private final List<UUID> servicosIds;
    private final List<String> especialidades;
    private LocalTime horaInicioTrabalho;
    private LocalTime horaFimTrabalho;
    private double notaMedia;

    public Profissional(
            UUID id,
            String nome,
            UUID estabelecimentoId,
            List<UUID> servicosIds,
            List<String> especialidades,
            LocalTime horaInicioTrabalho,
            LocalTime horaFimTrabalho
    ) {
        this(id, nome, null, estabelecimentoId, servicosIds, especialidades, horaInicioTrabalho, horaFimTrabalho);
    }

    public Profissional(
            UUID id,
            String nome,
            String email,
            UUID estabelecimentoId,
            List<UUID> servicosIds,
            List<String> especialidades,
            LocalTime horaInicioTrabalho,
            LocalTime horaFimTrabalho) 
    {
        validarCampos(nome, estabelecimentoId);
        if (email != null) {
            validarEmail(email);
        }
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.estabelecimentoId = estabelecimentoId;
        this.servicosIds = servicosIds != null ? new ArrayList<>(servicosIds) : new ArrayList<>();
        this.especialidades = especialidades != null ? new ArrayList<>(especialidades) : new ArrayList<>();
        this.horaInicioTrabalho = horaInicioTrabalho;
        this.horaFimTrabalho = horaFimTrabalho;
        this.notaMedia = 0.0;
    }

    private static void validarCampos(String nome, UUID estabelecimentoId) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome do profissional não pode ser nulo ou vazio");
        }

        if (estabelecimentoId == null) {
            throw new IllegalArgumentException("ID do estabelecimento não pode ser nulo");
        }
    }

    private static void validarEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("E-mail do profissional não pode ser nulo ou vazio");
        }
        
        if (!PADRAO_EMAIL.matcher(email.trim()).matches()) {
            throw new IllegalArgumentException("E-mail inválido: " + email);
        }
    }

    public static Profissional criar(String nome, String email, UUID estabelecimentoId) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("E-mail do profissional é obrigatório");
        }
        
        return new Profissional(UUID.randomUUID(), nome, email.trim(), estabelecimentoId, new ArrayList<>(), new ArrayList<>(), null, null);
    }

    public static Profissional vincular(String nome, UUID estabelecimentoId) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome do profissional não pode ser nulo ou vazio");
        }
        
        return new Profissional(
                UUID.randomUUID(),
                nome.trim(),
                estabelecimentoId,
                new ArrayList<>(),
                new ArrayList<>(),
                null,
                null
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

    public UUID getEstabelecimentoId() {
        return estabelecimentoId;
    }

    public List<UUID> getServicosIds() {
        return new ArrayList<>(servicosIds);
    }

    public List<String> getEspecialidades() {
        return new ArrayList<>(especialidades);
    }

    public LocalTime getHoraInicioTrabalho() {
        return horaInicioTrabalho;
    }

    public LocalTime getHoraFimTrabalho() {
        return horaFimTrabalho;
    }

    public String getEmail() {
        return email;
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

    public void atualizarEspecialidades(List<String> novasEspecialidades) {
        this.especialidades.clear();

        if (novasEspecialidades != null) {
            this.especialidades.addAll(
                    novasEspecialidades.stream()
                            .filter(item -> item != null && !item.isBlank())
                            .map(String::trim)
                            .toList());
        }
    }

    public void definirHorariosTrabalho(LocalTime horaInicioTrabalho, LocalTime horaFimTrabalho) {
        if (horaInicioTrabalho == null || horaFimTrabalho == null) {
            throw new IllegalArgumentException("Horário de trabalho não pode ser nulo");
        }

        if (!horaInicioTrabalho.isBefore(horaFimTrabalho)) {
            throw new BusinessException("Hora de início da jornada deve ser anterior à hora de fim");
        }

        this.horaInicioTrabalho = horaInicioTrabalho;
        
        this.horaFimTrabalho = horaFimTrabalho;
    }

    @Override
    public String toString() {
        return String.format("Profissional{id=%s, nome='%s', estabelecimentoId=%s, totalServicos=%d}", id, nome, estabelecimentoId, servicosIds.size());
    }
}
