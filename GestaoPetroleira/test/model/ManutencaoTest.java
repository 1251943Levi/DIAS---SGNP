package model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitarios da entidade Manutencao.
 * Regra: uma manutencao esta "em curso" enquanto nao tiver data de fim.
 */
class ManutencaoTest {

    private Navio navio() {
        return new Navio(1, "Atlantico I", "IMO9111111",
                new TipoNavio(1, "Crude", 300000, 1),
                280000, 1, "Portugal", 2015, EstadoOperacional.EM_MANUTENCAO, 1);
    }

    @Test
    void manutencaoSemDataFimEstaEmCurso() {
        Manutencao m = new Manutencao(1, navio(), LocalDate.of(2026, 1, 1), null, "Inspecao");
        assertTrue(m.emCurso());
    }

    @Test
    void manutencaoComDataFimNaoEstaEmCurso() {
        Manutencao m = new Manutencao(1, navio(),
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 2, 1), "Inspecao");
        assertFalse(m.emCurso());
    }

    @Test
    void concluirManutencaoMarcaDataFim() {
        Manutencao m = new Manutencao(1, navio(), LocalDate.of(2026, 1, 1), null, "Inspecao");
        assertTrue(m.emCurso());
        m.setDataFim(LocalDate.of(2026, 3, 1));
        assertFalse(m.emCurso());
        assertEquals(LocalDate.of(2026, 3, 1), m.getDataFim());
    }
}
