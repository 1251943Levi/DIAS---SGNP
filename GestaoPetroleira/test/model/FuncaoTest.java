package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitarios do enum Funcao (rotulos legiveis usados na interface).
 */
class FuncaoTest {

    @Test
    void rotulosLegiveis() {
        assertEquals("Capitão", Funcao.CAPITAO.toString());
        assertEquals("Oficial", Funcao.OFICIAL.toString());
        assertEquals("Engenheiro", Funcao.ENGENHEIRO.toString());
        assertEquals("Operador", Funcao.OPERADOR.toString());
    }

    @Test
    void valueOfReconheceConstantes() {
        assertEquals(Funcao.CAPITAO, Funcao.valueOf("CAPITAO"));
        assertEquals(4, Funcao.values().length);
    }
}
