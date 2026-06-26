package dao;

import model.Carga;
import model.Porto;
import model.TipoCarga;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CargaDAO {
    private final TipoCargaDAO tipoCargaDAO = new TipoCargaDAO();
    private final PortoDAO portoDAO = new PortoDAO();

    // Consulta unica com JOINs: traz a carga + tipo de carga + portos numa so ida a BD.
    // (Antes, cada carga fazia 3 consultas extra, cada uma a abrir nova ligacao -> N+1, muito lento.)
    private static final String BASE =
            "SELECT c.id_carga, c.designacao, c.volume, c.peso, c.id_viagem, " +
            "tc.id_tipo_carga, tc.designacao AS tc_designacao, tc.inflamavel, tc.corrosiva, tc.toxica, " +
            "pc.id_porto AS pc_id, pc.nome AS pc_nome, pc.pais AS pc_pais, pc.codigo AS pc_codigo, " +
            "pd.id_porto AS pd_id, pd.nome AS pd_nome, pd.pais AS pd_pais, pd.codigo AS pd_codigo " +
            "FROM dias.CARGA c " +
            "JOIN dias.TIPO_CARGA tc ON c.id_tipo_carga = tc.id_tipo_carga " +
            "JOIN dias.PORTO pc ON c.id_porto_carga = pc.id_porto " +
            "JOIN dias.PORTO pd ON c.id_porto_descarga = pd.id_porto";

    public List<Carga> listarTodos() {
        List<Carga> lista = new ArrayList<>();
        try (Connection c = db.getConn(); Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(BASE)) {
            while (rs.next()) lista.add(mapear(rs));
        } catch (Exception e) { throw new DataAccessException("Erro ao aceder à base de dados (cargas): " + e.getMessage(), e); }
        return lista;
    }

    /** Cargas associadas a uma viagem (CARGA.id_viagem). */
    public List<Carga> listarPorViagem(int idViagem) {
        List<Carga> lista = new ArrayList<>();
        try (Connection c = db.getConn(); PreparedStatement st = c.prepareStatement(BASE + " WHERE c.id_viagem=?")) {
            st.setInt(1, idViagem);
            try (ResultSet rs = st.executeQuery()) { while (rs.next()) lista.add(mapear(rs)); }
        } catch (Exception e) { throw new DataAccessException("Erro ao aceder à base de dados (cargas): " + e.getMessage(), e); }
        return lista;
    }

    public Carga buscarPorId(int id) {
        try (Connection c = db.getConn();
             PreparedStatement st = c.prepareStatement(BASE + " WHERE c.id_carga=?")) {
            st.setInt(1, id);
            try (ResultSet rs = st.executeQuery()) { if (rs.next()) return mapear(rs); }
        } catch (Exception e) { throw new DataAccessException("Erro ao aceder à base de dados (cargas): " + e.getMessage(), e); }
        return null;
    }

    public void inserir(Carga carga) {
        String sql = "INSERT INTO dias.CARGA(designacao,id_tipo_carga,volume,peso,id_porto_carga,id_porto_descarga) VALUES(?,?,?,?,?,?)";
        try (Connection c = db.getConn();
             PreparedStatement st = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            st.setString(1, carga.getDesignacao()); st.setInt(2, carga.getTipoCarga().getId());
            st.setDouble(3, carga.getVolume()); st.setDouble(4, carga.getPeso());
            st.setInt(5, carga.getPortoCarga().getId()); st.setInt(6, carga.getPortoDescarga().getId());
            st.executeUpdate();
            try (ResultSet rs = st.getGeneratedKeys()) { if (rs.next()) carga.setId(rs.getInt(1)); }
        } catch (Exception e) { throw new DataAccessException("Erro ao aceder à base de dados (cargas): " + e.getMessage(), e); }
    }

    public void atualizar(Carga carga) {
        String sql = "UPDATE dias.CARGA SET designacao=?,id_tipo_carga=?,volume=?,peso=?,id_porto_carga=?,id_porto_descarga=? WHERE id_carga=?";
        try (Connection c = db.getConn(); PreparedStatement st = c.prepareStatement(sql)) {
            st.setString(1, carga.getDesignacao()); st.setInt(2, carga.getTipoCarga().getId());
            st.setDouble(3, carga.getVolume()); st.setDouble(4, carga.getPeso());
            st.setInt(5, carga.getPortoCarga().getId()); st.setInt(6, carga.getPortoDescarga().getId());
            st.setInt(7, carga.getId()); st.executeUpdate();
        } catch (Exception e) { throw new DataAccessException("Erro ao aceder à base de dados (cargas): " + e.getMessage(), e); }
    }

    public void eliminar(int id) {
        try (Connection c = db.getConn();
             PreparedStatement st = c.prepareStatement("DELETE FROM dias.CARGA WHERE id_carga=?")) {
            st.setInt(1, id); st.executeUpdate();
        } catch (Exception e) { throw new DataAccessException("Erro ao aceder à base de dados (cargas): " + e.getMessage(), e); }
    }

    /** Associa a carga a uma viagem (define CARGA.id_viagem). */
    public void associarAViagem(int idViagem, int idCarga) {
        try (Connection c = db.getConn();
             PreparedStatement st = c.prepareStatement("UPDATE dias.CARGA SET id_viagem=? WHERE id_carga=?")) {
            st.setInt(1, idViagem); st.setInt(2, idCarga); st.executeUpdate();
        } catch (Exception e) { throw new DataAccessException("Erro ao aceder à base de dados (cargas): " + e.getMessage(), e); }
    }

    /** Remove a associacao da carga a viagem (CARGA.id_viagem = NULL). */
    public void desassociarDaViagem(int idViagem, int idCarga) {
        try (Connection c = db.getConn();
             PreparedStatement st = c.prepareStatement("UPDATE dias.CARGA SET id_viagem=NULL WHERE id_carga=? AND id_viagem=?")) {
            st.setInt(1, idCarga); st.setInt(2, idViagem); st.executeUpdate();
        } catch (Exception e) { throw new DataAccessException("Erro ao aceder à base de dados (cargas): " + e.getMessage(), e); }
    }

    public int contarCargasPorViagem(int idViagem) {
        try (Connection c = db.getConn();
             PreparedStatement st = c.prepareStatement("SELECT COUNT(*) FROM dias.CARGA WHERE id_viagem=?")) {
            st.setInt(1, idViagem);
            try (ResultSet rs = st.executeQuery()) { if (rs.next()) return rs.getInt(1); }
        } catch (Exception e) { throw new DataAccessException("Erro ao aceder à base de dados (cargas): " + e.getMessage(), e); }
        return 0;
    }

    public double pesotalPorViagem(int idViagem) {
        String sql = "SELECT ISNULL(SUM(peso),0) FROM dias.CARGA WHERE id_viagem=?";
        try (Connection c = db.getConn(); PreparedStatement st = c.prepareStatement(sql)) {
            st.setInt(1, idViagem);
            try (ResultSet rs = st.executeQuery()) { if (rs.next()) return rs.getDouble(1); }
        } catch (Exception e) { throw new DataAccessException("Erro ao aceder à base de dados (cargas): " + e.getMessage(), e); }
        return 0;
    }

    private Carga mapear(ResultSet rs) throws Exception {
        TipoCarga tc = new TipoCarga(rs.getInt("id_tipo_carga"), rs.getString("tc_designacao"),
                rs.getBoolean("inflamavel"), rs.getBoolean("corrosiva"), rs.getBoolean("toxica"));
        Porto pc = new Porto(rs.getInt("pc_id"), rs.getString("pc_nome"),
                rs.getString("pc_pais"), rs.getString("pc_codigo"));
        Porto pd = new Porto(rs.getInt("pd_id"), rs.getString("pd_nome"),
                rs.getString("pd_pais"), rs.getString("pd_codigo"));
        return new Carga(rs.getInt("id_carga"), rs.getString("designacao"), tc,
                rs.getDouble("volume"), rs.getDouble("peso"), pc, pd);
    }
}
