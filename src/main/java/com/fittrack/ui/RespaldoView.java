package com.fittrack.ui;

import com.fittrack.servicio.FitTrackServicio;
import com.fittrack.servicio.RespaldoResumen;

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;

public class RespaldoView {

    private final FitTrackServicio servicio;

    private final javafx.scene.control.TextField txtUbicacion
            = new javafx.scene.control.TextField();

    private final Label lblResultado = new Label();

    private final Button btnGenerar
            = Ui.boton("Generar respaldo", "boton-primario");

    private final Button btnActualizar
            = Ui.boton("Actualizar", "boton-secundario");

    private final TableView<RespaldoResumen> tabla
            = new TableView<>();

    private VBox raiz;

    private static final DateTimeFormatter FORMATO_FECHA
            = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public RespaldoView(FitTrackServicio servicio) {
        this.servicio = servicio;
    }

    public Node crearVista() {

        raiz = new VBox(16);
        raiz.getStyleClass().add("pantalla-clara");

        // Configurar campo de ubicación.
        txtUbicacion.getStyleClass().add("campo");

        txtUbicacion.setText(
                System.getProperty("user.home")
                + File.separator + "FitTrack"
                + File.separator + "Respaldos"
        );

        HBox.setHgrow(txtUbicacion, Priority.ALWAYS);

        // Configurar botones.
        Button btnElegir
                = Ui.boton("Elegir carpeta", "boton-secundario");

        btnElegir.setOnAction(e -> elegirCarpeta());
        btnGenerar.setOnAction(e -> generar());
        btnActualizar.setOnAction(e -> cargarRespaldos());

        // Configurar mensaje de resultado.
        lblResultado.getStyleClass().add("mensaje-exito");
        lblResultado.setMaxWidth(Double.MAX_VALUE);
        lblResultado.setWrapText(true);
        lblResultado.setVisible(false);
        lblResultado.setManaged(false);

        // Panel de generación.
        Label descripcion = Ui.etiqueta(
                "Incluye socios, membresías, pagos, accesos y usuarios.",
                "texto-secundario"
        );

        VBox panelGenerar = Ui.panel(
                12,
                Ui.etiqueta(
                        "Generar respaldo completo",
                        "subtitulo-panel"
                ),
                descripcion,
                Ui.etiqueta("Ubicación", "etiqueta-campo"),
                new HBox(10, txtUbicacion, btnElegir),
                btnGenerar,
                lblResultado
        );

        panelGenerar.setMaxWidth(720);

        // Configurar tabla del historial.
        configurarTabla();

        VBox panelHistorial = Ui.panel(
                12,
                new HBox(
                        10,
                        Ui.etiqueta(
                                "Historial de respaldos",
                                "subtitulo-panel"
                        ),
                        btnActualizar
                ),
                tabla
        );

        tabla.setMaxWidth(Double.MAX_VALUE);
        tabla.setPrefHeight(320);

        // Agregar secciones a la pantalla.
        raiz.getChildren().addAll(
                Ui.encabezado(
                        "Administración",
                        "Respaldo de base de datos"
                ),
                panelGenerar,
                panelHistorial
        );

        VBox.setVgrow(panelHistorial, Priority.ALWAYS);

        // Cargar registros iniciales.
        cargarRespaldos();

        return raiz;
    }

    private void configurarTabla() {

        tabla.getColumns().clear();

        tabla.getColumns().addAll(
                Ui.<RespaldoResumen>columna(
                        "ID",
                        respaldo -> String.valueOf(respaldo.getIdRespaldo())
                ),
                Ui.<RespaldoResumen>columna(
                        "Fecha de generación",
                        respaldo -> respaldo.getFecha() == null
                        ? "No disponible"
                        : respaldo.getFecha().format(
                                FORMATO_FECHA
                        )
                ),
                Ui.<RespaldoResumen>columna(
                        "Administrador",
                        RespaldoResumen::getAdministrador
                )
        );

        tabla.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );

        tabla.setPlaceholder(
                new Label("No hay respaldos registrados.")
        );
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

            DialogoFitTrack.advertencia(
                    Ui.ventana(raiz),
                    "Falta la ubicación",
                    "Elige la carpeta donde se guardará el respaldo."
            );

            return;
        }

        btnGenerar.setDisable(true);

        lblResultado.setVisible(false);
        lblResultado.setManaged(false);

        Thread hilo = new Thread(() -> {

            try {

                String nombre = servicio.generarRespaldo(carpeta);

                Platform.runLater(() -> {

                    lblResultado.setText(
                            "Respaldo generado correctamente: " + nombre
                    );

                    lblResultado.setVisible(true);
                    lblResultado.setManaged(true);

                    btnGenerar.setDisable(false);

                    cargarRespaldos();
                });

            } catch (Exception ex) {

                Platform.runLater(() -> {

                    btnGenerar.setDisable(false);

                    DialogoFitTrack.error(
                            Ui.ventana(raiz),
                            "No se pudo generar el respaldo",
                            ex.getMessage() != null
                            ? ex.getMessage()
                            : "Ocurrió un error inesperado."
                    );
                });
            }
        });

        hilo.setDaemon(true);
        hilo.start();
    }

    private void cargarRespaldos() {

        btnActualizar.setDisable(true);

        Thread hilo = new Thread(() -> {

            try {

                List<RespaldoResumen> respaldos
                        = servicio.listarRespaldos();

                Platform.runLater(() -> {

                    tabla.getItems().setAll(respaldos);

                    btnActualizar.setDisable(false);
                });

            } catch (Exception ex) {

                Platform.runLater(() -> {

                    btnActualizar.setDisable(false);

                    DialogoFitTrack.error(
                            Ui.ventana(raiz),
                            "No se pudo cargar el historial",
                            ex.getMessage() != null
                            ? ex.getMessage()
                            : "Ocurrió un error inesperado."
                    );
                });
            }
        });

        hilo.setDaemon(true);
        hilo.start();
    }
}
