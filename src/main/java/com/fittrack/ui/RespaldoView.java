package com.fittrack.ui;

import com.fittrack.servicio.FitTrackServicio;
import java.io.File;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;

/** Pantalla 15: Respaldo de base de datos (tema claro, solo administrador). */
public class RespaldoView {

    private final FitTrackServicio servicio;

    private final TextField txtUbicacion = new TextField();
    private final Label lblResultado = new Label();
    private final Button btnGenerar = Ui.boton("Generar respaldo", "boton-primario");
    private VBox raiz;

    public RespaldoView(FitTrackServicio servicio) {
        this.servicio = servicio;
    }

    public Node crearVista() {
        raiz = new VBox(16);
        raiz.getStyleClass().add("pantalla-clara");

        txtUbicacion.getStyleClass().add("campo");
        txtUbicacion.setText(System.getProperty("user.home") + File.separator + "FitTrack" + File.separator + "Respaldos");
        HBox.setHgrow(txtUbicacion, Priority.ALWAYS);

        Button btnElegir = Ui.boton("Elegir carpeta", "boton-secundario");
        btnElegir.setOnAction(e -> elegirCarpeta());
        btnGenerar.setOnAction(e -> generar());

        lblResultado.getStyleClass().add("mensaje-exito");
        lblResultado.setMaxWidth(Double.MAX_VALUE);
        lblResultado.setWrapText(true);
        lblResultado.setVisible(false);
        lblResultado.setManaged(false);

        Label descripcion = Ui.etiqueta("Incluye socios, membresías, pagos, accesos y usuarios.", "texto-secundario");
        VBox panel = Ui.panel(12,
            Ui.etiqueta("Generar respaldo completo", "subtitulo-panel"), descripcion,
            Ui.etiqueta("Ubicación", "etiqueta-campo"),
            new HBox(10, txtUbicacion, btnElegir), btnGenerar, lblResultado);
        panel.setMaxWidth(720);

        raiz.getChildren().addAll(Ui.encabezado("Administración", "Respaldo de base de datos"), panel);
        return raiz;
    }

    private void elegirCarpeta() {
        DirectoryChooser selector = new DirectoryChooser();
        selector.setTitle("Elegir carpeta de respaldos");
        File actual = new File(txtUbicacion.getText());
        if (actual.isDirectory()) {
            selector.setInitialDirectory(actual);
        }
        File elegida = selector.showDialog(Ui.ventana(raiz));
        if (elegida != null) {
            txtUbicacion.setText(elegida.getAbsolutePath());
        }
    }

    private void generar() {
        String carpeta = txtUbicacion.getText().trim();
        if (carpeta.isEmpty()) {
            DialogoFitTrack.advertencia(Ui.ventana(raiz), "Falta la ubicación", "Elige la carpeta donde se guardará el respaldo.");
            return;
        }
        btnGenerar.setDisable(true);
        lblResultado.setVisible(false);
        lblResultado.setManaged(false);

        Thread hilo = new Thread(() -> {
            try {
                String nombre = servicio.generarRespaldo(carpeta);
                Platform.runLater(() -> {
                    lblResultado.setText("Respaldo generado correctamente: " + nombre);
                    lblResultado.setVisible(true);
                    lblResultado.setManaged(true);
                    btnGenerar.setDisable(false);
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    btnGenerar.setDisable(false);
                    DialogoFitTrack.error(Ui.ventana(raiz), "No se pudo generar el respaldo",
                        "Revisa que la carpeta " + carpeta + " exista y tenga permiso de escritura.");
                });
            }
        });
        hilo.setDaemon(true);
        hilo.start();
    }
}
