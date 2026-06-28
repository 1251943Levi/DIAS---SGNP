package service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitarios da regra do numero maximo de cargas por viagem.
 * Testa o metodo puro {@link CargaService#limiteCargasAtingido(int, int)},
 * que nao depende da base de dados.
 */
class CargaServiceTest {

    @Test
    void abaixoDoLimitePermite() {
        // tipo de navio permite 4 cargas, ja tem 2 -> ainda cabe
        assertFalse(CargaService.limiteCargasAtingido(4, 2));
    }

    @Test
    void noLimiteBloqueia() {
        // permite 1 carga (ex.: VLCC), ja tem 1 -> nao cabe mais
        assertTrue(CargaService.limiteCargasAtingido(1, 1));
    }

    @Test
    void acimaDoLimiteBloqueia() {
        assertTrue(CargaService.limiteCargasAtingido(2, 3));
    }

    @Test
    void viagemVaziaPermite() {
        assertFalse(CargaService.limiteCargasAtingido(1, 0));
    }

    @Test
    void semLimiteDefinidoPermiteSempre() {
        // maxCargas <= 0 significa "sem limite"
        assertFalse(CargaService.limiteCargasAtingido(0, 10));
    }

    // ── Regra do compartimento (tanque) ──────────────────────────────────────

    @Test
    void compartimentoDentroDoIntervaloEhValido() {
        // navio com 4 tanques -> 1 e 4 sao validos
        assertTrue(CargaService.compartimentoValido(1, 4));
        assertTrue(CargaService.compartimentoValido(4, 4));
    }

    @Test
    void compartimentoZeroOuNegativoEhInvalido() {
        assertFalse(CargaService.compartimentoValido(0, 4));
        assertFalse(CargaService.compartimentoValido(-1, 4));
    }

    @Test
    void compartimentoAcimaDoTotalEhInvalido() {
        // navio so tem 4 tanques -> o 5 nao existe
        assertFalse(CargaService.compartimentoValido(5, 4));
    }

    // ── Regra da capacidade ──────────────────────────────────────────────────

    @Test
    void cargaQueCabeNaoExcedeCapacidade() {
        // ocupado 100 t, nova 50 t, maximo 200 t -> cabe
        assertFalse(CargaService.capacidadeExcedida(100, 50, 200));
    }

    @Test
    void cargaExatamenteNoLimiteNaoExcede() {
        // 150 + 50 = 200, maximo 200 -> ainda cabe (nao ultrapassa)
        assertFalse(CargaService.capacidadeExcedida(150, 50, 200));
    }

    @Test
    void cargaQueUltrapassaExcedeCapacidade() {
        // 180 + 50 = 230 > 200 -> excede
        assertTrue(CargaService.capacidadeExcedida(180, 50, 200));
    }
}
