package dao;

import model.TipoCarga;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TipoCargaDAO {
    private Connection connection;
    public TipoCargaDAO(Connection connection) {
        this.connection = connection;
    }
    public TipoCarga buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM tipo_carga WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new TipoCarga(
                        rs.getInt("id"), rs.getString("nome"),
                        rs.getBoolean("inflamavel"), rs.getBoolean("corrosiva"), rs.getBoolean("toxica")
                );
            }
        }
        return null;
    }
}
