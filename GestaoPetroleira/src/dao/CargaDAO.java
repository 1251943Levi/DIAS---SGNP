package dao;

import model.Carga;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CargaDAO {
    private final TipoCargaDAO tipoCargaDAO = new TipoCargaDAO();
    private final PortoDAO portoDAO = new PortoDAO();

    // Aliases para o mapear() manter os nomes do modelo.
    private static final String COLS =
            "id_carga AS id, designacao, id_tipo_carga, volume, peso, id_porto_carga, id_porto_descarga";

    public List<Carga> listarTodos() {
        List<Carga> lista = new ArrayList<>();
        try (Connection c = db.getConn(); Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT " + COLS + " FROM dias.CARGA")) {
            while (rs.next()) lista.add(mapear(rs));
        } catch (Exception e) { e.printStackTrace(); }
        return lista;
    }

    /** Cargas associadas a uma viagem (CARGA.id_viagem). */
    public List<Carga> listarPorViagem(int idViagem) {
        List<Carga> lista = new ArrayList<>();
        String sql = "SELECT " + COLS + " FROM dias.CARGA WHERE id_viagem=?";
        try (Connection c = db.getConn(); PreparedStatement st = c.prepareStatement(sql)) {
            st.setInt(1, idViagem);
            try (ResultSet rs = st.executeQuery()) { while (rs.next()) lista.add(mapear(rs)); }
        } catch (Exception e) { e.printStackTrace(); }
        return lista;
    }

    public Carga buscarPorId(int id) {
        try (Connection c = db.getConn();
             PreparedStatement st = c.prepareStatement("SELECT " + COLS + " FROM dias.CARGA WHERE id_carga=?")) {
            st.setInt(1, id);
            try (ResultSet rs = st.executeQuery()) { if (rs.next()) return mapear(rs); }
        } catch (Exception e) { e.printStackTrace(); }
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
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void atualizar(Carga carga) {
        String sql = "UPDATE dias.CARGA SET designacao=?,id_tipo_carga=?,volume=?,peso=?,id_porto_carga=?,id_porto_descarga=? WHERE id_carga=?";
        try (Connection c = db.getConn(); PreparedStatement st = c.prepareStatement(sql)) {
            st.setString(1, carga.getDesignacao()); st.setInt(2, carga.getTipoCarga().getId());
            st.setDouble(3, carga.getVolume()); st.setDouble(4, carga.getPeso());
            st.setInt(5, carga.getPortoCarga().getId()); st.setInt(6, carga.getPortoDescarga().getId());
            st.setInt(7, carga.getId()); st.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void eliminar(int id) {
        try (Connection c = db.getConn();
             PreparedStatement st = c.prepareStatement("DELETE FROM dias.CARGA WHERE id_carga=?")) {
            st.setInt(1, id); st.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    /** Associa a carga a uma viagem (define CARGA.id_viagem). */
    public void associarAViagem(int idViagem, int idCarga) {
        try (Connection c = db.getConn();
             PreparedStatement st = c.prepareStatement("UPDATE dias.CARGA SET id_viagem=? WHERE id_carga=?")) {
            st.setInt(1, idViagem); st.setInt(2, idCarga); st.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    /** Remove a associacao da carga a viagem (CARGA.id_viagem = NULL). */
    public void desassociarDaViagem(int idViagem, int idCarga) {
        try (Connection c = db.getConn();
             PreparedStatement st = c.prepareStatement("UPDATE dias.CARGA SET id_viagem=NULL WHERE id_carga=? AND id_viagem=?")) {
            st.setInt(1, idCarga); st.setInt(2, idViagem); st.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public int contarCargasPorViagem(int idViagem) {
        try (Connection c = db.getConn();
             PreparedStatement st = c.prepareStatement("SELECT COUNT(*) FROM dias.CARGA WHERE id_viagem=?")) {
            st.setInt(1, idViagem);
            try (ResultSet rs = st.executeQuery()) { if (rs.next()) return rs.getInt(1); }
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }

    public double pesotalPorViagem(int idViagem) {
        String sql = "SELECT ISNULL(SUM(peso),0) FROM dias.CARGA WHERE id_viagem=?";
        try (Connection c = db.getConn(); PreparedStatement st = c.prepareStatement(sql)) {
            st.setInt(1, idViagem);
            try (ResultSet rs = st.executeQuery()) { if (rs.next()) return rs.getDouble(1); }
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }

    private Carga mapear(ResultSet rs) throws Exception {
        return new Carga(rs.getInt("id"), rs.getString("designacao"),
                tipoCargaDAO.buscarPorId(rs.getInt("id_tipo_carga")),
                rs.getDouble("volume"), rs.getDouble("peso"),
                portoDAO.buscarPorId(rs.getInt("id_porto_carga")),
                portoDAO.buscarPorId(rs.getInt("id_porto_descarga")));
    }
}
