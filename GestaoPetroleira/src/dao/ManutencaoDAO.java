package dao;

import model.Manutencao;
import model.Navio;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ManutencaoDAO {

    private final NavioDAO navioDAO = new NavioDAO();

    public List<Manutencao> listarTodos() {
        List<Manutencao> lista = new ArrayList<>();
        String sql = "SELECT id_manutencao AS id, id_navio, data_inicio, data_fim, descricao FROM dias.MANUTENCAO";

        try (Connection conn = db.getConn();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) lista.add(mapear(rs));

        } catch (Exception e) {
            throw new DataAccessException("Erro ao aceder à base de dados (manutenções): " + e.getMessage(), e);
        }
        return lista;
    }

    public List<Manutencao> listarPorNavio(int idNavio) {
        List<Manutencao> lista = new ArrayList<>();
        String sql = "SELECT id_manutencao AS id, id_navio, data_inicio, data_fim, descricao FROM dias.MANUTENCAO WHERE id_navio = ?";

        try (Connection conn = db.getConn();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idNavio);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }

        } catch (Exception e) {
            throw new DataAccessException("Erro ao aceder à base de dados (manutenções): " + e.getMessage(), e);
        }
        return lista;
    }

    public Manutencao buscarPorId(int id) {
        String sql = "SELECT id_manutencao AS id, id_navio, data_inicio, data_fim, descricao FROM dias.MANUTENCAO WHERE id_manutencao = ?";

        try (Connection conn = db.getConn();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }

        } catch (Exception e) {
            throw new DataAccessException("Erro ao aceder à base de dados (manutenções): " + e.getMessage(), e);
        }
        return null;
    }

    public void inserir(Manutencao m) {
        String sql = "INSERT INTO dias.MANUTENCAO (id_navio, data_inicio, data_fim, descricao) VALUES (?, ?, ?, ?)";

        try (Connection conn = db.getConn();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, m.getNavio().getId());
            stmt.setDate(2, Date.valueOf(m.getDataInicio()));
            if (m.getDataFim() != null) stmt.setDate(3, Date.valueOf(m.getDataFim()));
            else stmt.setNull(3, Types.DATE);
            stmt.setString(4, m.getDescricao());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) m.setId(rs.getInt(1));
            }

        } catch (Exception e) {
            throw new DataAccessException("Erro ao aceder à base de dados (manutenções): " + e.getMessage(), e);
        }
    }

    public void atualizar(Manutencao m) {
        String sql = "UPDATE dias.MANUTENCAO SET id_navio = ?, data_inicio = ?, data_fim = ?, descricao = ? WHERE id_manutencao = ?";

        try (Connection conn = db.getConn();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, m.getNavio().getId());
            stmt.setDate(2, Date.valueOf(m.getDataInicio()));
            if (m.getDataFim() != null) stmt.setDate(3, Date.valueOf(m.getDataFim()));
            else stmt.setNull(3, Types.DATE);
            stmt.setString(4, m.getDescricao());
            stmt.setInt(5, m.getId());
            stmt.executeUpdate();

        } catch (Exception e) {
            throw new DataAccessException("Erro ao aceder à base de dados (manutenções): " + e.getMessage(), e);
        }
    }

    public void eliminar(int id) {
        String sql = "DELETE FROM dias.MANUTENCAO WHERE id_manutencao = ?";

        try (Connection conn = db.getConn();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();

        } catch (Exception e) {
            throw new DataAccessException("Erro ao aceder à base de dados (manutenções): " + e.getMessage(), e);
        }
    }

    private Manutencao mapear(ResultSet rs) throws Exception {
        Navio navio = navioDAO.buscarPorId(rs.getInt("id_navio"));
        Date dataFimSql = rs.getDate("data_fim");
        LocalDate dataFim = dataFimSql != null ? dataFimSql.toLocalDate() : null;

        return new Manutencao(
                rs.getInt("id"),
                navio,
                rs.getDate("data_inicio").toLocalDate(),
                dataFim,
                rs.getString("descricao")
        );
    }
}