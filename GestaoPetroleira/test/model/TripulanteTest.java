package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitarios da entidade Tripulante.
 */
class TripulanteTest {

    @Test
    void gettersDevolvemValoresDoConstrutor() {
        Tripulante t = new Tripulante(1, "Joao Martins", "MAT1001", Funcao.CAPITAO, true);
        assertEquals("Joao Martins", t.getNome());
        assertEquals("MAT1001", t.getNumeroMatricula());
        assertEquals(Funcao.CAPITAO, t.getFuncao());
        assertTrue(t.isDisponivel());
    }

    @Test
    void disponibilidadeAlteravel() {
        Tripulante t = new Tripulante(1, "Ana Sousa", "MAT1002", Funcao.OFICIAL, true);
        t.setDisponivel(false);
        assertFalse(t.isDisponivel());
    }

    @Test
    void toStringContemNomeEFuncao() {
        Tripulante t = new Tripulante(1, "Ana Sousa", "MAT1002", Funcao.OFICIAL, true);
        String s = t.toString();
        assertTrue(s.contains("Ana Sousa"));
        assertTrue(s.contains("Oficial"));
    }
}
