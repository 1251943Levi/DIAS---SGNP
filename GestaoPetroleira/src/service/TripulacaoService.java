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

    public void atualizarTripulante(Tripulante t) throws Exception {
        Tripulante atual = tripulanteDAO.buscarPorId(t.getId());
        // Só permite alterar a função se o tripulante NÃO estiver numa viagem ativa (planeada/em curso).
        if (atual != null && atual.getFuncao() != t.getFuncao() && estaEmViagemAtiva(t.getId()))
            throw new Exception("Não é possível alterar a função: o tripulante está a ser utilizado numa viagem ativa. "
                    + "Conclua ou cancele a viagem primeiro.");
        tripulanteDAO.atualizar(t);
    }

    /** Um tripulante está "em viagem ativa" se participa numa viagem PLANEADA ou EM_CURSO. */
    private boolean estaEmViagemAtiva(int idTripulante) {
        for (TripulacaoViagem tv : tripulacaoViagemDAO.listarPorTripulante(idTripulante)) {
            EstadoViagem e = tv.getViagem().getEstado();
            if (e == EstadoViagem.PLANEADA || e == EstadoViagem.EM_CURSO) return true;
        }
        return false;
    }

    /** Liberta (torna disponíveis) os tripulantes de uma viagem — usado ao concluir/cancelar. */
    public void libertarTripulacao(int idViagem) {
        for (TripulacaoViagem tv : tripulacaoViagemDAO.listarPorViagem(idViagem)) {
            Tripulante t = tv.getTripulante();
            t.setDisponivel(true);
            tripulanteDAO.atualizar(t);
        }
    }

    public void eliminarTripulante(int id) throws Exception {
        // Não permitir eliminar um tripulante com histórico de participação em viagens
        // (a FK em TRIPULACAO_VIAGEM impediria o DELETE; aqui damos uma mensagem clara).
        if (!tripulacaoViagemDAO.listarPorTripulante(id).isEmpty())
            throw new Exception("Não é possível eliminar: o tripulante tem histórico de participação "
                    + "em viagens. Desassocie-o das viagens primeiro.");
        tripulanteDAO.eliminar(id);
    }

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

 