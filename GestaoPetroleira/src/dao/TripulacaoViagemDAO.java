package dao;

import model.Funcao;
import model.TripulacaoViagem;
import model.Tripulante;
import model.Viagem;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TripulacaoViagemDAO {
    private final TripulanteDAO tripulanteDAO = new TripulanteDAO();
    private final ViagemDAO viagemDAO = new ViagemDAO();

    public List<TripulacaoViagem> listarPorViagem(int idViagem) {
        List<TripulacaoViagem> lista = new ArrayList<>();
        try (Connection c = db.getConn();
             PreparedStatement st = c.prepareStatement(
                 "SELECT id_viagem,id_tripulante,funcao FROM dias.TRIPULACAO_VIAGEM WHERE id_viagem=?")) {
            st.setInt(1, idViagem);
            try (ResultSet rs = st.executeQuery()) { while (rs.next()) lista.add(mapear(rs)); }
        } catch (Exception e) { throw new DataAccessException("Erro ao aceder à base de dados (tripulação da viagem): " + e.getMessage(), e); }
        return lista;
    }

    /** Historico: todas as participacoes (viagens) de um tripulante. */
    public List<TripulacaoViagem> listarPorTripulante(int idTripulante) {
        List<TripulacaoViagem> lista = new ArrayList<>();
        try (Connection c = db.getConn();
             PreparedStatement st = c.prepareStatement(
                 "SELECT id_viagem,id_tripulante,funcao FROM dias.TRIPULACAO_VIAGEM WHERE id_tripulante=?")) {
            st.setInt(1, idTripulante);
            try (ResultSet rs = st.executeQuery()) { while (rs.next()) lista.add(mapear(rs)); }
        } catch (Exception e) { throw new DataAccessException("Erro ao aceder à base de dados (tripulação da viagem): " + e.getMessage(), e); }
        return lista;
    }

    public void inserir(TripulacaoViagem tv) {
        try (Connection c = db.getConn();
             PreparedStatement st = c.prepareStatement(
                 "INSERT INTO dias.TRIPULACAO_VIAGEM(id_viagem,id_tripulante,funcao) VALUES(?,?,?)")) {
            st.setInt(1, tv.getViagem().getId()); st.setInt(2, tv.getTripulante().getId());
            st.setString(3, tv.getFuncao().name()); st.executeUpdate();
        } catch (Exception e) { throw new DataAccessException("Erro ao aceder à base de dados (tripulação da viagem): " + e.getMessage(), e); }
    }

    public void eliminar(int idViagem, int idTripulante) {
        try (Connection c = db.getConn();
             PreparedStatement st = c.prepareStatement(
                 "DELETE FROM dias.TRIPULACAO_VIAGEM WHERE id_viagem=? AND id_tripulante=?")) {
            st.setInt(1, idViagem); st.setInt(2, idTripulante); st.executeUpdate();
        } catch (Exception e) { throw new DataAccessException("Erro ao aceder à base de dados (tripulação da viagem): " + e.getMessage(), e); }
    }

    public int contarPorViagem(int idViagem) {
        try (Connection c = db.getConn();
             PreparedStatement st = c.prepareStatement(
                 "SELECT COUNT(*) FROM dias.TRIPULACAO_VIAGEM WHERE id_viagem=?")) {
            st.setInt(1, idViagem);
            try (ResultSet rs = st.executeQuery()) { if (rs.next()) return rs.getInt(1); }
        } catch (Exception e) { throw new DataAccessException("Erro ao aceder à base de dados (tripulação da viagem): " + e.getMessage(), e); }
        return 0;
    }

    public boolean jaAlocado(int idViagem, int idTripulante) {
        try (Connection c = db.getConn();
             PreparedStatement st = c.prepareStatement(
                 "SELECT COUNT(*) FROM dias.TRIPULACAO_VIAGEM WHERE id_viagem=? AND id_tripulante=?")) {
            st.setInt(1, idViagem); st.setInt(2, idTripulante);
            try (ResultSet rs = st.executeQuery()) { if (rs.next()) return rs.getInt(1) > 0; }
        } catch (Exception e) { throw new DataAccessException("Erro ao aceder à base de dados (tripulação da viagem): " + e.getMessage(), e); }
        return false;
    }

    private TripulacaoViagem mapear(ResultSet rs) throws Exception {
        Viagem v = viagemDAO.buscarPorId(rs.getInt("id_viagem"));
        Tripulante t = tripulanteDAO.buscarPorId(rs.getInt("id_tripulante"));
        return new TripulacaoViagem(0, v, t, Funcao.valueOf(rs.getString("funcao")));
    }
}
