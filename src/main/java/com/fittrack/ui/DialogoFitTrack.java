package com.fittrack.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

/**
 * Ventanas emergentes de FitTrack.
 *
 * Uso:
 *   DialogoFitTrack.error(ventana, "Acceso denegado", "La membresía venció.");
 *   if (DialogoFitTrack.confirmar(ventana, "Dar de baja", "¿Continuar?", "Dar de baja")) { ... }
 *
 * "ventana" puede ser null o, normalmente, nodo.getScene().getWindow().
 */
public final class DialogoFitTrack {

    public enum Tipo { ERROR, ADVERTENCIA, EXITO, CONFIRMACION }

    private DialogoFitTrack() { }

    public static void error(Window dueno, String titulo, String mensaje) {
        mostrar(dueno, Tipo.ERROR, titulo, mensaje, "Aceptar", null);
    }

    public static void advertencia(Window dueno, String titulo, String mensaje) {
        mostrar(dueno, Tipo.ADVERTENCIA, titulo, mensaje, "Aceptar", null);
    }

    public static void exito(Window dueno, String titulo, String mensaje) {
        mostrar(dueno, Tipo.EXITO, titulo, mensaje, "Aceptar", null);
    }

    /** Devuelve true si la persona presionó el botón de confirmar. */
    public static boolean confirmar(Window dueno, String titulo, String mensaje, String textoConfirmar) {
        return mostrar(dueno, Tipo.CONFIRMACION, titulo, mensaje, textoConfirmar, "Cancelar");
    }

    /** Versión completa: permite textos propios en los botones. textoCancelar null = un solo botón. */
    public static boolean mostrar(Window dueno, Tipo tipo, String titulo, String mensaje,
                                  String textoAceptar, String textoCancelar) {
        Stage stage = new Stage(StageStyle.TRANSPARENT);
        if (dueno != null) {
            stage.initOwner(dueno);
            stage.initModality(Modality.WINDOW_MODAL);
        } else {
            stage.initModality(Modality.APPLICATION_MODAL);
        }

        boolean[] aceptado = {false};

        Label glifo = new Label(glifoDe(tipo));
        String colorGlifo = tipo == Tipo.ADVERTENCIA ? Tema.TEXTO : "white";
        glifo.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " + colorGlifo + ";");
        StackPane icono = new StackPane(new Circle(23, Color.web(colorDe(tipo))), glifo);

        Label lblTitulo = new Label(titulo);
        lblTitulo.getStyleClass().add("dialogo-titulo");
        lblTitulo.setWrapText(true);
        lblTitulo.setTextAlignment(TextAlignment.CENTER);

        Label lblMensaje = new Label(mensaje);
        lblMensaje.getStyleClass().add("dialogo-mensaje");
        lblMensaje.setWrapText(true);
        lblMensaje.setTextAlignment(TextAlignment.CENTER);

        HBox botones = new HBox(8);

        if (textoCancelar != null) {
            Button btnCancelar = new Button(textoCancelar);
            btnCancelar.getStyleClass().add("boton-secundario");
            btnCancelar.setMaxWidth(Double.MAX_VALUE);
            btnCancelar.setCancelButton(true);
            btnCancelar.setOnAction(e -> stage.close());
            HBox.setHgrow(btnCancelar, Priority.ALWAYS);
            botones.getChildren().add(btnCancelar);
        }

        Button btnAceptar = new Button(textoAceptar);
        btnAceptar.getStyleClass().add("boton-primario");
        btnAceptar.setMaxWidth(Double.MAX_VALUE);
        btnAceptar.setDefaultButton(true);
        btnAceptar.setOnAction(e -> {
            aceptado[0] = true;
            stage.close();
        });
        HBox.setHgrow(btnAceptar, Priority.ALWAYS);
        botones.getChildren().add(btnAceptar);

        VBox tarjeta = new VBox(8, icono, lblTitulo, lblMensaje, botones);
        tarjeta.getStyleClass().add("dialogo");
        tarjeta.setAlignment(Pos.CENTER);
        tarjeta.setPrefWidth(360);
        tarjeta.setMaxWidth(360);
        VBox.setMargin(botones, new Insets(10, 0, 0, 0));

        StackPane raiz = new StackPane(tarjeta);
        raiz.setPadding(new Insets(18));
        raiz.setStyle("-fx-background-color: transparent;");

        Scene scene = new Scene(raiz);
        scene.setFill(Color.TRANSPARENT);
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                stage.close();
            }
        });
        Tema.aplicar(scene);

        stage.setScene(scene);
        stage.showAndWait();
        return aceptado[0];
    }

    private static String glifoDe(Tipo tipo) {
        switch (tipo) {
            case ERROR: return "✕";
            case ADVERTENCIA: return "!";
            case EXITO: return "✓";
            default: return "?";
        }
    }

    private static String colorDe(Tipo tipo) {
        switch (tipo) {
            case ERROR: return Tema.ERROR;
            case ADVERTENCIA: return Tema.ADVERTENCIA;
            case EXITO: return Tema.EXITO;
            default: return Tema.AZUL_MEDIO;
        }
    }
}
