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

    public void adicionarNavio(Navio navio) {
        navio.setEstadoOperacional(EstadoOperacional.ATIVO);
        navioDAO.inserir(navio);
    }

    public void atualizarNavio(Navio navio) { navioDAO.atualizar(navio); }

    public void eliminarNavio(int id) { navioDAO.eliminar(id); }

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
