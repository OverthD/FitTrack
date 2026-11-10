package com.fittrack.ui;

import com.fittrack.servicio.FilaAcceso;
import com.fittrack.servicio.FitTrackServicio;
import java.time.LocalDate;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/** Pantalla 10: Historial de accesos (tema oscuro). */
public class HistorialAccesosView {

    private final FitTrackServicio servicio;
    private final Navegador nav;
    private final String filtroInicial;

    private final DatePicker dpFecha = new DatePicker();
    private final TextField txtFiltro = Ui.campo("Filtrar por socio", true);
    private final TableView<FilaAcceso> tabla = Ui.tabla("No hay accesos para esta búsqueda.");
    private VBox raiz;

    public HistorialAccesosView(FitTrackServicio servicio, Navegador nav, String filtroInicial) {
        this.servicio = servicio;
        this.nav = nav;
        this.filtroInicial = filtroInicial;
    }

    public Node crearVista() {
        raiz = new VBox(16);
        raiz.getStyleClass().add("pantalla-oscura");

        dpFecha.getStyleClass().add("campo-oscuro");
        dpFecha.setPromptText("Todas las fechas");
        dpFecha.setPrefWidth(180);
        HBox.setHgrow(txtFiltro, Priority.ALWAYS);

        if (filtroInicial != null) {
            txtFiltro.setText(filtroInicial);
        } else {
            dpFecha.setValue(LocalDate.now());
        }
        dpFecha.valueProperty().addListener((o, a, n) -> recargar());
        txtFiltro.textProperty().addListener((o, a, n) -> recargar());

        tabla.getColumns().add(Ui.<FilaAcceso>columna("Socio", f -> f.socio().getNombre() + " " + f.socio().getApellido()));
        tabla.getColumns().add(Ui.<FilaAcceso>columna("Fecha", f -> Formato.fecha(f.acceso().getFecha())));
        tabla.getColumns().add(Ui.<FilaAcceso>columna("Entrada", f -> Formato.hora(f.acceso().getHoraEntrada())));
        tabla.getColumns().add(Ui.<FilaAcceso>columnaNodo("Salida", this::celdaSalida));

        raiz.getChildren().addAll(Ui.encabezado("Accesos", "Historial de accesos"),
            new HBox(10, dpFecha, txtFiltro), tabla);
        recargar();
        return raiz;
    }

    private Node celdaSalida(FilaAcceso f) {
        if (f.acceso().getHoraSalida() != null) {
            return new Label(Formato.hora(f.acceso().getHoraSalida()));
        }
        Button btn = Ui.boton("Registrar salida", "boton-secundario");
        btn.setStyle("-fx-padding: 4 12;");
        btn.setOnAction(e -> {
            try {
                servicio.registrarSalida(f.acceso().getIdAcceso());
                recargar();
            } catch (IllegalStateException | IllegalArgumentException ex) {
                DialogoFitTrack.advertencia(Ui.ventana(raiz), "No se registró la salida", ex.getMessage());
            }
        });
        HBox caja = new HBox(10, Ui.insignia("Pendiente", "insignia-tolerancia"), btn);
        caja.setAlignment(Pos.CENTER_LEFT);
        return caja;
    }

    private void recargar() {
        tabla.setItems(FXCollections.observableArrayList(
            servicio.listarAccesos(dpFecha.getValue(), txtFiltro.getText())));
    }
}
