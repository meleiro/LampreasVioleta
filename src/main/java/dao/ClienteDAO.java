package dao;
// Paquete donde vive esta clase. Normalmente 'dao' agrupa los Data Access Objects,
// clases dedicadas exclusivamente a hablar con la base de datos.

import db.Db;
// Clase que gestiona la obtención de conexiones JDBC (probablemente un método estático getConnection()).

import model.Cliente;
// Modelo/entidad Cliente. Representa una fila de la tabla 'cliente'.

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
// Imports necesarios para el uso del API JDBC de Java.

import java.util.ArrayList;
import java.util.List;
// Usamos listas dinámicas para devolver varios clientes cuando hacemos un SELECT *.

public class ClienteDAO {
    // Clase DAO que contiene la lógica de acceso a datos para la entidad Cliente.
    // Todo lo relacionado con INSERT, SELECT, UPDATE y DELETE de clientes se pone aquí.

    // ----------------------------------------------------------
    // SENTENCIAS SQL PREPARADAS COMO CONSTANTES
    // ----------------------------------------------------------

    private static final String INSERT_SQL =
            "INSERT INTO cliente (id, nombre, email) VALUES (?, ?, ?)";
    // Consulta SQL para insertar un cliente.
    // Usamos ? para parámetros → evita SQL injection y mejora rendimiento con sentencias preparadas.

    private static final String SELECT_BY_ID_SQL =
            "SELECT id, nombre, email FROM cliente WHERE id = ?";
    // Consulta SQL para buscar un cliente por su ID.

    private static final String SELECT_ALL_SQL =
            "SELECT id, nombre, email FROM cliente ORDER BY id";
    // Consulta SQL para obtener todos los clientes ordenados por id.


    private static final String SEARCH_SQL = """
                    SELECT id, nombre, email
                    FROM cliente
                    WHERE CAST(id AS TEXT) ILIKE ? 
                        OR nombre ILIKE ?  
                        OR email ILIKE ?
                    ORDER BY id                    
                    """;


    // ----------------------------------------------------------
    // MÉTODO: INSERTAR UN CLIENTE
    // ----------------------------------------------------------

    public void insert(Cliente c) throws SQLException {
        // Método público que inserta un cliente en la base de datos.
        // Recibe un objeto Cliente y lanza SQLException si algo sale mal.

        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(INSERT_SQL)) {

            // try-with-resources: la conexión y el PreparedStatement se cerrarán automáticamente
            // al final del bloque, aunque haya errores.

            ps.setInt(1, c.getId());         // Parámetro 1 → columna id
            ps.setString(2, c.getNombre());  // Parámetro 2 → columna nombre
            ps.setString(3, c.getEmail());   // Parámetro 3 → columna email

            ps.executeUpdate();
            // Ejecuta la sentencia. Como es un INSERT, no devuelve ResultSet.

            // Recuperar el ID generado por PostgreSQL
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int idGenerado = rs.getInt(1);
                    c.setId(idGenerado);  // lo guardamos en el objeto
                }
            }

        }
    }

    // Versión transaccional: usa una conexión que le pasa el servicio
    public void insert(Cliente c, Connection con) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(INSERT_SQL)) {
            ps.setInt(1, c.getId());
            ps.setString(2, c.getNombre());
            ps.setString(3, c.getEmail());
            ps.executeUpdate();
        }
    }

    // ----------------------------------------------------------
    // MÉTODO: BUSCAR CLIENTE POR ID
    // ----------------------------------------------------------

    public Cliente findById(int id) throws SQLException {
        // Devuelve el Cliente cuyo id coincida con el parámetro.
        // Si no existe, devuelve null.

        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_BY_ID_SQL)) {

            ps.setInt(1, id);  // Asignamos el id al parámetro ?

            try (ResultSet rs = ps.executeQuery()) {
                // executeQuery() devuelve un ResultSet ↔ una tabla virtual con las filas devueltas.

                if (rs.next()) {
                    // Si rs.next() = true → hay fila. Avanzamos a ella y leemos sus columnas.

                    return new Cliente(
                            rs.getInt("id"),          // Columna 'id'
                            rs.getString("nombre"),   // Columna 'nombre'
                            rs.getString("email")     // Columna 'email'
                    );
                }

                return null;
                // Si no hay resultado, devolvemos null para indicar "no encontrado".
            }
        }
    }


    // ----------------------------------------------------------
    // MÉTODO: LISTAR TODOS LOS CLIENTES
    // ----------------------------------------------------------

    public List<Cliente> findAll() throws SQLException {
        // Devuelve una lista con todos los clientes de la tabla.
        // Nunca devuelve null; si no hay datos, devuelve lista vacía.

        List<Cliente> out = new ArrayList<>();

        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_ALL_SQL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                // Iteramos por cada fila del ResultSet.
                // Cada fila se convierte en un objeto Cliente.

                Cliente c = new Cliente(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("email")
                );

                out.add(c);   // Añadimos el cliente a la lista.
            }
        }

        return out;   // Devolvemos la lista completa.
    }

    public List<Cliente> search(String filtro) throws SQLException {

        String patron = "%" + filtro + "%";

        try (Connection con = Db.getConnection();
           PreparedStatement pst = con.prepareStatement(SEARCH_SQL)) {
            pst.setString(1, patron);
            pst.setString(2, patron);
            pst.setString(3, patron);

            List<Cliente> out = new ArrayList<>();

            try(ResultSet rs = pst.executeQuery()){

                while (rs.next()){
                    out.add(mapRow(rs));
                }
            }
            return out;
        }
    }

    private Cliente mapRow(ResultSet rs) throws SQLException {

        Cliente c = new Cliente(
                rs.getInt("id"),
                rs.getString("nombre"),
                rs.getString("email")
        );

        return c;
    }


}