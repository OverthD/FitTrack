package com.fittrack.ui;

import com.fittrack.servicio.FilaPago;
import com.fittrack.servicio.FitTrackServicio;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Pantalla 9: Historial de pagos (tema claro).
 */
public class HistorialPagosView {

    private final FitTrackServicio servicio;
    private final Navegador nav;
    private final String filtroInicial;

    private final TextField txtBuscar = new TextField();
    private final TableView<FilaPago> tabla = Ui.tabla("No hay pagos que coincidan con la búsqueda.");

    public HistorialPagosView(FitTrackServicio servicio, Navegador nav, String filtroInicial) {
        this.servicio = servicio;
        this.nav = nav;
        this.filtroInicial = filtroInicial;
    }

    public Node crearVista() {
        VBox raiz = new VBox(16);
        raiz.getStyleClass().add("pantalla-clara");

        //Button btnRegistrar = Ui.boton("Registrar pago", "boton-primario");
        //btnRegistrar.setOnAction(e -> nav.irRegistrarPago(null, null));

        txtBuscar.getStyleClass().add("campo");
        txtBuscar.setPromptText("Buscar por ID o nombre");
        txtBuscar.setOnAction(e -> recargar());
        Button btnBuscar = Ui.boton("Buscar", "boton-marino");
        btnBuscar.setOnAction(e -> recargar());
        HBox.setHgrow(txtBuscar, Priority.ALWAYS);

        tabla.getColumns().add(Ui.<FilaPago>columna("Fecha", f -> Formato.fecha(f.pago().getFechaPago())));
        tabla.getColumns().add(Ui.<FilaPago>columna(
                "ID Socio",
                f -> Formato.idSocio(f.socio().getIdSocio())
        ));
        tabla.getColumns().add(Ui.<FilaPago>columna("Socio", f -> f.socio().getNombre() + " " + f.socio().getApellido()));
        tabla.getColumns().add(Ui.<FilaPago>columna("Membresía",
                f -> f.membresia() == null ? "—" : Formato.texto(f.membresia().getTipo())));
        tabla.getColumns().add(Ui.<FilaPago>columna(
                "Monto",
                f -> Formato.dinero(f.pago().getMonto().doubleValue())
        ));
        tabla.getColumns().add(Ui.<FilaPago>columna("Método", f -> f.pago().getMetodoPago()));

        raiz.getChildren().addAll(Ui.filaEncabezado("Pagos", "Historial de pagos"),
                new HBox(10, txtBuscar, btnBuscar), tabla);
        if (filtroInicial != null) {
            txtBuscar.setText(filtroInicial);
        }
        recargar();
        return raiz;
    }

    private void recargar() {
        tabla.setItems(FXCollections.observableArrayList(servicio.listarPagos(txtBuscar.getText())));
    }
}
