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

    public void eliminarTipoCarga(int id) { tipoCargaDAO.eliminar(id); }

    // ── CARGA CRUD ────────────────────────────────────────────────────────────
    public List<Carga> listarCargas() { return cargaDAO.listarTodos(); }

    public List<Carga> listarCargasDaViagem(int idViagem) {
        return cargaDAO.listarPorViagem(idViagem);
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
    public void associarCargaAViagem(Viagem viagem, Carga carga) throws Exception {
        Navio navio = navioDAO.buscarPorId(viagem.getNavio().getId());
        if (navio == null) throw new Exception("Navio não encontrado.");

        // Regra 1 – compatibilidade de tipo
        boolean compativel = compatibilidadeDAO.existeCompatibilidade(
                navio.getTipoNavio().getId(), carga.getTipoCarga().getId());
        if (!compativel)
            throw new Exception("Tipo de carga '" + carga.getTipoCarga().getNome() +
                    "' não é compatível com o tipo de navio '" + navio.getTipoNavio().getNome() + "'.");

        // Regra 3 – número máximo de cargas por viagem (definido pelo tipo de navio)
        int maxCargas = navio.getTipoNavio().getMaxCargas();
        int cargasAtuais = cargaDAO.contarCargasPorViagem(viagem.getId());
        if (limiteCargasAtingido(maxCargas, cargasAtuais))
            throw new Exception(String.format(
                    "Limite de cargas atingido. O tipo de navio '%s' permite no máximo %d carga(s) por viagem.",
                    navio.getTipoNavio().getNome(), maxCargas));

        // Regra 2 – capacidade
        double pesoAtual = cargaDAO.pesotalPorViagem(viagem.getId());
        if (pesoAtual + carga.getPeso() > navio.getCapacidadeMaxima())
            throw new Exception(String.format(
                    "Capacidade excedida. Ocupado: %.1f t, Carga: %.1f t, Máximo: %.1f t.",
                    pesoAtual, carga.getPeso(), navio.getCapacidadeMaxima()));

        cargaDAO.associarAViagem(viagem.getId(), carga.getId());
    }

    /**
     * Decide se o limite de cargas por viagem já foi atingido.
     * Extraído como método puro para permitir teste unitário sem base de dados.
     * Um {@code maxCargas <= 0} significa "sem limite definido".
     */
    static boolean limiteCargasAtingido(int maxCargas, int cargasAtuais) {
        return maxCargas > 0 && cargasAtuais >= maxCargas;
    }

    public void desassociarCargaDaViagem(int idViagem, int idCarga) {
        cargaDAO.desassociarDaViagem(idViagem, idCarga);
    }

    // ── COMPATIBILIDADE CRUD ──────────────────────────────────────────────────
    public List<Compatibilidade> listarCompatibilidades() {
        return compatibilidadeDAO.listarTodos();
    }

    public List<TipoCarga> listarCargasCompativeis(int idTipoNavio) {
        return compatibilidadeDAO.listarCargasCompativeis(idTipoNavio);
    }

    public void adicionarCompatibilidade(Compatibilidade comp) {
        compatibilidadeDAO.inserir(comp);
    }

    public void eliminarCompatibilidade(int idTipoNavio, int idTipoCarga) {
        compatibilidadeDAO.eliminar(idTipoNavio, idTipoCarga);
    }
}
