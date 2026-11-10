package com.fittrack.ui;

import com.fittrack.model.AcreditacionEstudiante;
import com.fittrack.model.Membresia;
import com.fittrack.model.tipoMembresia;
import com.fittrack.servicio.MetodoPago;
import com.fittrack.servicio.FitTrackServicio;
import com.fittrack.servicio.SocioResumen;
import java.time.LocalDate;
import java.util.List;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

/**
 * Pantalla 6: Registrar membresía (tema claro).
 */
public class RegistrarMembresiaView {

    public static final List<String> COMPROBANTES
            = List.of("Credencial vigente", "Constancia de estudios", "Boleta de calificaciones");

    private final FitTrackServicio servicio;
    private final Navegador nav;
    private final Integer idInicial;

    private final TextField txtIdSocio = new TextField();
    private final Label lblSocio = Ui.etiqueta("", "texto-secundario");
    private final ComboBox<tipoMembresia> cmbTipo = new ComboBox<>();
    private final DatePicker dpInicio = new DatePicker(LocalDate.now());
    private final TextField txtVence = new TextField();
    private final TextField txtPrecio = new TextField();
    private final TextField txtEstado = new TextField();

    private final DatePicker dpVerificacion = new DatePicker(LocalDate.now());
    private final DatePicker dpVigencia = new DatePicker();
    private final ComboBox<String> cmbComprobante = new ComboBox<>();
    private final TextField txtSegundo = new TextField();
    private final Label lblTotal = Ui.etiqueta("$0.00", "total-valor");

    private final ComboBox<String> cmbMetodoPago = new ComboBox<>();

    private VBox bloqueEstudiante;
    private VBox panelDos;
    private VBox raiz;

    public RegistrarMembresiaView(FitTrackServicio servicio, Navegador nav, Integer idInicial) {
        this.servicio = servicio;
        this.nav = nav;
        this.idInicial = idInicial;
    }

    public Node crearVista() {
        raiz = new VBox(16);
        raiz.getStyleClass().add("pantalla-clara");

        for (TextField t : List.of(txtVence, txtEstado)) {
            t.setEditable(false);
            t.getStyleClass().add("campo-solo-lectura");
        }

        txtPrecio.setPromptText("Introduce el precio");
        txtPrecio.setText("");
        txtPrecio.textProperty().addListener((o, anterior, nuevo) -> actualizarTotal());
        cmbTipo.getItems().setAll(tipoMembresia.values());
        cmbTipo.setConverter(new StringConverter<>() {
            @Override
            public String toString(tipoMembresia t) {
                return t == null ? "" : Formato.texto(t);
            }

            @Override
            public tipoMembresia fromString(String s) {
                return null;
            }
        });
        cmbTipo.setValue(tipoMembresia.MENSUAL);
        cmbComprobante.getItems().setAll(COMPROBANTES);
        cmbComprobante.setPromptText("Elige un comprobante");
        cmbMetodoPago.getItems().setAll(MetodoPago.TODOS);
        cmbMetodoPago.setValue(MetodoPago.EFECTIVO);
        dpVigencia.setPromptText("dd/mm/aaaa");

        txtIdSocio.setPromptText("FT-000128");
        txtIdSocio.setOnAction(e -> mostrarSocio());
        txtIdSocio.focusedProperty().addListener((o, antes, ahora) -> {
            if (!ahora) {
                mostrarSocio();
            }
        });
        cmbTipo.valueProperty().addListener((o, a, n) -> actualizar());
        dpInicio.valueProperty().addListener((o, a, n) -> actualizar());

        VBox colId = Ui.conEtiqueta("ID del socio", txtIdSocio);
        colId.getChildren().add(lblSocio);

        bloqueEstudiante = new VBox(10,
                Ui.etiqueta("Estudiante", "subtitulo-panel"),
                Ui.etiqueta("Comprobante vigente obligatorio.", "texto-secundario"),
                Ui.rejilla(Ui.conEtiqueta("Fecha de verificación", dpVerificacion),
                        Ui.conEtiqueta("Vigencia del comprobante", dpVigencia),
                        Ui.conEtiqueta("Tipo de comprobante", cmbComprobante)));
        bloqueEstudiante.getStyleClass().add("paso-siguiente");

        VBox formulario = Ui.panel(14,
                Ui.rejilla(colId, Ui.conEtiqueta("Tipo de membresía", cmbTipo),
                        Ui.conEtiqueta("Fecha de inicio", dpInicio), Ui.conEtiqueta("Vencimiento (automático)", txtVence),
                        Ui.conEtiqueta("Precio", txtPrecio), Ui.conEtiqueta("Estado inicial", txtEstado)),
                bloqueEstudiante);
        HBox.setHgrow(formulario, Priority.ALWAYS);

        panelDos = Ui.panel(10,
                Ui.etiqueta("Membresía para dos personas", "subtitulo-panel"),
                Ui.etiqueta("Ambos socios activos, precio total único.", "texto-secundario"),
                Ui.conEtiqueta("ID del segundo socio", txtSegundo));
        txtSegundo.setPromptText("FT-000041");

        VBox total = new VBox(4, Ui.etiqueta("Total a pagar", "dato-etiqueta"), lblTotal);
        total.getStyleClass().add("tarjeta-total");
        total.getChildren().get(0).setStyle("-fx-text-fill: white;");

        Button btnGuardar = Ui.boton("Guardar y registrar pago", "boton-primario");
        btnGuardar.setMaxWidth(Double.MAX_VALUE);
        btnGuardar.setOnAction(e -> guardar());

        VBox derecha = new VBox(
                16,
                panelDos,
                Ui.conEtiqueta("Método de pago", cmbMetodoPago),
                total,
                btnGuardar
        );
        derecha.setPrefWidth(300);
        derecha.setMinWidth(260);

        HBox cuerpo = new HBox(16, formulario, derecha);
        cuerpo.setAlignment(Pos.TOP_LEFT);
        raiz.getChildren().addAll(Ui.encabezado("Membresías", "Registrar membresía"), cuerpo);

        if (idInicial != null) {
            txtIdSocio.setText(Formato.idSocio(idInicial));
            mostrarSocio();
        }
        actualizar();
        return raiz;
    }

    private SocioResumen leerSocio(String texto) {
        try {
            return servicio.buscarSocio(Formato.leerIdSocio(texto));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void mostrarSocio() {
        if (txtIdSocio.getText().isBlank()) {
            lblSocio.setText("");
            return;
        }
        SocioResumen r = leerSocio(txtIdSocio.getText());
        lblSocio.setText(r == null ? "No se encontró el socio." : r.nombreCompleto() + " · " + Formato.texto(r.getSocio().getEstado()));
    }

    private void actualizar() {
        tipoMembresia tipo = cmbTipo.getValue();
        LocalDate inicio = dpInicio.getValue();

        boolean estudiante = tipo == tipoMembresia.ESTUDIANTE;
        boolean dos = tipo == tipoMembresia.DOSPERSONAS;

        bloqueEstudiante.setVisible(estudiante);
        bloqueEstudiante.setManaged(estudiante);

        panelDos.setVisible(dos);
        panelDos.setManaged(dos);

        if (tipo == null || inicio == null) {
            return;
        }

        Membresia vista = new Membresia(
                0,
                tipo,
                inicio,
                java.math.BigDecimal.ZERO
        );

        txtVence.setText(
                vista.getFechaVencimiento() == null
                ? "Sin definir"
                : Formato.fecha(vista.getFechaVencimiento())
        );

        txtEstado.setText(Formato.texto(vista.getEstado()));

        actualizarTotal();
    }

    private void actualizarTotal() {
        try {
            String texto = txtPrecio.getText().trim();

            if (texto.isEmpty()) {
                lblTotal.setText("$0.00");
                return;
            }

            java.math.BigDecimal precio
                    = new java.math.BigDecimal(texto);

            if (precio.signum() < 0) {
                lblTotal.setText("$0.00");
                return;
            }

            lblTotal.setText(Formato.dinero(precio.doubleValue()));

        } catch (NumberFormatException ex) {
            lblTotal.setText("$0.00");
        }
    }

    private void guardar() {
        var ventana = Ui.ventana(raiz);
        SocioResumen socio = leerSocio(txtIdSocio.getText());
        if (socio == null) {
            DialogoFitTrack.advertencia(ventana, "Socio no encontrado", "Escribe un ID de socio que exista, por ejemplo FT-000128.");
            return;
        }
        tipoMembresia tipo = cmbTipo.getValue();
        LocalDate inicio = dpInicio.getValue();
        if (tipo == null || inicio == null) {
            DialogoFitTrack.advertencia(ventana, "Faltan datos obligatorios", "Elige el tipo de membresía y la fecha de inicio.");
            return;
        }

        java.math.BigDecimal precio;

        try {
            precio = new java.math.BigDecimal(txtPrecio.getText().trim());

            if (precio.compareTo(java.math.BigDecimal.ZERO) <= 0) {
                throw new NumberFormatException();
            }

        } catch (NumberFormatException ex) {
            DialogoFitTrack.advertencia(
                    ventana,
                    "Precio no válido",
                    "Introduce un precio numérico mayor que cero."
            );
            return;
        }
        Membresia vista = new Membresia(
                0, tipo, inicio, java.math.BigDecimal.ZERO
        );

        if (vista.getFechaVencimiento() == null) {
            DialogoFitTrack.advertencia(ventana, "Duración sin definir",
                    "Falta definir la duración de la membresía \"" + Formato.texto(tipo)
                    + "\" en Membresia.calcularVencimiento().");
            return;
        }

        LocalDate verificacion = null;
        LocalDate vigencia = null;
        String comprobante = null;
        if (tipo == tipoMembresia.ESTUDIANTE) {
            verificacion = dpVerificacion.getValue();
            vigencia = dpVigencia.getValue();
            comprobante = cmbComprobante.getValue();
            if (verificacion == null || vigencia == null || cmbComprobante.getValue() == null) {
                DialogoFitTrack.advertencia(ventana, "Faltan datos obligatorios",
                        "Completa la verificación, la vigencia y el tipo de comprobante del estudiante.");
                return;
            }
            if (!vigencia.isAfter(verificacion)) {
                DialogoFitTrack.advertencia(ventana, "Vigencia no válida", "La vigencia debe ser posterior a la fecha de verificación.");
                return;
            }
            if (vigencia.isBefore(LocalDate.now())) {
                DialogoFitTrack.advertencia(ventana, "Comprobante vencido", "El comprobante ya no está vigente. Pide uno nuevo.");
                return;
            }
            //acreditacion = new AcreditacionEstudiante(0, verificacion, vigencia, cmbComprobante.getValue());
        }

        Integer idSegundo = null;
        if (tipo == tipoMembresia.DOSPERSONAS) {
            SocioResumen segundo = leerSocio(txtSegundo.getText());
            if (segundo == null) {
                DialogoFitTrack.advertencia(ventana, "Segundo socio no encontrado", "Escribe el ID de un segundo socio que exista.");
                return;
            }
            idSegundo = segundo.getSocio().getIdSocio();
        }

        Membresia actual = socio.getMembresia();
        if (actual != null && actual.permitirAcceso()) {
            boolean seguir = DialogoFitTrack.confirmar(ventana, "Ya tiene una membresía activa",
                    socio.nombreCompleto() + " ya tiene una membresía " + Formato.texto(actual.getTipo()).toLowerCase()
                    + " vigente hasta el " + Formato.fecha(actual.getFechaVencimiento()) + ". ¿Registrar otra?", "Registrar otra");
            if (!seguir) {
                return;
            }
        }
        try {
            Membresia m = servicio.registrarMembresia(
                    socio.getSocio().getIdSocio(),
                    tipo,
                    inicio,
                    idSegundo,
                    verificacion,
                    vigencia,
                    comprobante,
                    cmbMetodoPago.getValue(),
                    precio
            );

            DialogoFitTrack.exito(
                    ventana,
                    "Membresía registrada",
                    "La membresía se registró correctamente y el pago quedó asociado."
            );

            nav.irPerfil(socio.getSocio().getIdSocio());

        } catch (IllegalArgumentException | IllegalStateException ex) {
            DialogoFitTrack.advertencia(
                    ventana,
                    "No se pudo registrar",
                    ex.getMessage()
            );
        }
    }
}
