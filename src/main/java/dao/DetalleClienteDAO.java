package dao;

import db.Db;
import model.DetalleCliente;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para la tabla detalle_cliente.
 *
 * Esta versión NO usa transacciones ni versiones con Connection.
 * Es equivalente al ClienteDAO básico que ya tienes.
 */
public class DetalleClienteDAO {

    // =========================================================================
    //  SQL COMO CONSTANTES (buenas prácticas)
    // =========================================================================

    /** Consulta para insertar un detalle asociado a un cliente. */
    private static final String INSERT_SQL = """
            INSERT INTO detalle_cliente (id, direccion, telefono, notas)
            VALUES (?, ?, ?, ?)
            """;

    /** Consulta para obtener un detalle por su id (que coincide con id cliente). */
    private static final String SELECT_BY_ID_SQL = """
            SELECT id, direccion, telefono, notas
            FROM detalle_cliente
            WHERE id = ?
            """;

    /** Consulta para listar todos los detalles (útil para debugging). */
    private static final String SELECT_ALL_SQL = """
            SELECT id, direccion, telefono, notas
            FROM detalle_cliente
            ORDER BY id
            """;

    /** Consulta para actualizar los datos del detalle. */
    private static final String UPDATE_SQL = """
            UPDATE detalle_cliente
            SET direccion = ?, telefono = ?, notas = ?
            WHERE id = ?
            """;

    /** Consulta para borrar un detalle por ID. */
    private static final String DELETE_SQL = """
            DELETE FROM detalle_cliente
            WHERE id = ?
            """;

    // =========================================================================
    //  MÉTODOS CRUD BÁSICOS
    // =========================================================================

    /**
     * Inserta un detalle nuevo.
     * IMPORTANTE: el id debe coincidir con un cliente existente (relación 1:1).
     */
    public void insert(DetalleCliente d) throws SQLException {
        try (Connection con = Db.getConnection();
             PreparedStatement pst = con.prepareStatement(INSERT_SQL)) {

            pst.setInt(1, d.getId());
            pst.setString(2, d.getDireccion());
            String tel = d.getTelefono();
            if (tel == null || tel.isBlank()) {
                pst.setNull(3, Types.VARCHAR);   // ← fuerza NULL → rompe NOT NULL
            } else {
                pst.setString(3, tel.trim());
            }
            pst.setString(4, d.getNotas());

            pst.executeUpdate();
        }
    }

    public void insert(DetalleCliente d, Connection con) throws SQLException {

        String sql = "INSERT INTO detalle_cliente (id, direccion, telefono, notas) VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, d.getId());
            ps.setString(2, d.getDireccion());

            // ✅ AQUÍ ESTÁ EL CAMBIO IMPORTANTE
            String tel = d.getTelefono();
            if (tel == null || tel.isBlank()) {
                ps.setNull(3, Types.VARCHAR);   // ← fuerza NULL → rompe NOT NULL
            } else {
                ps.setString(3, tel.trim());
            }

            ps.setString(4, d.getNotas());

            ps.executeUpdate();
        }
    }


    /**
     * Obtiene un detalle según el ID (clave primaria).
     * Devuelve null si no existe.
     */
    public DetalleCliente findById(int id) throws SQLException {
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

    /**
     * Lista todos los detalles cliente.
     */
    public List<DetalleCliente> findAll() throws SQLException {
        List<DetalleCliente> out = new ArrayList<>();

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
     * Actualiza los datos del detalle.
     * Si id no existe, devuelve 0.
     */
    public int update(DetalleCliente d) throws SQLException {
        try (Connection con = Db.getConnection();
             PreparedStatement pst = con.prepareStatement(UPDATE_SQL)) {

            pst.setString(1, d.getDireccion());
            pst.setString(2, d.getTelefono());
            pst.setString(3, d.getNotas());
            pst.setInt(4, d.getId());

            return pst.executeUpdate(); // número de filas afectadas
        }
    }

    /**
     * Borra un detalle concreto.
     */
    public int deleteById(int id) throws SQLException {
        try (Connection con = Db.getConnection();
             PreparedStatement pst = con.prepareStatement(DELETE_SQL)) {

            pst.setInt(1, id);
            return pst.executeUpdate();
        }
    }

    // =========================================================================
    //  MAPEO ResultSet → DetalleCliente (buenas prácticas)
    // =========================================================================

    /**
     * Convierte una fila de ResultSet en un objeto DetalleCliente.
     */
    private DetalleCliente mapRow(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String direccion = rs.getString("direccion");
        String telefono = rs.getString("telefono");
        String notas = rs.getString("notas");

        return new DetalleCliente(id, direccion, telefono, notas);
    }
}
