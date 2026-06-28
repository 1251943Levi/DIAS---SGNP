package service;

import dao.CargaDAO;
import dao.CompatibilidadeDAO;
import dao.NavioDAO;
import dao.TipoCargaDAO;
import model.*;
import java.util.List;

public class CargaService {
    private final CargaDAO cargaDAO = new CargaDAO();
    private final TipoCargaDAO tipoCargaDAO = new TipoCargaDAO();
    private final CompatibilidadeDAO compatibilidadeDAO = new CompatibilidadeDAO();
    private final NavioDAO navioDAO = new NavioDAO();

    // ── TIPO_CARGA CRUD ──────────────────────────────────────────────────────
    public List<TipoCarga> listarTiposCarga() { return tipoCargaDAO.listarTodos(); }

    public void adicionarTipoCarga(TipoCarga tc) { tipoCargaDAO.inserir(tc); }

    public void eliminarTipoCarga(int id) throws Exception {
        // Não permitir eliminar um tipo de carga em uso (cargas ou compatibilidades),
        // senão a FK impede o DELETE com uma mensagem técnica pouco clara.
        boolean usadoEmCargas = cargaDAO.listarTodos().stream()
                .anyMatch(c -> c.getTipoCarga() != null && c.getTipoCarga().getId() == id);
        if (usadoEmCargas)
            throw new Exception("Não é possível eliminar: existem cargas deste tipo. Elimine-as primeiro.");
        boolean usadoEmCompat = compatibilidadeDAO.listarTodos().stream()
                .anyMatch(comp -> comp.getTipoCarga() != null && comp.getTipoCarga().getId() == id);
        if (usadoEmCompat)
            throw new Exception("Não é possível eliminar: este tipo de carga está em compatibilidades. "
                    + "Remova as compatibilidades primeiro.");
        tipoCargaDAO.eliminar(id);
    }

    // ── CARGA CRUD ────────────────────────────────────────────────────────────
    public List<Carga> listarCargas() { return cargaDAO.listarTodos(); }

    /** Cargas livres (sem viagem) — para a combo de associação a uma viagem. */
    public List<Carga> listarCargasLivres() { return cargaDAO.listarSemViagem(); }

    /** Cargas livres E compatíveis com o tipo de navio indicado (para a combo da viagem). */
    public List<Carga> listarCargasLivresCompativeis(int idTipoNavio) {
        List<Integer> tiposCompat = compatibilidadeDAO.listarCargasCompativeis(idTipoNavio)
                .stream().map(TipoCarga::getId).collect(java.util.stream.Collectors.toList());
        return cargaDAO.listarSemViagem().stream()
                .filter(c -> c.getTipoCarga() != null && tiposCompat.contains(c.getTipoCarga().getId()))
                .collect(java.util.stream.Collectors.toList());
    }

    public List<Carga> listarCargasDaViagem(int idViagem) {
        return cargaDAO.listarPorViagem(idViagem);
    }

    /** Histórico de entregas (cargas de viagens concluídas), só de leitura. */
    public List<EntregaHistorico> listarHistoricoEntregas() {
        return cargaDAO.listarHistoricoEntregas();
    }

    public void adicionarCarga(Carga carga) { cargaDAO.inserir(carga); }

    public void atualizarCarga(Carga carga) { cargaDAO.atualizar(carga); }

    public void eliminarCarga(int id) { cargaDAO.eliminar(id); }

    // ── REGRAS DE NEGÓCIO ─────────────────────────────────────────────────────

    /**
     * Regra 1: O tipo de carga deve ser compatível com o tipo do navio.
     * Regra 2: A capacidade máxima do navio não pode ser excedida.
     * Regra 3: O número de cargas por viagem não pode exceder o máximo
     *          definido pelo tipo de navio.
     */
    public void associarCargaAViagem(Viagem viagem, Carga carga, int numeroTanque) throws Exception {
        // Só se podem associar cargas enquanto a viagem está PLANEADA.
        if (viagem.getEstado() != EstadoViagem.PLANEADA)
            throw new Exception("Só é possível associar cargas a viagens no estado PLANEADA. "
                    + "Esta viagem está " + viagem.getEstado() + ".");

        Navio navio = navioDAO.buscarPorId(viagem.getNavio().getId());
        if (navio == null) throw new Exception("Navio não encontrado.");

        // Regra 1 – compatibilidade de tipo
        boolean compativel = compatibilidadeDAO.existeCompatibilidade(
                navio.getTipoNavio().getId(), carga.getTipoCarga().getId());
        if (!compativel)
            throw new Exception("Tipo de carga '" + carga.getTipoCarga().getNome() +
                    "' não é compatível com o tipo de navio '" + navio.getTipoNavio().getNome() + "'.");

        // Regra do compartimento: tem de existir no navio e estar livre
        if (!compartimentoValido(numeroTanque, navio.getNumCompartimentos()))
            throw new Exception("Compartimento inválido. Este navio tem " + navio.getNumCompartimentos() + " tanque(s).");
        for (Carga c : cargaDAO.listarPorViagem(viagem.getId()))
            if (c.getNumeroTanque() != null && c.getNumeroTanque() == numeroTanque)
                throw new Exception("O compartimento " + numeroTanque + " já está ocupado nesta viagem.");

        // Regra 3 – número máximo de cargas por viagem (definido pelo tipo de navio)
        int maxCargas = navio.getTipoNavio().getMaxCargas();
        int cargasAtuais = cargaDAO.contarCargasPorViagem(viagem.getId());
        if (limiteCargasAtingido(maxCargas, cargasAtuais))
            throw new Exception(String.format(
                    "Limite de cargas atingido. O tipo de navio '%s' permite no máximo %d carga(s) por viagem.",
                    navio.getTipoNavio().getNome(), maxCargas));

        // Regra 2 – capacidade
        double pesoAtual = cargaDAO.pesotalPorViagem(viagem.getId());
        if (capacidadeExcedida(pesoAtual, carga.getPeso(), navio.getCapacidadeMaxima()))
            throw new Exception(String.format(
                    "Capacidade excedida. Ocupado: %.1f t, Carga: %.1f t, Máximo: %.1f t.",
                    pesoAtual, carga.getPeso(), navio.getCapacidadeMaxima()));

        // Cria uma CÓPIA da carga para a viagem. A original fica no catálogo, reutilizável
        // como "template" noutras viagens (e o histórico da viagem fica com a sua própria cópia).
        // Opção 2: a cópia herda os portos da rota da viagem (origem -> carga, destino -> descarga).
        // O template não tem portos; é a viagem que os define.
        Carga copia = new Carga(0, carga.getDesignacao(), carga.getTipoCarga(),
                carga.getVolume(), carga.getPeso(), viagem.getPortoOrigem(), viagem.getPortoDestino());
        cargaDAO.inserir(copia);
        cargaDAO.associarAViagem(viagem.getId(), copia.getId(), numeroTanque);
    }

    /** Números dos compartimentos ainda livres do navio desta viagem (para a combo). */
    public List<Integer> tanquesLivres(Viagem viagem) {
        Navio navio = navioDAO.buscarPorId(viagem.getNavio().getId());
        java.util.Set<Integer> usados = new java.util.HashSet<>();
        for (Carga c : cargaDAO.listarPorViagem(viagem.getId()))
            if (c.getNumeroTanque() != null) usados.add(c.getNumeroTanque());
        List<Integer> livres = new java.util.ArrayList<>();
        for (int i = 1; i <= navio.getNumCompartimentos(); i++)
            if (!usados.contains(i)) livres.add(i);
        return livres;
    }

    /** Esvazia os compartimentos de uma viagem (cargas entregues) — usado ao concluir. */
    public void esvaziarCompartimentos(int idViagem) {
        cargaDAO.eliminarPorViagem(idViagem);
    }

    /**
     * Decide se o limite de cargas por viagem já foi atingido.
     * Extraído como método puro para permitir teste unitário sem base de dados.
     * Um {@code maxCargas <= 0} significa "sem limite definido".
     */
    static boolean limiteCargasAtingido(int maxCargas, int cargasAtuais) {
        return maxCargas > 0 && cargasAtuais >= maxCargas;
    }

    /**
     * Decide se um número de compartimento é válido para um navio com
     * {@code numCompartimentos} tanques (tem de estar entre 1 e o total).
     * Extraído como método puro para teste unitário sem base de dados.
     */
    static boolean compartimentoValido(int numeroTanque, int numCompartimentos) {
        return numeroTanque >= 1 && numeroTanque <= numCompartimentos;
    }

    /**
     * Decide se associar uma carga excede a capacidade do navio: a soma do peso
     * já ocupado com o peso da nova carga não pode ultrapassar a capacidade máxima.
     * Extraído como método puro para teste unitário sem base de dados.
     */
    static boolean capacidadeExcedida(doub