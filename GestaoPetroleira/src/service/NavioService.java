package service;

import dao.ManutencaoDAO;
import dao.NavioDAO;
import model.EstadoOperacional;
import model.Manutencao;
import model.Navio;
import java.time.LocalDate;
import java.util.List;

public class NavioService {
    private final NavioDAO navioDAO = new NavioDAO();
    private final ManutencaoDAO manutencaoDAO = new ManutencaoDAO();

    public List<Navio> listarNavios() { return navioDAO.listarTodos(); }

    /** Apenas navios ATIVO — os únicos que podem ser usados em viagens. */
    public List<Navio> listarNaviosAtivos() {
        return navioDAO.listarTodos().stream()
                .filter(n -> n.getEstadoOperacional() == EstadoOperacional.ATIVO)
                .collect(java.util.stream.Collectors.toList());
    }

    public void adicionarNavio(Navio navio) throws Exception {
        validarTanques(navio);
        if (navio.getEstadoOperacional() == null) navio.setEstadoOperacional(EstadoOperacional.ATIVO);
        navioDAO.inserir(navio);
    }

    public void atualizarNavio(Navio navio) throws Exception {
        validarTanques(navio);
        navioDAO.atualizar(navio);
    }

    /** Coerência: o navio deve ter tanques suficientes para o nº de cargas que o seu tipo permite. */
    private void validarTanques(Navio navio) throws Exception {
        if (navio.getTipoNavio() != null
                && navio.getNumCompartimentos() < navio.getTipoNavio().getMaxCargas())
            throw new Exception("O navio precisa de pelo menos " + navio.getTipoNavio().getMaxCargas()
                    + " tanques: o tipo \"" + navio.getTipoNavio().getNome() + "\" permite "
                    + navio.getTipoNavio().getMaxCargas() + " carga(s) por viagem.");
    }

    public void eliminarNavio(int id) {
        // Remove primeiro as manutenções do navio, senão a chave estrangeira impede o DELETE
        for (Manutencao m : manutencaoDAO.listarPorNavio(id)) manutencaoDAO.eliminar(m.getId());
        navioDAO.eliminar(id);
    }

    public boolean podeIniciarViagem(Navio navio) {
        return navio.getEstadoOperacional() == EstadoOperacional.ATIVO;
    }

    public void registarManutencao(Navio navio, String descricao) throws Exception {
        for (Manutencao m : manutencaoDAO.listarPorNavio(navio.getId())) {
            if (m.emCurso()) throw new Exception("O navio já tem uma manutenção em curso.");
        }
        manutencaoDAO.inserir(new Manutencao(0, navio, LocalDate.now(), null, descricao));
        navio.setEstadoOperacional(EstadoOperacional.EM_MANUTENCAO);
        navioDAO.atualizar(navio);
    }

    public void concluirManutencao(Manutencao manutencao) {
        manutencao.setDataFim(LocalDate.now());
        manutencaoDAO.atualizar(manutencao);
        manutencao.getNavio().setEstadoOperacional(EstadoOperacional.ATIVO);
        navioDAO.atualizar(manutencao.getNavio());
    }

    public void eliminarManutencao(int id) {
        manutencaoDAO.eliminar(id);
    }

    public List<Manutencao> listarManutencoes(int idNavio) {
        return manutencaoDAO.listarPorNavio(idNavio);
    }
}
