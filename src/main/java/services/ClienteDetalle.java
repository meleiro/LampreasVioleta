package services;

import dao.ClienteDAO;
import dao.DetalleClienteDAO;
import db.Db;
import model.Cliente;
import model.DetalleCliente;

import java.sql.Connection;
import java.sql.SQLException;

public class ClienteDetalle {

    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final DetalleClienteDAO detalleClienteDAO = new DetalleClienteDAO();

    /**
     * Guarda un cliente y su detalle en una única transacción.
     * Si algo falla, se hace rollback y no se inserta nada.
     */
// services/ClienteDetalle.java
    public void guardarClienteCompleto(Cliente c, DetalleCliente d) throws SQLException {
        try (Connection con = Db.getConnection()) {
            con.setAutoCommit(false);

            try {
                // Aquí tus insert(c, con) / insert(d, con) que USAN ESA misma conexión
                clienteDAO.insert(c, con);
                detalleClienteDAO.insert(d, con);

                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }


}
