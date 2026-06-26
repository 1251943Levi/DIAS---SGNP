package dao;

import model.Compatibilidade;
import model.TipoCarga;
import model.TipoNavio;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CompatibilidadeDAO {
    private final TipoNavioDAO tipoNavioDAO = new TipoNavioDAO();
    private final TipoCargaDAO tipoCargaDAO = new TipoCargaDAO();

    public List<Compatibilidade> listarTodos() {
        List<Compatibilidade> lista = new ArrayList<>();
        // JOIN unico: traz tipo de navio + tipo de carga de uma vez (evita N+1).
        String sql = "SELECT tn.id_tipo_navio, tn.designacao AS tn_designacao, tn.max_cargas, " +
                "tc.id_tipo_carga, tc.designacao AS tc_designacao, tc.inflamavel, tc.corrosiva, tc.toxica " +
                "FROM dias.COMPATIBILIDADE comp " +
                "JOIN dias.TIPO_NAVIO tn ON comp.id_tipo_navio = tn.id_tipo_navio " +
                "JOIN dias.TIPO_CARGA tc ON comp.id_tipo_carga = tc.id_tipo_carga";
        try (Connection c = db.getConn(); Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) lista.add(mapear(rs));
        } catch (Exception e) { throw new DataAccessException("Erro ao aceder à base de dados (compatibilidades): " + e.getMessage(), e); }
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
        } catch (Exception e) { throw new DataAccessException("Erro ao aceder à base de dados (compatibilidades): " + e.getMessage(), e); }
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
        } catch (Exception e) { throw new DataAccessException("Erro ao aceder à base de dados (compatibilidades): " + e.getMessage(), e); }
        return false;
    }

    public void inserir(Compatibilidade comp) {
        try (Connection c = db.getConn();
             PreparedStatement st = c.prepareStatement(
                 "INSERT INTO dias.COMPATIBILIDADE(id_tipo_navio,id_tipo_carga) VALUES(?,?)")) {
            st.setInt(1, comp.getTipoNavio().getId()); st.setInt(2, comp.getTipoCarga().getId());
            st.executeUpdate();
        } catch (Exception e) { throw new DataAccessException("Erro ao aceder à base de dados (compatibilidades): " + e.getMessage(), e); }
    }

    /** Chave primaria composta: elimina pela combinacao tipo de navio + tipo de carga. */
    public void eliminar(int idTipoNavio, int idTipoCarga) {
        try (Connection c = db.getConn();
             PreparedStatement st = c.prepareStatement(
                 "DELETE FROM dias.COMPATIBILIDADE WHERE id_tipo_navio=? AND id_tipo_carga=?")) {
            st.setInt(1, idTipoNavio); st.setInt(2, idTipoCarga); st.executeUpdate();
        } catch (Exception e) { throw new DataAccessException("Erro ao aceder à base de dados (compatibilidades): " + e.getMessage(), e); }
    }

    private Compatibilidade mapear(ResultSet rs) throws Exception {
        TipoNavio tn = new TipoNavio(rs.getInt("id_tipo_navio"), rs.getString("tn_designacao"),
                0, rs.getInt("max_cargas"));
        TipoCarga tc = new TipoCarga(rs.getInt("id_tipo_carga"), rs.getString("tc_designacao"),
                rs.getBoolean("inflamavel"), rs.getBoolean("corrosiva"), rs.getBoolean("toxica"));
        return new Compatibilidade(0, tn, tc);
    }
}
