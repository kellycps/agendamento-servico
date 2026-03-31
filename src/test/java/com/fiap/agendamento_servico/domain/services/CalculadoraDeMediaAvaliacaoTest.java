package com.fiap.agendamento_servico.domain.services;

import com.fiap.agendamento_servico.domain.entities.Avaliacao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CalculadoraDeMediaAvaliacao")
class CalculadoraDeMediaAvaliacaoTest {

    private CalculadoraDeMediaAvaliacao calculadora;

    @BeforeEach
    void setUp() {
        calculadora = new CalculadoraDeMediaAvaliacao();
    }

    @Test
    @DisplayName("deve retornar 0.0 quando lista de avaliações é vazia")
    void deveRetornarZeroQuandoListaDeAvaliacoesForVazia() {
        assertEquals(0.0, calculadora.calcularNotaMedia(List.of()));
    }

    @Test
    @DisplayName("deve retornar 0.0 quando lista de avaliações é nula")
    void deveRetornarZeroQuandoListaDeAvaliacoesForNula() {
        assertEquals(0.0, calculadora.calcularNotaMedia(null));
    }

    @Test
    @DisplayName("deve retornar a nota quando há apenas uma avaliação")
    void deveRetornarNotaQuandoHaApenasUmaAvaliacao() {
        List<Avaliacao> avaliacoes = List.of(avaliacao(4));
        
        assertEquals(4.0, calculadora.calcularNotaMedia(avaliacoes));
    }

    @Test
    @DisplayName("deve calcular média corretamente com múltiplas avaliações")
    void deveCalcularMediaCorretamenteComMultiplasAvaliacoes() {
        List<Avaliacao> avaliacoes = List.of(avaliacao(5), avaliacao(3), avaliacao(4));

        assertEquals(4.0, calculadora.calcularNotaMedia(avaliacoes), 0.001);
    }

    @Test
    @DisplayName("deve calcular média que não é inteira")
    void deveCalcularMediaQueNaoEInteira() {
        List<Avaliacao> avaliacoes = List.of(avaliacao(5), avaliacao(4));

        assertEquals(4.5, calculadora.calcularNotaMedia(avaliacoes), 0.001);
    }

    private Avaliacao avaliacao(int nota) {
        return Avaliacao.criar(UUID.randomUUID(), nota, "comentário avaliação");
    }
}
