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

import com.fittrack.service.ReportePDFService;
import com.lowagie.text.DocumentException;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;

import java.awt.Desktop;

/**
 * Pantalla 13: Reportes (tema claro, solo administrador).
 */
public class ReportesView {

    private final FitTrackServicio servicio;

    private final ToggleGroup tipos = new ToggleGroup();
    private final DatePicker dpDesde = new DatePicker(YearMonth.now().atDay(1));
    private final DatePicker dpHasta = new DatePicker(YearMonth.now().atEndOfMonth());
    private final Label lblFiltro = Ui.etiqueta("Estado", "etiqueta-campo");
    private final ComboBox<String> cmbFiltro = new ComboBox<>();
    private final TableView<List<String>> tabla = Ui.tabla("Elige un reporte y presiona \"Generar reporte\".");
    private VBox raiz;
    private TablaReporte reporteActual;

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
        Button btnExportar = Ui.boton("Exportar PDF", "boton-secundario");
        btnExportar.setOnAction(e -> exportarPDF());

        VBox colFiltro = new VBox(4, lblFiltro, cmbFiltro);
        HBox filtros = new HBox(12,
                new VBox(4, Ui.etiqueta("Desde", "etiqueta-campo"), dpDesde),
                new VBox(4, Ui.etiqueta("Hasta", "etiqueta-campo"), dpHasta),
                colFiltro, Ui.espacio(), btnGenerar, btnExportar);
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
            default ->
                usaFiltro = false;
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
                DialogoFitTrack.advertencia(
                        Ui.ventana(raiz),
                        "Periodo no válido",
                        "Elige las dos fechas y que la final no sea anterior a la inicial.");
                return;
            }
        }

        reporteActual = servicio.reporte(
                tipoActual(), desde, hasta, cmbFiltro.getValue());

        TablaReporte reporte = reporteActual;

        tabla.getColumns().clear();

        for (int i = 0; i < reporte.columnas().size(); i++) {

            final int indice = i;
            String nombreColumna = reporte.columnas().get(i);
            String nombre = nombreColumna.toLowerCase().trim();

            var columna = Ui.<List<String>>columna(
                    nombreColumna,
                    fila -> {
                        String valor = fila.get(indice);

                        // Mostrar el ID del socio con el formato oficial.
                        if (nombre.equals("idsocio")
                        || nombre.equals("id socio")) {
                            try {
                                return Formato.idSocio(
                                        Formato.leerIdSocio(valor));
                            } catch (Exception ex) {
                                return valor;
                            }
                        }

                        return valor;
                    });

            double ancho = switch (nombre) {
                case "id", "idempleado", "id empleado" ->
                    90;
                case "idsocio", "id socio" ->
                    125;
                case "nombre", "apellido" ->
                    150;
                case "correo", "correo electrónico", "correoelectronico" ->
                    250;
                case "teléfono", "telefono" ->
                    140;
                case "estado", "método de pago", "metodo de pago" ->
                    145;
                default ->
                    140;
            };

            columna.setMinWidth(ancho);
            columna.setPrefWidth(ancho);
            columna.setMaxWidth(ancho);

            tabla.getColumns().add(columna);
        }

        // Mantener anchos estables al cambiar entre reportes.
        tabla.setColumnResizePolicy(
                TableView.UNCONSTRAINED_RESIZE_POLICY);

        tabla.setPlaceholder(
                new Label("No hay datos para este reporte."));

        tabla.setItems(
                FXCollections.observableArrayList(reporte.filas()));
    }

    private void exportarPDF() {

        if (reporteActual == null) {
            DialogoFitTrack.advertencia(
                    Ui.ventana(raiz),
                    "Reporte no disponible",
                    "Primero genera un reporte.");
            return;
        }

        FileChooser selector = new FileChooser();
        selector.setTitle("Guardar reporte PDF");

        selector.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        "Documento PDF", "*.pdf"));

        String fechaHora = LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

        selector.setInitialFileName(
                "FitTrack_Reporte_" + tipoActual().name()
                + "_" + fechaHora + ".pdf");

        File archivo = selector.showSaveDialog(Ui.ventana(raiz));

        if (archivo == null) {
            return;
        }

        if (!archivo.getName().toLowerCase().endsWith(".pdf")) {
            archivo = new File(archivo.getAbsolutePath() + ".pdf");
        }

        try {
            // Primero generar y guardar el documento.
            ReportePDFService generador = new ReportePDFService();

            generador.generar(
                    archivo.toPath(),
                    tipoActual(),
                    dpDesde.getValue(),
                    dpHasta.getValue(),
                    cmbFiltro.isDisabled()
                    ? "Sin filtro"
                    : cmbFiltro.getValue(),
                    reporteActual
            );

            // Comprobar que el archivo realmente exista.
            if (!archivo.exists()) {
                throw new java.io.IOException(
                        "El PDF no se encontró después de generarlo.");
            }

            // Después abrirlo con el visor predeterminado.
            if (Desktop.isDesktopSupported()
                    && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {

                Desktop.getDesktop().open(archivo);

                DialogoFitTrack.advertencia(
                        Ui.ventana(raiz),
                        "Exportación completada",
                        "El reporte se generó y se abrió correctamente.");

            } else {
                DialogoFitTrack.advertencia(
                        Ui.ventana(raiz),
                        "PDF generado",
                        "El archivo se guardó correctamente, pero este equipo "
                        + "no permite abrirlo automáticamente:\n"
                        + archivo.getAbsolutePath());
            }

        } catch (java.io.IOException ex) {
            DialogoFitTrack.error(
                    Ui.ventana(raiz),
                    "Error al exportar",
                    "No fue posible guardar o abrir el PDF:\n"
                    + ex.getMessage());

            ex.printStackTrace();

        } catch (Exception ex) {
            DialogoFitTrack.error(
                    Ui.ventana(raiz),
                    "Error al generar PDF",
                    ex.getMessage());

            ex.printStackTrace();
        }
    }

}
