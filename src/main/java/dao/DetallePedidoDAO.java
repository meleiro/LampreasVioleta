package dao;

import db.Db;
import model.DetallePedido;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO de DetallePedido.
 * Tabla intermedia N:M entre Pedido y Producto.
 */
public class DetallePedidoDAO {

    // ===============================
    // SQL
    // ===============================

    private static final String INSERT_SQL =
            """
            INSERT INTO ldetalle_pedido
            (pedido_id, producto_id, cantidad, precio_unit)
            VALUES (?, ?, ?, ?)
            """;

    private static final String SELECT_ALL_SQL =
            """
            SELECT pedido_id, producto_id, cantidad, precio_unit
            FROM detalle_pedido
            ORDER BY pedido_id, producto_id
            """;

    private static final String SELECT_BY_PEDIDO_SQL =
            """
            SELECT pedido_id, producto_id, cantidad, precio_unit
            FROM detalle_pedido
            WHERE pedido_id = ?
            ORDER BY producto_id
            """;

    // ===============================
    // CRUD BÁSICO
    // ===============================

    public void insert(DetallePedido dp) throws SQLException {
        try (Connection con = Db.getConnection();
             PreparedStatement pst = con.prepareStatement(INSERT_SQL)) {

            pst.setInt(1, dp.getPedidoId());
            pst.setInt(2, dp.getProductoId());
            pst.setInt(3, dp.getCantidad());
            pst.setDouble(4, dp.getPrecioUnit());

            pst.executeUpdate();
        }
    }

    public List<DetallePedido> findAll() throws SQLException {
        List<DetallePedido> out = new ArrayList<>();

        try (Connection con = Db.getConnection();
             PreparedStatement pst = con.prepareStatement(SELECT_ALL_SQL);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                out.add(mapRow(rs));
            }
        }

        return out;
    }

    /**
     * Devuelve todas las líneas de un pedido concreto.
     * Muy útil para mostrar pedido + líneas juntas.
     */
    public List<DetallePedido> findByPedidoId(int pedidoId) throws SQLException {
        List<DetallePedido> out = new ArrayList<>();

        try (Connection con = Db.getConnection();
             PreparedStatement pst = con.prepareStatement(SELECT_BY_PEDIDO_SQL)) {

            pst.setInt(1, pedidoId);

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    out.add(mapRow(rs));
                }
            }
        }

        return out;
    }

    // ===============================
    // MAPEADOR
    // ===============================

    private DetallePedido mapRow(ResultSet rs) throws SQLException {
        return new DetallePedido(
                rs.getInt("pedido_id"),
                rs.getInt("producto_id"),
                rs.getInt("cantidad"),
                rs.getDouble("precio_unit")
        );
    }
}
