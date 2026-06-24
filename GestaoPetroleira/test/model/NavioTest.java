package model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitarios da entidade Navio.
 * Regra de negocio: so um navio ATIVO pode iniciar viagem.
 */
class NavioTest {

    private Navio navioCom(EstadoOperacional estado) {
        TipoNavio tipo = new TipoNavio(1, "Petroleiro Crude", 300000, 1);
        return new Navio(1, "Atlantico I", "IMO9111111", tipo,
                280000, 1, "Portugal", 2015, estado, 1);
    }

    @Test
    void navioAtivoPodeIniciarViagem() {
        assertTrue(navioCom(EstadoOperacional.ATIVO).podeIniciarViagem());
    }

    @Test
    void navioEmManutencaoNaoPodeIniciarViagem() {
        assertFalse(navioCom(EstadoOperacional.EM_MANUTENCAO).podeIniciarViagem());
    }

    @Test
    void navioInativoNaoPodeIniciarViagem() {
        assertFalse(navioCom(EstadoOperacional.INATIVO).podeIniciarViagem());
    }

    @Test
    void gettersDevolvemValoresDoConstrutor() {
        Navio n = navioCom(EstadoOperacional.ATIVO);
        assertEquals("Atlantico I", n.getNome());
        assertEquals("IMO9111111", n.getCodigoImo());
        assertEquals(280000, n.getCapacidadeMaxima());
        assertEquals(2015, n.getAnoFabrico());
        assertEquals(EstadoOperacional.ATIVO, n.getEstadoOperacional());
    }

    @Test
    void toStringContemNomeEImo() {
        String s = navioCom(EstadoOperacional.ATIVO).toString();
        assertTrue(s.contains("Atlantico I"));
        assertTrue(s.contains("IMO9111111"));
    }
}
