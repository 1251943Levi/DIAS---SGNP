package model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitarios da entidade Viagem.
 * Regra: uma viagem PLANEADA ou EM_CURSO ainda "ocupa" o navio (esta ativa).
 */
class ViagemTest {

    private Viagem viagemCom(EstadoViagem estado) {
        Porto origem = new Porto(1, "Leixoes", "Portugal", "PTLEI");
        Porto destino = new Porto(2, "Sines", "Portugal", "PTSIN");
        Navio navio = new Navio(1, "Atlantico I", "IMO9111111",
                new TipoNavio(1, "Crude", 300000, 1),
                280000, 1, "Portugal", 2015, EstadoOperacional.ATIVO, 1);
        return new Viagem(1, navio, origem, destino,
                LocalDate.of(2026, 6, 1), null, estado);
    }

    @Test
    void viagemPlaneadaEstaAtiva() {
        assertTrue(viagemCom(EstadoViagem.PLANEADA).estaAtiva());
    }

    @Test
    void viagemEmCursoEstaAtiva() {
        assertTrue(viagemCom(EstadoViagem.EM_CURSO).estaAtiva());
    }

    @Test
    void viagemConcluidaNaoEstaAtiva() {
        assertFalse(viagemCom(EstadoViagem.CONCLUIDA).estaAtiva());
    }

    @Test
    void viagemCanceladaNaoEstaAtiva() {
        assertFalse(viagemCom(EstadoViagem.CANCELADA).estaAtiva());
    }

    @Test
    void toStringContemNavioEPortos() {
        String s = viagemCom(EstadoViagem.PLANEADA).toString();
        assertTrue(s.contains("Atlantico I"));
        assertTrue(s.contains("PLANEADA"));
    }
}
