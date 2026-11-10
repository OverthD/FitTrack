package com.fittrack.ui;

import com.fittrack.servicio.FitTrackServicio;
import com.fittrack.servicio.MetodoPago;
import com.fittrack.servicio.TablaReporte;
import com.fittrack.servicio.TipoReporte;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.print.PageLayout;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.transform.Scale;

/** Pantalla 13: Reportes (tema claro, solo administrador). */
public class ReportesView {

    private final FitTrackServicio servicio;

    private final ToggleGroup tipos = new ToggleGroup();
    private final DatePicker dpDesde = new DatePicker(YearMonth.now().atDay(1));
    private final DatePicker dpHasta = new DatePicker(YearMonth.now().atEndOfMonth());
    private final Label lblFiltro = Ui.etiqueta("Estado", "etiqueta-campo");
    private final ComboBox<String> cmbFiltro = new ComboBox<>();
    private final TableView<List<String>> tabla = Ui.tabla("Elige un reporte y presiona \"Generar reporte\".");
    private VBox raiz;

    public ReportesView(FitTrackServicio servicio) {
        this.servicio = servicio;
    }

    public Node crearVista() {
        raiz = new VBox(16);
        raiz.getStyleClass().add("pantalla-clara");

        HBox pildoras = new HBox(8);
        pildoras.setAlignment(Pos.CENTER_LEFT);
        agregarTipo(pildoras, "Socios activos", TipoReporte.SOCIOS_ACTIVOS, true);
        agregarTipo(pildoras, "Membresías", TipoReporte.MEMBRESIAS, false);
        agregarTipo(pildoras, "Pagos", TipoReporte.PAGOS, false);
        agregarTipo(pildoras, "Accesos", TipoReporte.ACCESOS, false);
        tipos.selectedToggleProperty().addListener((o, anterior, nuevo) -> {
            if (nuevo == null) {
                anterior.setSelected(true);
            } else {
                configurarFiltros();
                generar();
            }
        });

        dpDesde.getStyleClass().add("campo");
        dpHasta.getStyleClass().add("campo");
        cmbFiltro.getStyleClass().add("campo");
        cmbFiltro.setMaxWidth(Double.MAX_VALUE);

        Button btnGenerar = Ui.boton("Generar reporte", "boton-marino");
        btnGenerar.setOnAction(e -> generar());
        Button btnImprimir = Ui.boton("Imprimir", "boton-secundario");
        btnImprimir.setOnAction(e -> imprimir());

        VBox colFiltro = new VBox(4, lblFiltro, cmbFiltro);
        HBox filtros = new HBox(12,
            new VBox(4, Ui.etiqueta("Desde", "etiqueta-campo"), dpDesde),
            new VBox(4, Ui.etiqueta("Hasta", "etiqueta-campo"), dpHasta),
            colFiltro, Ui.espacio(), btnGenerar, btnImprimir);
        filtros.setAlignment(Pos.BOTTOM_LEFT);
        VBox panelFiltros = Ui.panel(0, filtros);

        raiz.getChildren().addAll(Ui.encabezado("Administración", "Reportes"), pildoras, panelFiltros, tabla);
        configurarFiltros();
        generar();
        return raiz;
    }

    private void agregarTipo(HBox contenedor, String texto, TipoReporte tipo, boolean seleccionado) {
        ToggleButton t = Ui.pildora(texto);
        t.setUserData(tipo);
        t.setToggleGroup(tipos);
        t.setSelected(seleccionado);
        contenedor.getChildren().add(t);
    }

    private TipoReporte tipoActual() {
        return (TipoReporte) tipos.getSelectedToggle().getUserData();
    }

    private void configurarFiltros() {
        TipoReporte tipo = tipoActual();
        List<String> opciones = new ArrayList<>(List.of("Todos"));
        boolean usaFiltro = true;
        switch (tipo) {
            case SOCIOS_ACTIVOS -> {
                lblFiltro.setText("Estado de la membresía");
                opciones.addAll(List.of("Vigente", "Tolerancia", "Vencida"));
            }
            case MEMBRESIAS -> {
                lblFiltro.setText("Estado de la membresía");
                opciones.addAll(List.of("Vigente", "Tolerancia", "Vencida", "Programada"));
            }
            case PAGOS -> {
                lblFiltro.setText("Método de pago");
                opciones.addAll(MetodoPago.TODOS);
            }
            default -> usaFiltro = false;
        }
        cmbFiltro.getItems().setAll(opciones);
        cmbFiltro.setValue("Todos");
        cmbFiltro.setDisable(!usaFiltro);
        boolean usaPeriodo = tipo != TipoReporte.SOCIOS_ACTIVOS;
        dpDesde.setDisable(!usaPeriodo);
        dpHasta.setDisable(!usaPeriodo);
    }

    private void generar() {
        LocalDate desde = dpDesde.getValue();
        LocalDate hasta = dpHasta.getValue();
        if (tipoActual() != TipoReporte.SOCIOS_ACTIVOS) {
            if (desde == null || hasta == null || hasta.isBefore(desde)) {
                DialogoFitTrack.advertencia(Ui.ventana(raiz), "Periodo no válido",
                    "Elige las dos fechas y que la final no sea anterior a la inicial.");
                return;
            }
        }
        TablaReporte reporte = servicio.reporte(tipoActual(), desde, hasta, cmbFiltro.getValue());

        tabla.getColumns().clear();
        for (int i = 0; i < reporte.columnas().size(); i++) {
            final int indice = i;
            tabla.getColumns().add(Ui.<List<String>>columna(reporte.columnas().get(i), fila -> fila.get(indice)));
        }
        tabla.setPlaceholder(new Label("No hay datos para este reporte."));
        tabla.setItems(FXCollections.observableArrayList(reporte.filas()));
    }

    private void imprimir() {
        if (tabla.getItems().isEmpty()) {
            DialogoFitTrack.advertencia(Ui.ventana(raiz), "Nada que imprimir", "Genera un reporte con datos antes de imprimir.");
            return;
        }
        PrinterJob trabajo = PrinterJob.createPrinterJob();
        if (trabajo == null) {
            DialogoFitTrack.error(Ui.ventana(raiz), "Sin impresora", "No se encontró una impresora disponible en este equipo.");
            return;
        }
        if (!trabajo.showPrintDialog(Ui.ventana(raiz))) {
            return;
        }
        PageLayout pagina = trabajo.getJobSettings().getPageLayout();
        double ancho = tabla.getBoundsInParent().getWidth();
        Scale escala = new Scale(1, 1);
        if (ancho > pagina.getPrintableWidth()) {
            double f = pagina.getPrintableWidth() / ancho;
            escala = new Scale(f, f);
        }
        tabla.getTransforms().add(escala);
        boolean ok = trabajo.printPage(tabla);
        tabla.getTransforms().remove(escala);
        if (ok) {
            trabajo.endJob();
        } else {
            DialogoFitTrack.error(Ui.ventana(raiz), "No se pudo imprimir", "Revisa la impresora e inténtalo de nuevo.");
        }
    }
}
