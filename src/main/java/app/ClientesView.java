package app;

import dao.ClienteDAO;
import model.Cliente;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Vista JavaFX para gestionar clientes.
 *
 * IMPORTANTE:
 *  - Ahora mismo el DAO solo tiene: insert, findById, findAll.
 *  - Los botones de Buscar / Modificar / Borrar ya existen en la interfaz,
 *    pero parte de la lógica está marcada como TODO para implementarla
 *    cuando se añadan los métodos update, delete, search en ClienteDAO.
 */
public class ClientesView {

    private final BorderPane root = new BorderPane();

    // Tabla y datos
    private final TableView<Cliente> tabla = new TableView<>();
    private final ObservableList<Cliente> datos = FXCollections.observableArrayList();

    // Campos de formulario
    private final TextField txtId = new TextField();
    private final TextField txtNombre = new TextField();
    private final TextField txtEmail = new TextField();

    // Botones CRUD
    private final Button btnNuevo = new Button("Nuevo");
    private final Button btnGuardar = new Button("Guardar");
    private final Button btnBorrar = new Button("Borrar");
    private final Button btnRecargar = new Button("Recargar");

    // Búsqueda
    private final TextField txtBuscar = new TextField();
    private final Button btnBuscar = new Button("Buscar");
    private final Button btnLimpiarBusqueda = new Button("Limpiar");

    // DAO (acceso a BD)
    private final ClienteDAO clienteDAO = new ClienteDAO();

    public ClientesView() {
        configurarTabla();
        configurarFormulario();
        configurarEventos();
        recargarDatos(); // al iniciar la vista cargamos los clientes
    }

    public Parent getRoot() {
        return root;
    }

    /* =========================================================
       CONFIGURACIÓN INTERFAZ
       ========================================================= */

    private void configurarTabla() {
        TableColumn<Cliente, Number> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(c ->
                new javafx.beans.property.SimpleIntegerProperty(c.getValue().getId()));

        TableColumn<Cliente, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue().getNombre()));

        TableColumn<Cliente, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue().getEmail()));

        tabla.getColumns().addAll(colId, colNombre, colEmail);
        tabla.setItems(datos);

        root.setCenter(tabla);
    }

    private void configurarFormulario() {
        GridPane form = new GridPane();
        form.setPadding(new Insets(10));
        form.setHgap(10);
        form.setVgap(10);

        txtId.setPromptText("ID (entero)");
        txtNombre.setPromptText("Nombre");
        txtEmail.setPromptText("Email");

        form.add(new Label("ID:"), 0, 0);
        form.add(txtId, 1, 0);
        form.add(new Label("Nombre:"), 0, 1);
        form.add(txtNombre, 1, 1);
        form.add(new Label("Email:"), 0, 2);
        form.add(txtEmail, 1, 2);

        // Zona botones CRUD
        HBox botonesCrud = new HBox(10, btnNuevo, btnGuardar, btnBorrar, btnRecargar);
        botonesCrud.setPadding(new Insets(10, 0, 0, 0));

        // Zona de búsqueda
        HBox zonaBusqueda = new HBox(10,
                new Label("Buscar:"), txtBuscar, btnBuscar, btnLimpiarBusqueda);
        zonaBusqueda.setPadding(new Insets(10, 0, 10, 0));

        BorderPane bottom = new BorderPane();
        bottom.setTop(zonaBusqueda);
        bottom.setCenter(form);
        bottom.setBottom(botonesCrud);

        root.setBottom(bottom);
    }

    private void configurarEventos() {
        // Cuando seleccionamos una fila en la tabla, pasamos los datos al formulario
        tabla.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                txtId.setText(String.valueOf(newSel.getId()));
                txtNombre.setText(newSel.getNombre());
                txtEmail.setText(newSel.getEmail());
                txtId.setDisable(true); // al editar, de momento, no dejamos cambiar el ID
            }
        });

        btnNuevo.setOnAction(e -> limpiarFormulario());

        btnGuardar.setOnAction(e -> guardarCliente());

        btnBorrar.setOnAction(e -> borrarClienteSeleccionado());

        btnRecargar.setOnAction(e -> {
            txtBuscar.clear();
            recargarDatos();
        });

        btnBuscar.setOnAction(e -> buscarClientesEnMemoria());

        btnLimpiarBusqueda.setOnAction(e -> {
            txtBuscar.clear();
            recargarDatos();
        });
    }

    /* =========================================================
       LÓGICA DE NEGOCIO (usando ClienteDAO actual)
       ========================================================= */

    /**
     * Carga todos los clientes desde la BD usando ClienteDAO.findAll()
     */
    private void recargarDatos() {
        try {
            List<Cliente> lista = clienteDAO.findAll();
            datos.setAll(lista);
        } catch (SQLException e) {
            mostrarError("Error al cargar clientes", e);
        }
    }

    /**
     * Búsqueda de momento hecha EN MEMORIA.
     *
     * Se carga toda la lista (findAll) y se filtra con streams.
     * Más adelante se puede cambiar para que use ClienteDAO.search()
     * cuando lo implementéis.
     */
    private void buscarClientesEnMemoria() {
        String filtro = txtBuscar.getText().trim();
        if (filtro.isEmpty()) {
            recargarDatos();
            return;
        }

        try {
            List<Cliente> lista = clienteDAO.findAll();
            String f = filtro.toLowerCase();

            List<Cliente> filtrados = lista.stream()
                    .filter(c ->
                            String.valueOf(c.getId()).contains(f) ||
                                    c.getNombre().toLowerCase().contains(f) ||
                                    c.getEmail().toLowerCase().contains(f)
                    )
                    .collect(Collectors.toList());

            datos.setAll(filtrados);
        } catch (SQLException e) {
            mostrarError("Error al buscar clientes", e);
        }
    }

    private void limpiarFormulario() {
        txtId.clear();
        txtNombre.clear();
        txtEmail.clear();
        txtId.setDisable(false);
        tabla.getSelectionModel().clearSelection();
    }

    /**
     * Guardar cliente:
     *  - Si no existe en la BD → INSERT usando ClienteDAO.insert()
     *  - Si existe → por ahora solo muestra un aviso.
     *
     * Más adelante, cuando implementéis ClienteDAO.update(Cliente),
     * aquí se puede llamar a update si ya existe.
     */
    private void guardarCliente() {
        // Validación rápida
        if (txtId.getText().isBlank() ||
                txtNombre.getText().isBlank() ||
                txtEmail.getText().isBlank()) {

            mostrarAlerta("Campos obligatorios",
                    "Debes rellenar ID, nombre y email.");
            return;
        }

        int id;
        try {
            id = Integer.parseInt(txtId.getText().trim());
        } catch (NumberFormatException ex) {
            mostrarAlerta("ID inválido", "El ID debe ser un número entero.");
            return;
        }

        Cliente c = new Cliente(id,
                txtNombre.getText().trim(),
                txtEmail.getText().trim());

        try {
            Cliente existente = clienteDAO.findById(id);

            if (existente == null) {
                // No existe → INSERT real
                clienteDAO.insert(c);
                mostrarInfo("Insertado", "Cliente creado correctamente.");
            } else {
                // Ya existe → aquí en el futuro iría un UPDATE.
                // TODO: cuando implementéis ClienteDAO.update(Cliente),
                //  llamad aquí a ese método en lugar de mostrar solo un aviso.
                mostrarAlerta("Actualizar pendiente",
                        "El cliente ya existe.\n" +
                                "Más adelante aquí haremos UPDATE desde el DAO.");
            }

            recargarDatos();
            limpiarFormulario();

        } catch (SQLException e) {
            mostrarError("Error al guardar cliente", e);
        }
    }

    /**
     * Borrar cliente seleccionado.
     * De momento solo muestra un aviso con un TODO.
     *
     * Cuando implementéis ClienteDAO.deleteById(int id),
     * se puede llamar aquí a ese método.
     */
    private void borrarClienteSeleccionado() {
        Cliente sel = tabla.getSelectionModel().getSelectedItem();
        if (sel == null) {
            mostrarAlerta("Sin selección", "Selecciona un cliente en la tabla.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar borrado");
        confirm.setHeaderText("¿Eliminar cliente?");
        confirm.setContentText("Se borrará el cliente con ID " + sel.getId());
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        // TODO: implementar ClienteDAO.deleteById(int id) y llamarlo aquí.
        mostrarAlerta("Borrado pendiente",
                "Aún no existe deleteById en ClienteDAO.\n" +
                        "Cuando lo implementemos, aquí se llamará al método.");

        // Cuando exista:
        /*
        try {
            int borradas = clienteDAO.deleteById(sel.getId());
            if (borradas > 0) {
                mostrarInfo("Borrado", "Cliente eliminado.");
                recargarDatos();
                limpiarFormulario();
            } else {
                mostrarAlerta("No borrado", "No se encontró el cliente en la BD.");
            }
        } catch (SQLException e) {
            mostrarError("Error al borrar cliente", e);
        }
        */
    }

    /* =========================================================
       DIÁLOGOS AUXILIARES
       ========================================================= */

    private void mostrarError(String titulo, Exception e) {
        e.printStackTrace();
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(titulo);
        alert.setContentText(e.getMessage());
        alert.showAndWait();
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Aviso");
        alert.setHeaderText(titulo);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarInfo(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información");
        alert.setHeaderText(titulo);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
