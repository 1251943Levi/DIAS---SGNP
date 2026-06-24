package dao;

import model.EstadoViagem;
import model.Navio;
import model.Porto;
import model.Viagem;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ViagemDAO {

    private final NavioDAO navioDAO = new NavioDAO();
    private final PortoDAO portoDAO = new PortoDAO();

    private static final String COLS =
            "id_viagem AS id, id_navio, id_porto_origem, id_porto_destino, " +
            "data_partida, data_chegada_prevista AS data_chegada, estado";

    public List<Viagem> listarTodos() {
        List<Viagem> lista = new ArrayList<>();
        String sql = "SELECT " + COLS + " FROM dias.VIAGEM";

        try (Connection conn = db.getConn();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) lista.add(mapear(rs));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }

    public List<Viagem> listarPorNavio(int idNavio) {
        List<Viagem> lista = new ArrayList<>();
        String sql = "SELECT " + COLS + " FROM dias.VIAGEM WHERE id_navio = ?";

        try (Connection conn = db.getConn();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idNavio);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }

    public Viagem buscarPorId(int id) {
        String sql = "SELECT " + COLS + " FROM dias.VIAGEM WHERE id_viagem = ?";

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

    public void inserir(Viagem v) {
        String sql = "INSERT INTO dias.VIAGEM (id_navio, id_porto_origem, id_porto_destino, " +
                "data_partida, data_chegada_prevista, estado) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = db.getConn();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, v.getNavio().getId());
            stmt.setInt(2, v.getPortoOrigem().getId());
            stmt.setInt(3, v.getPortoDestino().getId());
            stmt.setDate(4, Date.valueOf(v.getDataPartida()));
            if (v.getDataChegada() != null) stmt.setDate(5, Date.valueOf(v.getDataChegada()));
            else stmt.setNull(5, Types.DATE);
            stmt.setString(6, v.getEstado().name());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) v.setId(rs.getInt(1));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void atualizar(Viagem v) {
        String sql = "UPDATE dias.VIAGEM SET id_navio = ?, id_porto_origem = ?, id_porto_destino = ?, " +
                "data_partida = ?, data_chegada_prevista = ?, estado = ? WHERE id_viagem = ?";

        try (Connection conn = db.getConn();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, v.getNavio().getId());
            stmt.setInt(2, v.getPortoOrigem().getId());
            stmt.setInt(3, v.getPortoDestino().getId());
            stmt.setDate(4, Date.valueOf(v.getDataPartida()));
            if (v.getDataChegada() != null) stmt.setDate(5, Date.valueOf(v.getDataChegada()));
            else stmt.setNull(5, Types.DATE);
            stmt.setString(6, v.getEstado().name());
            stmt.setInt(7, v.getId());
            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void eliminar(int id) {
        String sql = "DELETE FROM dias.VIAGEM WHERE id_viagem = ?";

        try (Connection conn = db.getConn();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Viagem mapear(ResultSet rs) throws Exception {
        Navio navio  = navioDAO.buscarPorId(rs.getInt("id_navio"));
        Porto origem = portoDAO.buscarPorId(rs.getInt("id_porto_origem"));
        Porto destino = portoDAO.buscarPorId(rs.getInt("id_porto_destino"));

        Date chegadaSql = rs.getDate("data_chegada");
        LocalDate dataChegada = chegadaSql != null ? chegadaSql.toLocalDate() : null;

        return new Viagem(
                rs.getInt("id"),
                navio,
                origem,
                destino,
                rs.getDate("data_partida").toLocalDate(),
                dataChegada,
                EstadoViagem.valueOf(rs.getString("estado"))
        );
    }
}
