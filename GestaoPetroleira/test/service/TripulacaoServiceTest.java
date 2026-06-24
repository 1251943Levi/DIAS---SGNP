package service;

import model.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitarios das regras de alocacao de tripulacao.
 *
 * NOTA: testa-se o caminho de validacao que lanca excecao ANTES de aceder a
 * base de dados (tripulante indisponivel). A alocacao bem-sucedida e os
 * caminhos que consultam a BD sao cobertos por testes de integracao.
 */
class TripulacaoServiceTest {

    private final TripulacaoService service = new TripulacaoService();

    private Viagem viagem() {
        Navio navio = new Navio(1, "Atlantico I", "IMO9111111",
                new TipoNavio(1, "Crude", 300000, 1),
                280000, 1, "Portugal", 2015, EstadoOperacional.ATIVO, 1);
        return new Viagem(1, navio,
                new Porto(1, "Leixoes", "Portugal", "PTLEI"),
                new Porto(2, "Sines", "Portugal", "PTSIN"),
                LocalDate.of(2026, 6, 1), null, EstadoViagem.PLANEADA);
    }

    @Test
    void associarTripulanteIndisponivelFalha() {
        Tripulante indisponivel =
                new Tripulante(1, "Joao Martins", "MAT1001", Funcao.CAPITAO, false);
        Exception e = assertThrows(Exception.class,
                () -> service.associarTripulanteAViagem(viagem(), indisponivel, Funcao.CAPITAO));
        assertTrue(e.getMessage().contains("disponível"));
    }
}
