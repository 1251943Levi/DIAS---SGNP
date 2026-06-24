package service;

import dao.NavioDAO;
import dao.PortoDAO;
import dao.TripulacaoViagemDAO;
import dao.ViagemDAO;
import model.*;

import java.time.LocalDate;
import java.util.List;

public class ViagemService {

    private final ViagemDAO viagemDAO = new ViagemDAO();
    private final NavioDAO navioDAO = new NavioDAO();
    private final PortoDAO portoDAO = new PortoDAO();
    private final TripulacaoViagemDAO tripulacaoViagemDAO = new TripulacaoViagemDAO();

    public List<Viagem> listarViagens() {
        return viagemDAO.listarTodos();
    }

    public List<Porto> listarPortos() {
        return portoDAO.listarTodos();
    }

    /** Cria uma viagem PLANEADA, garantindo que o navio nao tem outra viagem ativa. */
    public void criarViagem(Navio navio, Porto origem, Porto destino, LocalDate dataPartida) throws Exception {
        if (navio == null) throw new Exception("Selecione um navio.");
        if (origem == null || destino == null) throw new Exception("Selecione os portos de origem e destino.");
        if (origem.getId() == destino.getId()) throw new Exception("Origem e destino devem ser diferentes.");
        if (dataPartida == null) throw new Exception("Indique a data de partida.");

        garantirSemViagemAtiva(navio.getId());

        Viagem v = new Viagem(0, navio, origem, destino, dataPartida, null, EstadoViagem.PLANEADA);
        viagemDAO.inserir(v);
    }

    /** PLANEADA -> EM_CURSO (so se o navio estiver ATIVO e houver tripulacao com capitao). */
    public void iniciarViagem(Viagem viagem) throws Exception {
        validarTransicao(viagem.getEstado(), EstadoViagem.EM_CURSO);
        if (!viagem.getNavio().podeIniciarViagem())
            throw new Exception("O navio nao esta ATIVO e nao pode iniciar viagem.");

        // Regra: a viagem tem de ter tripulacao associada (com um capitao) antes de iniciar.
        List<TripulacaoViagem> tripulacao = tripulacaoViagemDAO.listarPorViagem(viagem.getId());
        if (tripulacao.isEmpty())
            throw new Exception("A viagem nao tem tripulacao. Associe tripulantes antes de a iniciar.");
        boolean temCapitao = tripulacao.stream().anyMatch(tv -> tv.getFuncao() == Funcao.CAPITAO);
        if (!temCapitao)
            throw new Exception("A viagem precisa de um capitao na tripulacao antes de iniciar.");

        viagem.setEstado(EstadoViagem.EM_CURSO);
        viagemDAO.atualizar(viagem);
    }

    /** EM_CURSO -> CONCLUIDA (regista chegada e atualiza porto atual do navio). */
    public void concluirViagem(Viagem viagem) throws Exception {
        validarTransicao(viagem.getEstado(), EstadoViagem.CONCLUIDA);
        viagem.setEstado(EstadoViagem.CONCLUIDA);
        viagem.setDataChegada(LocalDate.now());
        viagemDAO.atualizar(viagem);

        Navio navio = viagem.getNavio();
        navio.setIdPortoAtual(viagem.getPortoDestino().getId());
        navioDAO.atualizar(navio);
    }

    /** PLANEADA/EM_CURSO -> CANCELADA. */
    public void cancelarViagem(Viagem viagem) throws Exception {
        validarTransicao(viagem.getEstado(), EstadoViagem.CANCELADA);
        viagem.setEstado(EstadoViagem.CANCELADA);
        viagemDAO.atualizar(viagem);
    }

    private void garantirSemViagemAtiva(int idNavio) throws Exception {
        for (Viagem v : viagemDAO.listarPorNavio(idNavio)) {
            if (v.estaAtiva()) throw new Exception("O navio ja tem uma viagem ativa.");
        }
    }

    /** Matriz de transicoes validas. */
    private void validarTransicao(EstadoViagem atual, EstadoViagem novo) throws Exception {
        boolean ok = switch (atual) {
            case PLANEADA -> novo == EstadoViagem.EM_CURSO || novo == EstadoViagem.CANCELADA;
            case EM_CURSO -> novo == EstadoViagem.CONCLUIDA || novo == EstadoViagem.CANCELADA;
            case CONCLUIDA, CANCELADA -> false;
        };
        if (!ok) throw new Exception("Transicao invalida: " + atual + " -> " + novo + ".");
    }
}
