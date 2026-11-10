package com.fittrack.ui;

import com.fittrack.model.Membresia;
import com.fittrack.model.tipoMembresia;
import com.fittrack.servicio.FitTrackServicio;
import com.fittrack.servicio.MetodoPago;
import com.fittrack.servicio.SocioResumen;

import java.time.LocalDate;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Pantalla 7: Renovar membresía (tema claro).
 */
public class RenovarMembresiaView {

    private final FitTrackServicio servicio;
    private final Navegador nav;
    private final int idSocio;
    private VBox raiz;

    public RenovarMembresiaView(FitTrackServicio servicio, Navegador nav, int idSocio) {
        this.servicio = servicio;
        this.nav = nav;
        this.idSocio = idSocio;
    }

    public Node crearVista() {
        raiz = new VBox(16);
        raiz.getStyleClass().add("pantalla-clara");
        raiz.getChildren().add(Ui.encabezado("Membresías / Renovar", "Renovar membresía"));

        SocioResumen r = servicio.buscarSocio(idSocio);
        Membresia actual = r == null ? null : r.getMembresia();
        if (r == null || actual == null) {
            raiz.getChildren().add(Ui.etiqueta("Este socio no tiene una membresía para renovar.", "texto-secundario"));
            Platform.runLater(() -> {
                boolean registrar = r != null && DialogoFitTrack.confirmar(Ui.ventana(raiz), "Sin membresía",
                        "Este socio todavía no tiene membresía. Puedes registrar una nueva.", "Registrar membresía");
                if (registrar) {
                    nav.irRegistrarMembresia(idSocio);
                } else if (r == null) {
                    DialogoFitTrack.error(Ui.ventana(raiz), "Socio no encontrado",
                            "No existe ningún socio con el ID " + Formato.idSocio(idSocio) + ".");
                }
            });
            return raiz;
        }

        tipoMembresia tipo = actual.getTipo();
        Membresia nueva = new Membresia(
                0, tipo, LocalDate.now(), java.math.BigDecimal.ZERO
        );

        VBox panelActual = Ui.panel(10,
                Ui.etiqueta("Membresía actual", "subtitulo-panel"),
                Ui.filaDato("Socio", r.nombreCompleto() + " · " + Formato.idSocio(idSocio)),
                Ui.filaDato("Tipo", Formato.texto(tipo)),
                Ui.filaDato("Vence", Formato.fecha(actual.getFechaVencimiento())),
                Ui.filaDato("Estado", Ui.insigniaMembresia(actual.getEstado())));
        HBox.setHgrow(panelActual, Priority.ALWAYS);

        ComboBox<String> cmbMetodo = new ComboBox<>();
        cmbMetodo.getItems().setAll(MetodoPago.TODOS);
        cmbMetodo.setValue(MetodoPago.EFECTIVO);
        TextField txtMonto = new TextField();
        txtMonto.setPromptText("Ingrese el precio de renovación");

        Button btnRenovar = Ui.boton("Renovar y registrar pago", "boton-primario");
        btnRenovar.setOnAction(e -> renovar(nueva, cmbMetodo.getValue(), txtMonto.getText()));

        VBox panelNuevo = Ui.panel(10,
                Ui.etiqueta("Nuevo periodo", "subtitulo-panel"),
                Ui.filaDato("Inicio", Formato.fecha(nueva.getFechaInicio())),
                Ui.filaDato("Vencimiento", nueva.getFechaVencimiento() == null
                        ? "Sin definir"
                        : Formato.fecha(nueva.getFechaVencimiento())),
                Ui.rejilla(
                        Ui.conEtiqueta("Método de pago", cmbMetodo),
                        Ui.conEtiqueta("Precio de renovación", txtMonto)
                )
        );
        HBox.setHgrow(panelNuevo, Priority.ALWAYS);

        HBox cuerpo = new HBox(16, panelActual, panelNuevo);
        cuerpo.setAlignment(Pos.TOP_LEFT);
        HBox pie = new HBox(Ui.espacio(), btnRenovar);
        raiz.getChildren().addAll(cuerpo, pie);
        return raiz;
    }

    private void renovar(Membresia nueva, String metodo, String textoMonto) {
        var ventana = Ui.ventana(raiz);
        if (nueva.getFechaVencimiento() == null) {
            DialogoFitTrack.advertencia(ventana, "Duración sin definir",
                    "Falta definir la duración de la membresía \"" + Formato.texto(nueva.getTipo())
                    + "\" en Membresia.calcularVencimiento().");
            return;
        }
        Double monto = Ui.leerMonto(textoMonto);
        if (monto == null || monto <= 0) {
            DialogoFitTrack.advertencia(ventana, "Monto no válido", "Escribe un monto mayor que cero, por ejemplo 400.00.");
            return;
        }
        try {
            Membresia m = servicio.renovarMembresia(idSocio, metodo, monto);
            DialogoFitTrack.exito(ventana, "Membresía renovada",
                    "Nuevo vencimiento: " + Formato.fecha(m.getFechaVencimiento()) + ". Pago registrado por " + Formato.dinero(monto) + ".");
            nav.irPerfil(idSocio);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            DialogoFitTrack.advertencia(ventana, "No se pudo renovar", ex.getMessage());
        }
    }
}
