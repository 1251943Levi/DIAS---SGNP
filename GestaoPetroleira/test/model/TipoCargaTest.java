package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitarios de TipoCarga, com foco no resumo de propriedades
 * (inflamavel / corrosiva / toxica) exigido pelo enunciado.
 */
class TipoCargaTest {

    @Test
    void propriedadesApenasInflamavel() {
        TipoCarga tc = new TipoCarga(1, "Gasolina", true, false, false);
        assertEquals("Inflamável", tc.getPropriedades());
    }

    @Test
    void propriedadesTodas() {
        TipoCarga tc = new TipoCarga(1, "Quimico", true, true, true);
        assertEquals("Inflamável Corrosiva Tóxica", tc.getPropriedades());
    }

    @Test
    void propriedadesNenhuma() {
        TipoCarga tc = new TipoCarga(1, "Inerte", false, false, false);
        assertEquals("—", tc.getPropriedades());
    }

    @Test
    void propriedadesApenasCorrosiva() {
        TipoCarga tc = new TipoCarga(1, "Acido", false, true, false);
        assertEquals("Corrosiva", tc.getPropriedades());
    }

    @Test
    void flagsRefletemConstrutor() {
        TipoCarga tc = new TipoCarga(1, "Quimico", true, false, true);
        assertTrue(tc.isInflamavel());
        assertFalse(tc.isCorrosiva());
        assertTrue(tc.isToxica());
    }
}
