package com.fittrack.ui;

import com.fittrack.servicio.FitTrackServicio;
import com.fittrack.servicio.LineaVenta;
import com.fittrack.servicio.ResumenVentas;
import java.time.LocalDate;
import java.time.YearMonth;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/** Pantalla 12: Ingresos y ventas (tema oscuro, solo administrador). */
public class IngresosView {

    private final FitTrackServicio servicio;

    private final ToggleGroup periodos = new ToggleGroup();
    private final DatePicker dpDesde = new DatePicker(YearMonth.now().atDay(1));
    private final DatePicker dpHasta = new DatePicker(LocalDate.now());
    private final HBox barraPersonalizada = new HBox(10);
    private final Label lblIngresos = new Label();
    private final Label lblVendidas = new Label();
    private final Label lblRenovaciones = new Label();
    private final VBox panelDetalle = new VBox(10);
    private final CategoryAxis ejeX = new CategoryAxis();
    private final NumberAxis ejeY = new NumberAxis();
    private final BarChart<String, Number> grafica = new BarChart<>(ejeX, ejeY);
    private VBox raiz;

    public IngresosView(FitTrackServicio servicio) {
        this.servicio = servicio;
    }

    public Node crearVista() {
        raiz = new VBox(16);
        raiz.getStyleClass().add("pantalla-oscura");

        HBox pildoras = new HBox(8);
        pildoras.setAlignment(Pos.CENTER_LEFT);
        for (String texto : new String[] {"Este mes", "Mes pasado", "Personalizado"}) {
            ToggleButton t = Ui.pildora(texto);
            t.setUserData(texto);
            t.setToggleGroup(periodos);
            t.setSelected(texto.equals("Este mes"));
            pildoras.getChildren().add(t);
        }
        periodos.selectedToggleProperty().addListener((o, anterior, nuevo) -> {
            if (nuevo == null) {
                anterior.setSelected(true);
                return;
            }
            boolean personalizado = "Personalizado".equals(nuevo.getUserData());
            barraPersonalizada.setVisible(personalizado);
            barraPersonalizada.setManaged(personalizado);
            recargar();
        });

        dpDesde.getStyleClass().add("campo-oscuro");
        dpHasta.getStyleClass().add("campo-oscuro");
        Button btnAplicar = Ui.boton("Aplicar", "boton-marino");
        btnAplicar.setOnAction(e -> recargar());
        barraPersonalizada.setAlignment(Pos.CENTER_LEFT);
        barraPersonalizada.getChildren().addAll(Ui.etiqueta("Desde", "texto-secundario"), dpDesde,
            Ui.etiqueta("Hasta", "texto-secundario"), dpHasta, btnAplicar);
        barraPersonalizada.setVisible(false);
        barraPersonalizada.setManaged(false);

        HBox tarjetas = new HBox(14,
            Ui.tarjetaDato("Ingresos del periodo", lblIngresos),
            Ui.tarjetaDato("Membresías vendidas", lblVendidas),
            Ui.tarjetaDato("Renovaciones", lblRenovaciones));
        for (Node t : tarjetas.getChildren()) {
            HBox.setHgrow(t, Priority.ALWAYS);
            ((VBox) t).setMaxWidth(Double.MAX_VALUE);
        }

        grafica.setLegendVisible(false);
        grafica.setAnimated(false);
        grafica.setCategoryGap(30);
        grafica.setHorizontalGridLinesVisible(true);
        grafica.setVerticalGridLinesVisible(false);
        ejeY.setMinorTickVisible(false);
        ejeY.setTickUnit(1);
        ejeY.setForceZeroInRange(true);
        ejeY.setLabel("Ventas");
        VBox.setVgrow(grafica, Priority.ALWAYS);
        VBox panelGrafica = Ui.panel(10, Ui.etiqueta("Ventas por tipo", "subtitulo-panel"), grafica);
        HBox.setHgrow(panelGrafica, Priority.ALWAYS);

        panelDetalle.setFillWidth(true);
        VBox cajaDetalle = Ui.panel(10, Ui.etiqueta("Detalle", "subtitulo-panel"), panelDetalle);
        cajaDetalle.setPrefWidth(300);
        cajaDetalle.setMinWidth(260);
        cajaDetalle.setMaxHeight(javafx.scene.layout.Region.USE_PREF_SIZE);

        HBox cuerpo = new HBox(16, panelGrafica, cajaDetalle);
        cuerpo.setAlignment(Pos.TOP_LEFT);
        VBox.setVgrow(cuerpo, Priority.ALWAYS);

        raiz.getChildren().addAll(Ui.encabezado("Administración", "Ingresos y ventas"),
            pildoras, barraPersonalizada, tarjetas, cuerpo);
        recargar();
        return raiz;
    }

    private void recargar() {
        String periodo = (String) periodos.getSelectedToggle().getUserData();
        LocalDate desde;
        LocalDate hasta;
        if ("Mes pasado".equals(periodo)) {
            YearMonth mes = YearMonth.now().minusMonths(1);
            desde = mes.atDay(1);
            hasta = mes.atEndOfMonth();
        } else if ("Personalizado".equals(periodo)) {
            desde = dpDesde.getValue();
            hasta = dpHasta.getValue();
            if (desde == null || hasta == null || hasta.isBefore(desde)) {
                DialogoFitTrack.advertencia(Ui.ventana(raiz), "Periodo no válido",
                    "Elige las dos fechas y que la final no sea anterior a la inicial.");
                return;
            }
        } else {
            YearMonth mes = YearMonth.now();
            desde = mes.atDay(1);
            hasta = mes.atEndOfMonth();
        }

        ResumenVentas r = servicio.resumenVentas(desde, hasta);
        lblIngresos.setText(Formato.dinero(r.ingresos()));
        lblVendidas.setText(String.valueOf(r.vendidas()));
        lblRenovaciones.setText(String.valueOf(r.renovaciones()));

        panelDetalle.getChildren().clear();
        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        int maximo = 0;
        for (LineaVenta l : r.lineas()) {
            maximo = Math.max(maximo, l.cantidad());
            panelDetalle.getChildren().add(Ui.filaDato(Formato.texto(l.tipo()),
                l.cantidad() + " · " + Formato.dinero(l.monto())));
            serie.getData().add(new XYChart.Data<>(Formato.texto(l.tipo()), l.cantidad()));
        }
        grafica.getData().setAll(java.util.List.of(serie));

        final int tope = maximo;
        for (XYChart.Data<String, Number> d : serie.getData()) {
            boolean esMaximo = tope > 0 && d.getYValue().intValue() == tope;
            d.nodeProperty().addListener((o, a, n) -> {
                if (n != null && esMaximo) {
                    n.setStyle("-fx-bar-fill: " + Tema.ROJO + ";");
                }
            });
            if (d.getNode() != null && esMaximo) {
                d.getNode().setStyle("-fx-bar-fill: " + Tema.ROJO + ";");
            }
        }
    }
}
