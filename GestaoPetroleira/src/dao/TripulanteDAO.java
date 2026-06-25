package dao;

import model.Funcao;
import model.Tripulante;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TripulanteDAO {

    // Aliases para o mapear() continuar a usar os nomes do modelo.
    private static final String COLS =
            "id_tripulante AS id, nome, nr_identificacao AS numero_matricula, funcao, disponibilidade AS disponivel";

    public List<Tripulante> listarTodos() {
        List<Tripulante> lista = new ArrayList<>();
        try (Connection c = db.getConn(); Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT " + COLS + " FROM dias.TRIPULANTE")) {
            while (rs.next()) lista.add(mapear(rs));
        } catch (Exception e) { e.printStackTrace(); }
        return lista;
    }

    public List<Tripulante> listarDisponiveis() {
        List<Tripulante> lista = new ArrayList<>();
        try (Connection c = db.getConn(); Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT " + COLS + " FROM dias.TRIPULANTE WHERE disponibilidade=1")) {
            while (rs.next()) lista.add(mapear(rs));
        } catch (Exception e) { e.printStackTrace(); }
        return lista;
    }

    public List<Tripulante> listarPorViagem(int idViagem) {
        List<Tripulante> lista = new ArrayList<>();
        String sql = "SELECT t.id_tripulante AS id, t.nome, t.nr_identificacao AS numero_matricula, " +
                     "t.funcao, t.disponibilidade AS disponivel " +
                     "FROM dias.TRIPULANTE t JOIN dias.TRIPULACAO_VIAGEM tv ON t.id_tripulante=tv.id_tripulante " +
                     "WHERE tv.id_viagem=?";
        try (Connection c = db.getConn(); PreparedStatement st = c.prepareStatement(sql)) {
            st.setInt(1, idViagem);
            try (ResultSet rs = st.executeQuery()) { while (rs.next()) lista.add(mapear(rs)); }
        } catch (Exception e) { e.printStackTrace(); }
        return lista;
    }

    public Tripulante buscarPorId(int id) {
        try (Connection c = db.getConn();
             PreparedStatement st = c.prepareStatement(
                 "SELECT " + COLS + " FROM dias.TRIPULANTE WHERE id_tripulante=?")) {
            st.setInt(1, id);
            try (ResultSet rs = st.executeQuery()) { if (rs.next()) return mapear(rs); }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    public void inserir(Tripulante t) {
        try (Connection c = db.getConn();
             PreparedStatement st = c.prepareStatement(
                 "INSERT INTO dias.TRIPULANTE(nome,nr_identificacao,funcao,disponibilidade) VALUES(?,?,?,?)",
                 Statement.RETURN_GENERATED_KEYS)) {
            st.setString(1, t.getNome()); st.setString(2, t.getNumeroMatricula());
            st.setString(3, t.getFuncao().name()); st.setBoolean(4, t.isDisponivel());
            st.executeUpdate();
            try (ResultSet rs = st.getGeneratedKeys()) { if (rs.next()) t.setId(rs.getInt(1)); }
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void atualizar(Tripulante t) {
        try (Connection c = db.getConn();
             PreparedStatement st = c.prepareStatement(
                 "UPDATE dias.TRIPULANTE SET nome=?,nr_identificacao=?,funcao=?,disponibilidade=? WHERE id_tripulante=?")) {
            st.setString(1, t.getNome()); st.setString(2, t.getNumeroMatricula());
            st.setString(3, t.getFuncao().name()); st.setBoolean(4, t.isDisponivel());
            st.setInt(5, t.getId()); st.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void eliminar(int id) {
        try (Connection c = db.getConn();
             PreparedStatement st = c.prepareStatement("DELETE FROM dias.TRIPULANTE WHERE id_tripulante=?")) {
            st.setInt(1, id); st.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private Tripulante mapear(ResultSet rs) throws Exception {
        return new Tripulante(rs.getInt("id"), rs.getString("nome"),
                rs.getString("numero_matricula"), Funcao.valueOf(rs.getString("funcao")),
                rs.getBoolean("disponivel"));
    }
}
