package dao;

import model.EstadoOperacional;
import model.EstadoViagem;
import model.Navio;
import model.Porto;
import model.TipoNavio;
import model.Viagem;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ViagemDAO {

    private final NavioDAO navioDAO = new NavioDAO();
    private final PortoDAO portoDAO = new PortoDAO();

    // Consulta unica com JOINs: viagem + navio + tipo de navio + portos numa so ida a BD.
    // (Antes, cada viagem buscava o navio, o tipo e 2 portos a parte -> N+1, lento.)
    private static final String BASE =
            "SELECT v.id_viagem, v.data_partida, v.data_chegada_prevista, v.estado, " +
            "n.id_navio, n.nome AS navio_nome, n.codigo_imo, n.capacidade_max, n.numero_tanques, " +
            "n.bandeira, n.ano_fabrico, n.estado_operacional, n.id_porto_atual, " +
            "tn.id_tipo_navio, tn.designacao AS tn_designacao, tn.max_cargas, " +
            "po.id_porto AS po_id, po.nome AS po_nome, po.pais AS po_pais, po.codigo AS po_codigo, " +
            "pd.id_porto AS pd_id, pd.nome AS pd_nome, pd.pais AS pd_pais, pd.codigo AS pd_codigo " +
            "FROM dias.VIAGEM v " +
            "JOIN dias.NAVIO n ON v.id_navio = n.id_navio " +
            "JOIN dias.TIPO_NAVIO tn ON n.id_tipo_navio = tn.id_tipo_navio " +
            "JOIN dias.PORTO po ON v.id_porto_origem = po.id_porto " +
            "JOIN dias.PORTO pd ON v.id_porto_destino = pd.id_porto";

    public List<Viagem> listarTodos() {
        List<Viagem> lista = new ArrayList<>();
        String sql = BASE;

        try (Connection conn = db.getConn();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) lista.add(mapear(rs));

        } catch (Exception e) {
            throw new DataAccessException("Erro ao aceder à base de dados (viagens): " + e.getMessage(), e);
        }
        return lista;
    }

    public List<Viagem> listarPorNavio(int idNavio) {
        List<Viagem> lista = new ArrayList<>();
        String sql = BASE + " WHERE v.id_navio = ?";

        try (Connection conn = db.getConn();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idNavio);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }

        } catch (Exception e) {
            throw new DataAccessException("Erro ao aceder à base de dados (viagens): " + e.getMessage(), e);
        }
        return lista;
    }

    public Viagem buscarPorId(int id) {
        String sql = BASE + " WHERE v.id_viagem = ?";

        try (Connection conn = db.getConn();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }

        } catch (Exception e) {
            throw new DataAccessException("Erro ao aceder à base de dados (viagens): " + e.getMessage(), e);
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
            throw new DataAccessException("Erro ao aceder à base de dados (viagens): " + e.getMessage(), e);
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
            throw new DataAccessException("Erro ao aceder à base de dados (viagens): " + e.getMessage(), e);
        }
    }

    public void eliminar(int id) {
        String sql = "DELETE FROM dias.VIAGEM WHERE id_viagem = ?";

        try (Connection conn = db.getConn();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();

        } catch (Exception e) {
            throw new DataAccessException("Erro ao aceder à base de dados (viagens): " + e.getMessage(), e);
        }
    }

    private Viagem mapear(ResultSet rs) throws Exception {
        TipoNavio tipo = new TipoNavio(rs.getInt("id_tipo_navio"), rs.getString("tn_designacao"),
                0, rs.getInt("max_cargas"));
        Integer idPortoAtual = rs.getObject("id_porto_atual") != null ? rs.getInt("id_porto_atual") : null;
        Navio navio = new Navio(rs.getInt("id_navio"), rs.getString("navio_nome"), rs.getString("codigo_imo"),
                tipo, rs.getDouble("capacidade_max"), rs.getInt("numero_tanques"),
                rs.getString("bandeira"), rs.getInt("ano_fabrico"),
                EstadoOperacional.valueOf(rs.getString("estado_operacional")), idPortoAtual);

        Porto origem  = new Porto(rs.getInt("po_id"), rs.getString("po_nome"),
                rs.getString("po_pais"), rs.getString("po_codigo"));
        Porto destino = new Porto(rs.getInt("pd_id"), rs.getString("pd_nome"),
                rs.getString("pd_pais"), rs.getString("pd_codigo"));

        Date chegadaSql = rs.getDate("data_chegada_prevista");
        LocalDate dataChegada = chegadaSql != null ? chegadaSql.toLocalDate() : null;

        return new Viagem(
                rs.getInt("id_viagem"),
                navio, origem, destino,
                rs.getDate("data_partida").toLocalDate(),
                dataChegada,
                EstadoViagem.valueOf(rs.getString("estado"))
        );
    }
}
