package dao;

import db.Db;
import model.Pedido;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO de la entidad Pedido.
 * Relación N:1 con Cliente.
 */
public class PedidoDAO {

    // ===============================
    // SQL
    // ===============================

    private static final String INSERT_SQL =
            "INSERT INTO pedido (id, cliente_id, fecha) VALUES (?, ?, ?)";

    private static final String SELECT_BY_ID_SQL =
            "SELECT id, cliente_id, fecha FROM lpedido WHERE id = ?";

    private static final String SELECT_ALL_SQL =
            "SELECT id, cliente_id, fecha FROM pedido ORDER BY id";

    // ===============================
    // CRUD BÁSICO
    // ===============================

    public void insert(Pedido p) throws SQLException {
        try (Connection con = Db.getConnection();
             PreparedStatement pst = con.prepareStatement(INSERT_SQL)) {

            pst.setInt(1, p.getId());
            pst.setInt(2, p.getClienteId());
            pst.setDate(3, Date.valueOf(p.getFecha()));

            pst.executeUpdate();
        }
    }

    public Pedido findById(int id) throws SQLException {
        try (Connection con = Db.getConnection();
             PreparedStatement pst = con.prepareStatement(SELECT_BY_ID_SQL)) {

            pst.setInt(1, id);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
                return null;
            }
        }
    }

    public List<Pedido> findAll() throws SQLException {
        List<Pedido> out = new ArrayList<>();

        try (Connection con = Db.getConnection();
             PreparedStatement pst = con.prepareStatement(SELECT_ALL_SQL);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                out.add(mapRow(rs));
            }
        }

        return out;
    }

    // ===============================
    // MAPEADOR
    // ===============================

    private Pedido mapRow(ResultSet rs) throws SQLException {
        return new Pedido(
                rs.getInt("id"),
                rs.getInt("cliente_id"),
                rs.getDate("fecha").toLocalDate()
        );
    }
}
