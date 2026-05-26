package proptech;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import proptech.servicios.PropTechService;
import proptech.util.DatosDePrueba;
import proptech.vista.MainView;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        PropTechService service = PropTechService.getInstance();
        DatosDePrueba.cargarSiEsNecesario(service);

        MainView mainView = new MainView(stage);
        Scene scene = mainView.buildScene();

        // Tamaño inicial grande y bien definido
        stage.setTitle("PropTech — Sistema de Gestión Inmobiliaria");
        stage.setScene(scene);
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.setWidth(1200);
        stage.setHeight(780);
        // Maximizar al inicio para que se vea completo
        stage.setMaximized(true);
        stage.show();
    }

    @Override
    public void stop() {
        proptech.repositorio.ConexionDB.getInstance().cerrar();
    }

    public static void main(String[] args) {
        launch(args);
    }
}