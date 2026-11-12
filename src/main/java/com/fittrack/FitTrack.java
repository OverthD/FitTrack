package com.fittrack;

import com.fittrack.model.UsuarioSistema;
import com.fittrack.servicio.FitTrackServicio;
import com.fittrack.ui.LoginView;
import com.fittrack.ui.Tema;
import com.fittrack.ui.VentanaPrincipal;

import javafx.application.Platform;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.fittrack.integracion.ServicioMySQL;

public class FitTrack extends Application {

    private final FitTrackServicio servicio = new ServicioMySQL();

    @Override
    public void start(Stage stage) {
        mostrarLogin(stage);
    }

    private void mostrarLogin(Stage stage) {
        LoginView login = new LoginView(
                servicio::autenticar,
                usuario -> mostrarPrincipal(stage, usuario)
        );

        Scene scene = new Scene(login.crearVista(), 1000, 650);
        Tema.aplicar(scene);

        stage.setTitle("FitTrack - Sistema de gestión de gimnasio");
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.setMaximized(false);
        stage.setScene(scene);
        stage.show();

        Platform.runLater(() -> stage.setMaximized(true));
    }

    private void mostrarPrincipal(Stage stage, UsuarioSistema usuario) {
        VentanaPrincipal principal = new VentanaPrincipal(
                usuario,
                servicio,
                () -> mostrarLogin(stage)
        );

        Scene scene = new Scene(principal.getRaiz(), 1200, 750);
        Tema.aplicar(scene);

        stage.setTitle("FitTrack - Sistema de gestión de gimnasio");
        stage.setMinWidth(1000);
        stage.setMinHeight(650);
        stage.setMaximized(false);
        stage.setScene(scene);
        stage.show();

        Platform.runLater(() -> stage.setMaximized(true));
    }

    public static void main(String[] args) {
        launch(args);
    }
}
