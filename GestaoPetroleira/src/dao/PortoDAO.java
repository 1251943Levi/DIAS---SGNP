package dao;

import model.Porto;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PortoDAO {

    public List<Porto> listarTodos() {
        List<Porto> lista = new ArrayList<>();
        String sql = "SELECT id_porto AS id, nome, pais, codigo FROM dias.PORTO";

        try (Connection conn = db.getConn();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) lista.add(mapear(rs));

        } catch (Exception e) {
            throw new DataAccessException("Erro ao aceder à base de dados (portos): " + e.getMessage(), e);
        }
        return lista;
    }

    public Porto buscarPorId(int id) {
        String sql = "SELECT id_porto AS id, nome, pais, codigo FROM dias.PORTO WHERE id_porto = ?";

        try (Connection conn = db.getConn();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }

        } catch (Exception e) {
            throw new DataAccessException("Erro ao aceder à base de dados (portos): " + e.getMessage(), e);
        }
        return null;
    }

    public void inserir(Porto p) {
        String sql = "INSERT INTO dias.PORTO (nome, pais, codigo) VALUES (?, ?, ?)";

        try (Connection conn = db.getConn();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, p.getNome());
            stmt.setString(2, p.getPais());
            stmt.setString(3, p.getCodigo());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) p.setId(rs.getInt(1));
            }

        } catch (Exception e) {
            throw new DataAccessException("Erro ao aceder à base de dados (portos): " + e.getMessage(), e);
        }
    }

    public void atualizar(Porto p) {
        String sql = "UPDATE dias.PORTO SET nome = ?, pais = ?, codigo = ? WHERE id_porto = ?";

        try (Connection conn = db.getConn();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, p.getNome());
            stmt.setString(2, p.getPais());
            stmt.setString(3, p.getCodigo());
            stmt.setInt(4, p.getId());
            stmt.executeUpdate();

        } catch (Exception e) {
            throw new DataAccessException("Erro ao aceder à base de dados (portos): " + e.getMessage(), e);
        }
    }

    public void eliminar(int id) {
        String sql = "DELETE FROM dias.PORTO WHERE id_porto = ?";

        try (Connection conn = db.getConn();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();

        } catch (Exception e) {
            throw new DataAccessException("Erro ao aceder à base de dados (portos): " + e.getMessage(), e);
        }
    }

    private Porto mapear(ResultSet rs) throws Exception {
        return new Porto(
                rs.getInt("id"),
                rs.getString("nome"),
                rs.getString("pais"),
                rs.getString("codigo")
        );
    }
}
