package dao;

import model.TipoNavio;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TipoNavioDAO {

    // 0 AS capacidade_maxima: o tipo de navio deixou de guardar capacidade
    // (passou a ter 'descricao'); o valor fica a 0 para o modelo continuar valido.
    private static final String COLS =
            "id_tipo_navio AS id, designacao AS nome, 0 AS capacidade_maxima, max_cargas";

    public List<TipoNavio> listarTodos() {
        List<TipoNavio> lista = new ArrayList<>();
        String sql = "SELECT " + COLS + " FROM dias.TIPO_NAVIO";

        try (Connection conn = db.getConn();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) lista.add(mapear(rs));

        } catch (Exception e) {
            throw new DataAccessException("Erro ao aceder à base de dados (tipos de navio): " + e.getMessage(), e);
        }
        return lista;
    }

    public TipoNavio buscarPorId(int id) {
        String sql = "SELECT " + COLS + " FROM dias.TIPO_NAVIO WHERE id_tipo_navio = ?";

        try (Connection conn = db.getConn();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }

        } catch (Exception e) {
            throw new DataAccessException("Erro ao aceder à base de dados (tipos de navio): " + e.getMessage(), e);
        }
        return null;
    }

    public void inserir(TipoNavio t) {
        String sql = "INSERT INTO dias.TIPO_NAVIO (designacao, max_cargas) VALUES (?, ?)";

        try (Connection conn = db.getConn();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, t.getNome());
            stmt.setInt(2, t.getMaxCargas());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) t.setId(rs.getInt(1));
            }

        } catch (Exception e) {
            throw new DataAccessException("Erro ao aceder à base de dados (tipos de navio): " + e.getMessage(), e);
        }
    }

    public void atualizar(TipoNavio t) {
        String sql = "UPDATE dias.TIPO_NAVIO SET designacao = ?, max_cargas = ? WHERE id_tipo_navio = ?";

        try (Connection conn = db.getConn();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, t.getNome());
            stmt.setInt(2, t.getMaxCargas());
            stmt.setInt(3, t.getId());
            stmt.executeUpdate();

        } catch (Exception e) {
            throw new DataAccessException("Erro ao aceder à base de dados (tipos de navio): " + e.getMessage(), e);
        }
    }

    public void eliminar(int id) {
        String sql = "DELETE FROM dias.TIPO_NAVIO WHERE id_tipo_navio = ?";

        try (Connection conn = db.getConn();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();

        } catch (Exception e) {
            throw new DataAccessException("Erro ao aceder à base de dados (tipos de navio): " + e.getMessage(), e);
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
