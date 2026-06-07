package dao;

import model.TipoNavio;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TipoNavioDAO {

    public List<TipoNavio> listarTodos() {
        List<TipoNavio> lista = new ArrayList<>();
        String sql = "SELECT id, nome, capacidade_maxima, max_cargas FROM dias.TIPO_NAVIO";

        try (Connection conn = db.getConn();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) lista.add(mapear(rs));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }

    public TipoNavio buscarPorId(int id) {
        String sql = "SELECT id, nome, capacidade_maxima, max_cargas FROM dias.TIPO_NAVIO WHERE id = ?";

        try (Connection conn = db.getConn();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void inserir(TipoNavio t) {
        String sql = "INSERT INTO dias.TIPO_NAVIO (nome, capacidade_maxima, max_cargas) VALUES (?, ?, ?)";

        try (Connection conn = db.getConn();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, t.getNome());
            stmt.setDouble(2, t.getCapacidadeMaxima());
            stmt.setInt(3, t.getMaxCargas());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) t.setId(rs.getInt(1));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void atualizar(TipoNavio t) {
        String sql = "UPDATE dias.TIPO_NAVIO SET nome = ?, capacidade_maxima = ?, max_cargas = ? WHERE id = ?";

        try (Connection conn = db.getConn();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, t.getNome());
            stmt.setDouble(2, t.getCapacidadeMaxima());
            stmt.setInt(3, t.getMaxCargas());
            stmt.setInt(4, t.getId());
            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void eliminar(int id) {
        String sql = "DELETE FROM dias.TIPO_NAVIO WHERE id = ?";

        try (Connection conn = db.getConn();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private TipoNavio mapear(ResultSet rs) throws Exception {
        return new TipoNavio(
                rs.getInt("id"),
                rs.getString("nome"),
                rs.getDouble("capacidade_maxima"),
                rs.getInt("max_cargas")
        );
    }
}