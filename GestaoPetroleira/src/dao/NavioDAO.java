package dao;

import model.EstadoOperacional;
import model.Navio;
import model.TipoNavio;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NavioDAO {
    private final TipoNavioDAO tipoNavioDAO = new TipoNavioDAO();

    private static final String COLS =
        "id, nome, codigo_imo, id_tipo_navio, capacidade_maxima, num_compartimentos, " +
        "bandeira, ano_fabrico, estado_operacional, id_porto_atual";

    public List<Navio> listarTodos() {
        List<Navio> lista = new ArrayList<>();
        try (Connection c = db.getConn(); Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT " + COLS + " FROM dias.NAVIO")) {
            while (rs.next()) lista.add(mapear(rs));
        } catch (Exception e) { e.printStackTrace(); }
        return lista;
    }

    public Navio buscarPorId(int id) {
        try (Connection c = db.getConn();
             PreparedStatement st = c.prepareStatement("SELECT " + COLS + " FROM dias.NAVIO WHERE id=?")) {
            st.setInt(1, id);
            try (ResultSet rs = st.executeQuery()) { if (rs.next()) return mapear(rs); }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    public void inserir(Navio n) {
        String sql = "INSERT INTO dias.NAVIO(nome,codigo_imo,id_tipo_navio,capacidade_maxima," +
                     "num_compartimentos,bandeira,ano_fabrico,estado_operacional,id_porto_atual) VALUES(?,?,?,?,?,?,?,?,?)";
        try (Connection c = db.getConn();
             PreparedStatement st = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            st.setString(1, n.getNome()); st.setString(2, n.getCodigoImo());
            st.setInt(3, n.getTipoNavio().getId()); st.setDouble(4, n.getCapacidadeMaxima());
            st.setInt(5, n.getNumCompartimentos()); st.setString(6, n.getBandeira());
            st.setInt(7, n.getAnoFabrico()); st.setString(8, n.getEstadoOperacional().name());
            if (n.getIdPortoAtual() != null) st.setInt(9, n.getIdPortoAtual()); else st.setNull(9, Types.INTEGER);
            st.executeUpdate();
            try (ResultSet rs = st.getGeneratedKeys()) { if (rs.next()) n.setId(rs.getInt(1)); }
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void atualizar(Navio n) {
        String sql = "UPDATE dias.NAVIO SET nome=?,codigo_imo=?,id_tipo_navio=?,capacidade_maxima=?," +
                     "num_compartimentos=?,bandeira=?,ano_fabrico=?,estado_operacional=?,id_porto_atual=? WHERE id=?";
        try (Connection c = db.getConn(); PreparedStatement st = c.prepareStatement(sql)) {
            st.setString(1, n.getNome()); st.setString(2, n.getCodigoImo());
            st.setInt(3, n.getTipoNavio().getId()); st.setDouble(4, n.getCapacidadeMaxima());
            st.setInt(5, n.getNumCompartimentos()); st.setString(6, n.getBandeira());
            st.setInt(7, n.getAnoFabrico()); st.setString(8, n.getEstadoOperacional().name());
            if (n.getIdPortoAtual() != null) st.setInt(9, n.getIdPortoAtual()); else st.setNull(9, Types.INTEGER);
            st.setInt(10, n.getId()); st.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void eliminar(int id) {
        try (Connection c = db.getConn();
             PreparedStatement st = c.prepareStatement("DELETE FROM dias.NAVIO WHERE id=?")) {
            st.setInt(1, id); st.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private Navio mapear(ResultSet rs) throws Exception {
        TipoNavio tipo = tipoNavioDAO.buscarPorId(rs.getInt("id_tipo_navio"));
        Integer idPorto = rs.getObject("id_porto_atual") != null ? rs.getInt("id_porto_atual") : null;
        return new Navio(rs.getInt("id"), rs.getString("nome"), rs.getString("codigo_imo"),
                tipo, rs.getDouble("capacidade_maxima"), rs.getInt("num_compartimentos"),
                rs.getString("bandeira"), rs.getInt("ano_fabrico"),
                EstadoOperacional.valueOf(rs.getString("estado_operacional")), idPorto);
    }
}
