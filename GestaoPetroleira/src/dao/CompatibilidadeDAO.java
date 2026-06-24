package dao;

import model.Compatibilidade;
import model.TipoCarga;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CompatibilidadeDAO {
    private final TipoNavioDAO tipoNavioDAO = new TipoNavioDAO();
    private final TipoCargaDAO tipoCargaDAO = new TipoCargaDAO();

    public List<Compatibilidade> listarTodos() {
        List<Compatibilidade> lista = new ArrayList<>();
        try (Connection c = db.getConn(); Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT id_tipo_navio,id_tipo_carga FROM dias.COMPATIBILIDADE")) {
            while (rs.next()) lista.add(mapear(rs));
        } catch (Exception e) { e.printStackTrace(); }
        return lista;
    }

    public List<TipoCarga> listarCargasCompativeis(int idTipoNavio) {
        List<TipoCarga> lista = new ArrayList<>();
        String sql = "SELECT tc.id_tipo_carga AS id,tc.designacao AS nome,tc.inflamavel,tc.corrosiva,tc.toxica " +
                     "FROM dias.TIPO_CARGA tc JOIN dias.COMPATIBILIDADE c ON tc.id_tipo_carga=c.id_tipo_carga " +
                     "WHERE c.id_tipo_navio=?";
        try (Connection c = db.getConn(); PreparedStatement st = c.prepareStatement(sql)) {
            st.setInt(1, idTipoNavio);
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) lista.add(new TipoCarga(rs.getInt("id"), rs.getString("nome"),
                        rs.getBoolean("inflamavel"), rs.getBoolean("corrosiva"), rs.getBoolean("toxica")));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return lista;
    }

    public boolean existeCompatibilidade(int idTipoNavio, int idTipoCarga) {
        try (Connection c = db.getConn();
             PreparedStatement st = c.prepareStatement(
                 "SELECT COUNT(*) FROM dias.COMPATIBILIDADE WHERE id_tipo_navio=? AND id_tipo_carga=?")) {
            st.setInt(1, idTipoNavio); st.setInt(2, idTipoCarga);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    public void inserir(Compatibilidade comp) {
        try (Connection c = db.getConn();
             PreparedStatement st = c.prepareStatement(
                 "INSERT INTO dias.COMPATIBILIDADE(id_tipo_navio,id_tipo_carga) VALUES(?,?)")) {
            st.setInt(1, comp.getTipoNavio().getId()); st.setInt(2, comp.getTipoCarga().getId());
            st.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    /** Chave primaria composta: elimina pela combinacao tipo de navio + tipo de carga. */
    public void eliminar(int idTipoNavio, int idTipoCarga) {
        try (Connection c = db.getConn();
             PreparedStatement st = c.prepareStatement(
                 "DELETE FROM dias.COMPATIBILIDADE WHERE id_tipo_navio=? AND id_tipo_carga=?")) {
            st.setInt(1, idTipoNavio); st.setInt(2, idTipoCarga); st.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private Compatibilidade mapear(ResultSet rs) throws Exception {
        return new Compatibilidade(0,
                tipoNavioDAO.buscarPorId(rs.getInt("id_tipo_navio")),
                tipoCargaDAO.buscarPorId(rs.getInt("id_tipo_carga")));
    }
}
