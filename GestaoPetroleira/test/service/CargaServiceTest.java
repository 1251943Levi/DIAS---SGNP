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
}
