package dao;

import model.Carga;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CargaDAO {
    private Connection connection;
    private TipoCargaDAO tipoCargaDAO;
    public CargaDAO(Connection connection) {
        this.connection = connection;
        this.tipoCargaDAO = new TipoCargaDAO(connection);
    }
    public void inserir(Carga carga) throws SQLException {
        String sql = "INSERT INTO carga (designacao, id_tipo_carga, volume, peso, porto_carga, porto_descarga) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            //stmt.setString(1, carga.getDesignacao());
            stmt.setInt(2, carga.getTipoCarga().getId());
            stmt.setDouble(3, carga.getVolume());
            //stmt.setDouble(4, carga.getPeso());
            //stmt.setString(5, carga.getPortoCarga());
            //stmt.setString(6, carga.getPortoDescarga());
            stmt.executeUpdate();
        }
    }
}
