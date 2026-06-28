package service;

import dao.CargaDAO;
import dao.CompatibilidadeDAO;
import dao.NavioDAO;
import dao.PortoDAO;
import dao.TripulacaoViagemDAO;
import dao.ViagemDAO;
import model.Carga;
import model.EstadoOperacional;
import model.EstadoViagem;
import model.Funcao;
import model.Navio;
import model.Porto;
import model.TipoCarga;
import model.TripulacaoViagem;
import model.Viagem;

import java.time.LocalDate;
import java.util.List;

public class ViagemService {

    private final ViagemDAO viagemDAO = new ViagemDAO();
    private final NavioDAO navioDAO = new NavioDAO();
    private final PortoDAO portoDAO = new PortoDAO();
    private final TripulacaoViagemDAO tripulacaoViagemDAO = new TripulacaoViagemDAO();
    private final CargaDAO cargaDAO = new CargaDAO();
    private final TripulacaoService tripulacaoService = new TripulacaoService();
    private final CompatibilidadeDAO compatibilidadeDAO = new CompatibilidadeDAO();

    public List<Viagem> listarViagens() {
        return viagemDAO.listarTodos();
    }

    public List<Porto> listarPortos() {
        return portoDAO.listarTodos();
    }

    /** Cria uma viagem PLANEADA sem data de chegada prevista. */
    public void criarViagem(Navio navio, Porto origem, Porto destino, LocalDate dataPartida) throws Exception {
        criarViagem(navio, origem, destino, dataPartida, null);
    }

    /** Cria uma viagem PLANEADA, garantindo que o navio nao tem outra viagem ativa. */
    public void criarViagem(Navio navio, Porto origem, Porto destino,
                            LocalDate dataPartida, LocalDate dataChegada) throws Exception {
        if (navio == null) throw new Exception("Selecione um navio.");
        if (origem == null || destino == null) throw new Exception("Selecione os portos de origem e destino.");
        if (origem.getId() == destino.getId()) throw new Exception("Origem e destino devem ser diferentes.");
        if (dataPartida == null) throw new Exception("Indique a data de partida.");
        if (dataChegada != null && dataChegada.isBefore(dataPartida))
            throw new Exception("A data de chegada não pode ser anterior à data de partida.");

        // Regra: o navio só pode partir do porto onde se encontra (porto atual).
        // Vai buscar o estado ATUAL à BD (a combo pode estar desatualizada).
        // Se ainda não tem porto atual (acabado de criar), permite qualquer origem.
        Navio navioAtual = navioDAO.buscarPorId(navio.getId());
        Integer idPortoAtual = navioAtual != null ? navioAtual.getIdPortoAtual() : navio.getIdPortoAtual();
        if (idPortoAtual != null && origem.getId() != idPortoAtual) {
            Porto portoAtual = portoDAO.buscarPorId(idPortoAtual);
            String nome = portoAtual != null ? portoAtual.getNome() : ("porto " + idPortoAtual);
            throw new Exception("O navio encontra-se em " + nome + "; a viagem tem de partir desse porto.");
        }

        // Regra: a partida não pode ser antes da chegada da última viagem concluída do navio
        // (o navio só fica disponível quando chega).
        LocalDate ultimaChegada = null;
        for (Viagem v2 : viagemDAO.listarPorNavio(navio.getId())) {
            if (v2.getEstado() == EstadoViagem.CONCLUIDA && v2.getDataChegada() != null
                    && (ultimaChegada == null || v2.getDataChegada().isAfter(ultimaChegada)))
                ultimaChegada = v2.getDataChegada();
        }
        if (ultimaChegada != null && dataPartida.isBefore(ultimaChegada))
            throw new Exception("O navio só ficou disponível em " + ultimaChegada
                    + " (chegada da última viagem); a partida não pode ser anterior a essa data.");

        garantirSemViagemAtiva(navio.getId());

        Viagem v = new Viagem(0, navio, origem, destino, dataPartida, dataChegada, EstadoViagem.PLANEADA);
        viagemDAO.inserir(v);
        // (Modelo A) As cargas são associadas manualmente na aba Viagens, com as regras de
        // compatibilidade/capacidade/máx. cargas — não há geração automática de carga pendente.
    }

    /** PLANEADA -> EM_CURSO (so se o navio estiver ATIVO e houver tripulacao com capitao). */
    public void iniciarViagem(Viagem viagem) throws Exception {
        validarTransicao(viagem.getEstado(), EstadoViagem.EM_CURSO);
        // A viagem só pode passar a EM_CURSO se o navio estiver ATIVO.
        // 1) Verificação rápida com o estado conhecido (sem aceder à BD).
        if (viagem.getNavio().getEstadoOperacional() != EstadoOperacional.ATIVO)
            throw new Exception("O navio está " + viagem.getNavio().getEstadoOperacional()
                    + "; só pode iniciar a viagem se estiver ATIVO.");
        // 2) Reconfirma com o estado ATUAL na BD (pode ter mudado depois de a viagem
        //    ter sido planeada — ex.: navio passou a INATIVO ou entrou em manutenção).
        Navio navioAtual = navioDAO.buscarPorId(viagem.getNavio().getId());
        if (navioAtual != null && navioAtual.getEstadoOperacional() != EstadoOperacional.ATIVO)
            throw new Exception("O navio está " + navioAtual.getEstadoOperacional()
                    + "; só pode iniciar a viagem se estiver ATIVO.");

        // Regra: a viagem tem de ter cargas (com peso) e não pode exceder a capacidade do navio.
        double pesoTotal = cargaDAO.pesotalPorViagem(viagem.getId());
        if (pesoTotal <= 0)
            throw new Exception("A viagem não tem cargas associadas. Associe pelo menos uma carga antes de iniciar.");
        if (pesoTotal > viagem.getNavio().getCapacidadeMaxima())
            throw new Exception("A carga total (" + pesoTotal + " t) excede a capacidade do navio ("
                    + viagem.getNavio().getCapacidadeMaxima() + " t).");

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

        // Atualiza o porto atual do navio para o destino. Vai buscar o navio FRESCO à BD
        // (em vez de usar o objeto da tabela de viagens, que pode estar desatualizado) para
        // não gravar por cima de edições entretanto feitas ao navio (capacidade, bandeira, etc.).
        Navio navio = navioDAO.buscarPorId(viagem.getNavio().getId());
        if (navio == null) navio = viagem.getNavio();
        navio.setIdPortoAtual(viagem.getPortoDestino().getId());
        navioDAO.atualizar(navio);

        // Viagem concluída: liberta a tripulação (fica disponível para novas viagens).
        // O navio fica também livre (viagens concluídas não contam como ativas).
        tripulacaoService.libertarTripulacao(viagem.getId());

        // As cargas ficam guardadas na viagem como registo da entrega (histórico).
        // Os tanques do navio ficam livres para a próxima viagem (que começa vazia).
    }

    /** PLANEADA/EM_CURSO -> CANCELADA. */
    public void cancelarViagem(Viagem viagem) throws Exception {
        validarTransicao(viagem.getEstado(), EstadoViagem.CANCELADA);
        viagem.setEstado(EstadoViagem.CANCELADA);
        viagemDAO.atualizar(viagem);

        // Viagem cancelada: liberta a tripulação para outras viagens.
        tripulacaoService.libertarTripulacao(viagem.getId());
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
