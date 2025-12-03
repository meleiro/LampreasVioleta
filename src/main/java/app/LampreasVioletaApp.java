package app;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class LampreasVioletaApp extends Application {
    @Override
    public void start(Stage stage) {
        ClientesView vistaClientes = new ClientesView();
        Scene scene = new Scene(vistaClientes.getRoot(), 900, 600);
        stage.setTitle("Gestión de Clientes - Lampreas Violeta");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
