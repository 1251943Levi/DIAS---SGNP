package service;

import model.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitarios das regras de negocio de ViagemService.
 *
 * NOTA: testam-se apenas os caminhos de validacao que lancam excecao ANTES
 * de qualquer acesso a base de dados (validacao de input e matriz de
 * transicoes de estado). Os caminhos "felizes" (que persistem) sao testados
 * por testes de integracao, fora do ambito destes testes unitarios.
 */
class ViagemServiceTest {

    private final ViagemService service = new ViagemService();

    private Navio navio(EstadoOperacional estado) {
        return new Navio(1, "Atlantico I", "IMO9111111",
                new TipoNavio(1, "Crude", 300000, 1),
                280000, 1, "Portugal", 2015, estado, 1);
    }

    private Porto porto(int id) {
        return new Porto(id, "Porto " + id, "Portugal", "PT" + id);
    }

    private Viagem viagem(EstadoViagem estado, EstadoOperacional estadoNavio) {
        return new Viagem(1, navio(estadoNavio), porto(1), porto(2),
                LocalDate.of(2026, 6, 1), null, estado);
    }

    // ── Validacao de input em criarViagem ────────────────────────────────────

    @Test
    void criarViagemSemNavioFalha() {
        Exception e = assertThrows(Exception.class,
                () -> service.criarViagem(null, porto(1), porto(2), LocalDate.now()));
        assertTrue(e.getMessage().contains("navio"));
    }

    @Test
    void criarViagemComMesmoPortoFalha() {
        Exception e = assertThrows(Exception.class,
                () -> service.criarViagem(navio(EstadoOperacional.ATIVO),
                        porto(1), porto(1), LocalDate.now()));
        assertTrue(e.getMessage().contains("diferentes"));
    }

    @Test
    void criarViagemSemDataFalha() {
        Exception e = assertThrows(Exception.class,
                () -> service.criarViagem(navio(EstadoOperacional.ATIVO),
                        porto(1), porto(2), null));
        assertTrue(e.getMessage().contains("data"));
    }

    @Test
    void criarViagemComChegadaAntesDaPartidaFalha() {
        Exception e = assertThrows(Exception.class,
                () -> service.criarViagem(navio(EstadoOperacional.ATIVO), porto(1), porto(2),
                        LocalDate.of(2026, 6, 10), LocalDate.of(2026, 6, 5)));
        assertTrue(e.getMessage().contains("anterior"));
    }

    // ── Matriz de transicoes de estado ───────────────────────────────────────

    @Test
    void iniciarViagemConcluidaEhTransicaoInvalida() {
        Exception e = assertThrows(Exception.class,
                () -> service.iniciarViagem(viagem(EstadoViagem.CONCLUIDA, EstadoOperacional.ATIVO)));
        assertTrue(e.getMessage().contains("invalida"));
    }

    @Test
    void iniciarViagemComNavioEmManutencaoFalha() {
        // Transicao PLANEADA->EM_CURSO e valida, mas o navio nao esta ATIVO.
        Exception e = assertThrows(Exception.class,
                () -> service.iniciarViagem(viagem(EstadoViagem.PLANEADA, EstadoOperacional.EM_MANUTENCAO)));
        assertTrue(e.getMessage().contains("ATIVO"));
    }

    @Test
    void cancelarViagemConcluidaEhTransicaoInvalida() {
        Exception e = assertThrows(Exception.class,
                () -> service.cancelarViagem(viagem(EstadoViagem.CONCLUIDA, EstadoOperacional.ATIVO)));
        assertTrue(e.getMessage().contains("invalida"));
    }

    @Test
    void cancelarViagemJaCanceladaEhTransicaoInvalida() {
        Exception e = assertThrows(Exception.class,
                () -> service.cancelarViagem(viagem(EstadoViagem.CANCELADA, EstadoOperacional.ATIVO)));
        assertTrue(e.getMessage().contains("invalida"));
    }

    @Test
    void concluirViagemPlaneadaEhTransicaoInvalida() {
        // So EM_CURSO pode passar a CONCLUIDA.
        Exception e = assertThrows(Exception.class,
                () -> service.concluirViagem(viagem(EstadoViagem.PLANEADA, EstadoOperacional.ATIVO)));
        assertTrue(e.getMessage().contains("invalida"));
    }
}
