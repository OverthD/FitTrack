package com.fittrack.ui;

import com.fittrack.model.Membresia;
import com.fittrack.servicio.FitTrackServicio;
import com.fittrack.servicio.MetodoPago;
import com.fittrack.servicio.SocioResumen;
import java.time.LocalDate;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/** Pantalla 8: Registrar pago (tema claro). */
public class RegistrarPagoView {

    private final FitTrackServicio servicio;
    private final Navegador nav;
    private final Integer idInicial;
    private final Double montoInicial;

    private final TextField txtIdSocio = new TextField();
    private final TextField txtMembresia = new TextField();
    private final DatePicker dpFecha = new DatePicker(LocalDate.now());
    private final TextField txtMonto = new TextField();
    private final ToggleGroup metodos = new ToggleGroup();

    private final Label resSocio = Ui.etiqueta("—", "dato-fila-valor");
    private final Label resMembresia = Ui.etiqueta("—", "dato-fila-valor");
    private final Label resMonto = Ui.etiqueta("—", "dato-fila-valor");
    private final Label resMetodo = Ui.etiqueta(MetodoPago.EFECTIVO, "dato-fila-valor");
    private VBox raiz;

    public RegistrarPagoView(FitTrackServicio servicio, Navegador nav, Integer idInicial, Double montoInicial) {
        this.servicio = servicio;
        this.nav = nav;
        this.idInicial = idInicial;
        this.montoInicial = montoInicial;
    }

    public Node crearVista() {
        raiz = new VBox(16);
        raiz.getStyleClass().add("pantalla-clara");

        txtMembresia.setEditable(false);
        txtMembresia.getStyleClass().add("campo-solo-lectura");
        txtIdSocio.setPromptText("FT-000128");
        txtMonto.setPromptText("0.00");

        HBox pildoras = new HBox(8);
        for (String m : MetodoPago.TODOS) {
            ToggleButton t = Ui.pildora(m);
            t.setUserData(m);
            t.setToggleGroup(metodos);
            t.setSelected(m.equals(MetodoPago.EFECTIVO));
            pildoras.getChildren().add(t);
        }
        metodos.selectedToggleProperty().addListener((o, anterior, nuevo) -> {
            if (nuevo == null) {
                anterior.setSelected(true);
            } else {
                resMetodo.setText((String) nuevo.getUserData());
            }
        });

        txtIdSocio.setOnAction(e -> consultarSocio(true));
        txtIdSocio.focusedProperty().addListener((o, antes, ahora) -> {
            if (!ahora) {
                consultarSocio(txtMonto.getText().isBlank());
            }
        });
        txtMonto.textProperty().addListener((o, a, n) -> {
            Double v = Ui.leerMonto(n);
            resMonto.setText(v == null || v <= 0 ? "—" : Formato.dinero(v));
        });

        Button btnRegistrar = Ui.boton("Registrar pago", "boton-primario");
        btnRegistrar.setOnAction(e -> registrar());
        HBox pie = new HBox(Ui.espacio(), btnRegistrar);

        VBox formulario = Ui.panel(14,
            Ui.rejilla(Ui.conEtiqueta("ID del socio", txtIdSocio), Ui.conEtiqueta("Membresía", txtMembresia),
                       Ui.conEtiqueta("Fecha del pago", dpFecha), Ui.conEtiqueta("Monto pagado", txtMonto)),
            new VBox(4, Ui.etiqueta("Método de pago", "etiqueta-campo"), pildoras),
            pie);
        HBox.setHgrow(formulario, Priority.ALWAYS);

        VBox resumen = Ui.panel(10,
            Ui.etiqueta("Resumen", "subtitulo-panel"),
            Ui.filaDato("Socio", resSocio), Ui.filaDato("Membresía", resMembresia),
            Ui.filaDato("Monto", resMonto), Ui.filaDato("Método", resMetodo));
        resumen.setPrefWidth(320);
        resumen.setMinWidth(260);

        HBox cuerpo = new HBox(16, formulario, resumen);
        cuerpo.setAlignment(Pos.TOP_LEFT);
        raiz.getChildren().addAll(Ui.encabezado("Pagos", "Registrar pago"), cuerpo);

        if (idInicial != null) {
            txtIdSocio.setText(Formato.idSocio(idInicial));
            if (montoInicial != null) {
                txtMonto.setText(Ui.monto(montoInicial));
            }
            consultarSocio(montoInicial == null);
        }
        return raiz;
    }

    private SocioResumen leerSocio() {
        try {
            return servicio.buscarSocio(Formato.leerIdSocio(txtIdSocio.getText()));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Llena la membresía y el resumen. Si llenarMonto, pone el precio de la membresía en el monto. */
    private void consultarSocio(boolean llenarMonto) {
        SocioResumen r = leerSocio();
        if (r == null) {
            txtMembresia.setText("");
            resSocio.setText("—");
            resMembresia.setText("—");
            return;
        }
        Membresia m = r.getMembresia();
        resSocio.setText(r.nombreCompleto());
        if (m == null) {
            txtMembresia.setText("Sin membresía");
            resMembresia.setText("Sin membresía");
        } else {
            String venc = m.getFechaVencimiento() == null ? "sin vencimiento" : "vence " + Formato.fecha(m.getFechaVencimiento());
            txtMembresia.setText(Formato.texto(m.getTipo()) + " · " + venc);
            resMembresia.setText(Formato.texto(m.getTipo()));
            if (llenarMonto) {
                txtMonto.setText(Ui.monto(servicio.precioDe(m.getTipo())));
            }
        }
    }

    private void registrar() {
        var ventana = Ui.ventana(raiz);
        SocioResumen r = leerSocio();
        if (r == null) {
            DialogoFitTrack.advertencia(ventana, "Socio no encontrado", "Escribe un ID de socio que exista, por ejemplo FT-000128.");
            return;
        }
        Double monto = Ui.leerMonto(txtMonto.getText());
        if (monto == null || monto <= 0) {
            DialogoFitTrack.advertencia(ventana, "Monto no válido", "Escribe un monto mayor que cero, por ejemplo 400.00.");
            return;
        }
        if (dpFecha.getValue() == null) {
            DialogoFitTrack.advertencia(ventana, "Falta la fecha", "Elige la fecha del pago.");
            return;
        }
        String metodo = (String) metodos.getSelectedToggle().getUserData();
        try {
            servicio.registrarPago(r.getSocio().getIdSocio(), monto, metodo, dpFecha.getValue());
            boolean validar = DialogoFitTrack.mostrar(ventana, DialogoFitTrack.Tipo.EXITO, "Pago registrado",
                metodo + " · " + Formato.dinero(monto) + " · " + Formato.fecha(dpFecha.getValue())
                    + ". Pago de " + r.nombreCompleto() + ".",
                "Validar acceso", "Ver pagos");
            if (validar) {
                nav.irValidarAcceso(r.getSocio().getIdSocio());
            } else {
                nav.ir("pagos");
            }
        } catch (IllegalArgumentException | IllegalStateException ex) {
            DialogoFitTrack.advertencia(ventana, "No se pudo registrar el pago", ex.getMessage());
        }
    }
}
