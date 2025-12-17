package dao;

import db.Db;
import model.Producto;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO de la entidad Producto.
 * Encapsula todo el acceso JDBC a la tabla producto.
 */
public class ProductoDAO {

    // ===============================
    // SQL
    // ===============================

    private static final String INSERT_SQL =
            "INSERT INTO producto (id, nombre, precio) VALUES (?, ?, ?)";

    private static final String SELECT_BY_ID_SQL =
            "SELECT id, nombre, precio FROM producto WHERE id = ?";

    private static final String SELECT_ALL_SQL =
            "SELECT id, nombre, precio FROM producto ORDER BY id";

    // ===============================
    // CRUD BÁSICO
    // ===============================

    public void insert(Producto p) throws SQLException {
        try (Connection con = Db.getConnection();
             PreparedStatement pst = con.prepareStatement(INSERT_SQL)) {

            pst.setInt(1, p.getId());
            pst.setString(2, p.getNombre());
            pst.setDouble(3, p.getPrecio());

            pst.executeUpdate();
        }
    }

    public Producto findById(int id) throws SQLException {
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

    public List<Producto> findAll() throws SQLException {
        List<Producto> out = new ArrayList<>();

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

    private Producto mapRow(ResultSet rs) throws SQLException {
        return new Producto(
                rs.getInt("id"),
                rs.getString("nombre"),
                rs.getDouble("precio")
        );
    }
}
