package dao;

import model.EstadoOperacional;
import model.Navio;
import model.TipoNavio;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NavioDAO {

    private final TipoNavioDAO tipoNavioDAO = new TipoNavioDAO();

    public List<Navio> listarTodos() {
        List<Navio> lista = new ArrayList<>();
        String sql = "SELECT id, nome, codigo_imo, id_tipo_navio, estado_operacional, id_porto_atual FROM dias.NAVIO";

        try (Connection conn = db.getConn();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) lista.add(mapear(rs));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }

    public Navio buscarPorId(int id) {
        String sql = "SELECT id, nome, codigo_imo, id_tipo_navio, estado_operacional, id_porto_atual FROM dias.NAVIO WHERE id = ?";

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

    public void inserir(Navio n) {
        String sql = "INSERT INTO dias.NAVIO (nome, codigo_imo, id_tipo_navio, estado_operacional, id_porto_atual) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = db.getConn();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, n.getNome());
            stmt.setString(2, n.getCodigoImo());
            stmt.setInt(3, n.getTipoNavio().getId());
            stmt.setString(4, n.getEstadoOperacional().name());
            if (n.getIdPortoAtual() != null) stmt.setInt(5, n.getIdPortoAtual());
            else stmt.setNull(5, Types.INTEGER);
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) n.setId(rs.getInt(1));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void atualizar(Navio n) {
        String sql = "UPDATE dias.NAVIO SET nome = ?, codigo_imo = ?, id_tipo_navio = ?, estado_operacional = ?, id_porto_atual = ? WHERE id = ?";

        try (Connection conn = db.getConn();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, n.getNome());
            stmt.setString(2, n.getCodigoImo());
            stmt.setInt(3, n.getTipoNavio().getId());
            stmt.setString(4, n.getEstadoOperacional().name());
            if (n.getIdPortoAtual() != null) stmt.setInt(5, n.getIdPortoAtual());
            else stmt.setNull(5, Types.INTEGER);
            stmt.setInt(6, n.getId());
            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void eliminar(int id) {
        String sql = "DELETE FROM dias.NAVIO WHERE id = ?";

        try (Connection conn = db.getConn();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Navio mapear(ResultSet rs) throws Exception {
        TipoNavio tipo = tipoNavioDAO.buscarPorId(rs.getInt("id_tipo_navio"));
        Integer idPorto = rs.getObject("id_porto_atual") != null ? rs.getInt("id_porto_atual") : null;

        return new Navio(
                rs.getInt("id"),
                rs.getString("nome"),
                rs.getString("codigo_imo"),
                tipo,
                EstadoOperacional.valueOf(rs.getString("estado_operacional")),
                idPorto
        );
    }
}