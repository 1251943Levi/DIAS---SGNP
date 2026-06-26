package service;

import dao.CargaDAO;
import dao.CompatibilidadeDAO;
import dao.NavioDAO;
import dao.PortoDAO;
import dao.TripulacaoViagemDAO;
import dao.ViagemDAO;
import model.Carga;
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

        garantirSemViagemAtiva(navio.getId());

        Viagem v = new Viagem(0, navio, origem, destino, dataPartida, dataChegada, EstadoViagem.PLANEADA);
        viagemDAO.inserir(v);

        // Gera automaticamente uma carga PENDENTE (peso 0), com um tipo compativel com o navio.
        gerarCargaPendente(v);
    }

    /**
     * Cria uma carga "pendente" (peso e volume a zero) associada a viagem, com um tipo de carga
     * compativel com o tipo de navio. O utilizador preenche depois o peso na Gestao de Cargas.
     * Os portos da carga herdam os da viagem.
     */
    private void gerarCargaPendente(Viagem v) {
        List<TipoCarga> compativeis =
                compatibilidadeDAO.listarCargasCompativeis(v.getNavio().getTipoNavio().getId());
        if (compativeis.isEmpty()) return;   // sem compatibilidade definida -> nao gera

        TipoCarga tipo = compativeis.get(0); // tipo compativel por defeito (utilizador pode alterar)
        Carga pendente = new Carga(0, "Carga pendente - " + v.getNavio().getNome(), tipo, 0, 0,
                v.getPortoOrigem(), v.getPortoDestino());
        cargaDAO.inserir(pendente);
        cargaDAO.associarAViagem(v.getId(), pendente.getId());
    }

    /** PLANEADA -> EM_CURSO (so se o navio estiver ATIVO e houver tripulacao com capitao). */
    public void iniciarViagem(Viagem viagem) throws Exception {
        validarTransicao(viagem.getEstado(), EstadoViagem.EM_CURSO);
        if (!viagem.getNavio().podeIniciarViagem())
            throw new Exception("O navio nao esta ATIVO e nao pode iniciar viagem.");

        // Regra: a carga nao pode estar pendente (peso 0) e nao pode exceder a capacidade do navio.
        double pesoTotal = cargaDAO.pesotalPorViagem(viagem.getId());
        if (pesoTotal <= 0)
            throw new Exception("A carga ainda esta pendente (peso a zero). Preencha o peso da carga antes de iniciar.");
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

        Navio navio = viagem.getNavio();
        navio.setIdPortoAtual(viagem.getPortoDestino().getId());
        navioDAO.atualizar(navio);

        // Viagem concluída: liberta a tripulação (fica disponível para novas viagens).
        // O navio fica também livre (viagens concluídas não contam como ativas).
        tripulacaoService.libertarTripulacao(viagem.getId());
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
