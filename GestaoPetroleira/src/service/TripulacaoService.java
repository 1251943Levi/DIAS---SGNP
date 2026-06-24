package service;

import dao.TripulacaoViagemDAO;
import dao.TripulanteDAO;
import model.*;
import java.util.List;

public class TripulacaoService {
    private final TripulanteDAO tripulanteDAO = new TripulanteDAO();
    private final TripulacaoViagemDAO tripulacaoViagemDAO = new TripulacaoViagemDAO();

    // ── TRIPULANTE CRUD ───────────────────────────────────────────────────────
    public List<Tripulante> listarTripulantes() { return tripulanteDAO.listarTodos(); }

    public List<Tripulante> listarTripulantesDisponiveis() { return tripulanteDAO.listarDisponiveis(); }

    public List<Tripulante> listarTripulantesDaViagem(int idViagem) {
        return tripulanteDAO.listarPorViagem(idViagem);
    }

    public void adicionarTripulante(Tripulante t) { tripulanteDAO.inserir(t); }

    public void atualizarTripulante(Tripulante t) { tripulanteDAO.atualizar(t); }

    public void eliminarTripulante(int id) { tripulanteDAO.eliminar(id); }

    // ── ALOCAÇÃO ──────────────────────────────────────────────────────────────

    /**
     * Regra 1: Tripulante deve estar disponível.
     * Regra 2: Tripulante não pode estar já alocado à mesma viagem.
     * Regra 3: Capitão obrigatório (valida ao iniciar viagem, não aqui).
     */
    public void associarTripulanteAViagem(Viagem viagem, Tripulante tripulante, Funcao funcao) throws Exception {
        if (!tripulante.isDisponivel())
            throw new Exception("O tripulante '" + tripulante.getNome() + "' não está disponível.");

        if (tripulacaoViagemDAO.jaAlocado(viagem.getId(), tripulante.getId()))
            throw new Exception("O tripulante '" + tripulante.getNome() + "' já está alocado a esta viagem.");

        TripulacaoViagem tv = new TripulacaoViagem(0, viagem, tripulante, funcao);
        tripulacaoViagemDAO.inserir(tv);

        // Marcar como indisponível
        tripulante.setDisponivel(false);
        tripulanteDAO.atualizar(tripulante);
    }

    public void desassociarTripulante(int idViagem, Tripulante tripulante) {
        tripulacaoViagemDAO.eliminar(idViagem, tripulante.getId());
        tripulante.setDisponivel(true);
        tripulanteDAO.atualizar(tripulante);
    }

    public List<TripulacaoViagem> listarTripulacaoDaViagem(int idViagem) {
        return tripulacaoViagemDAO.listarPorViagem(idViagem);
    }

    /** Verifica se a viagem tem pelo menos um capitão */
    public boolean temCapitao(int idViagem) {
        return listarTripulacaoDaViagem(idViagem).stream()
                .anyMatch(tv -> tv.getFuncao() == Funcao.CAPITAO);
    }

    /** Historico de participacao em viagens de um tripulante. */
    public List<TripulacaoViagem> historicoDoTripulante(int idTripulante) {
        return tripulacaoViagemDAO.listarPorTripulante(idTripulante);
    }
}
