
package com.fittrack;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class FitTrack extends Application {

    @Override
    public void start(Stage stage) {
        Label mensaje = new Label("FitTrack");
        mensaje.setStyle(
            "-fx-font-size: 28px;" +
            "-fx-font-weight: bold;"
        );

        StackPane root = new StackPane(mensaje);
        Scene scene = new Scene(root, 900, 600);

        stage.setTitle("FitTrack - Sistema de gestión de gimnasio");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
