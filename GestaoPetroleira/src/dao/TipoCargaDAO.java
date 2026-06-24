package dao;

import model.TipoCarga;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class TipoCargaDAO {
    public List<TipoCarga> listarTodos() {
        List<TipoCarga> lista = new ArrayList<>();
        try (Connection c = db.getConn(); Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT id_tipo_carga AS id,designacao AS nome,inflamavel,corrosiva,toxica FROM dias.TIPO_CARGA")) {
            while (rs.next()) lista.add(mapear(rs));
        } catch (Exception e) { e.printStackTrace(); }
        return lista;
    }

    public TipoCarga buscarPorId(int id) {
        try (Connection c = db.getConn();
             PreparedStatement st = c.prepareStatement("SELECT id_tipo_carga AS id,designacao AS nome,inflamavel,corrosiva,toxica FROM dias.TIPO_CARGA WHERE id_tipo_carga=?")) {
            st.setInt(1, id);
            try (ResultSet rs = st.executeQuery()) { if (rs.next()) return mapear(rs); }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    public void inserir(TipoCarga t) {
        try (Connection c = db.getConn();
             PreparedStatement st = c.prepareStatement(
                 "INSERT INTO dias.TIPO_CARGA(designacao,inflamavel,corrosiva,toxica) VALUES(?,?,?,?)",
                 Statement.RETURN_GENERATED_KEYS)) {
            st.setString(1, t.getNome()); st.setBoolean(2, t.isInflamavel());
            st.setBoolean(3, t.isCorrosiva()); st.setBoolean(4, t.isToxica());
            st.executeUpdate();
            try (ResultSet rs = st.getGeneratedKeys()) { if (rs.next()) t.setId(rs.getInt(1)); }
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void eliminar(int id) {
        try (Connection c = db.getConn();
             PreparedStatement st = c.prepareStatement("DELETE FROM dias.TIPO_CARGA WHERE id_tipo_carga=?")) {
            st.setInt(1, id); st.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private TipoCarga mapear(ResultSet rs) throws Exception {
        return new TipoCarga(rs.getInt("id"), rs.getString("nome"),
                rs.getBoolean("inflamavel"), rs.getBoolean("corrosiva"), rs.getBoolean("toxica"));
    }
}
