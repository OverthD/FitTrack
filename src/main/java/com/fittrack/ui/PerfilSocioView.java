package com.fittrack.ui;

import com.fittrack.model.Membresia;
import com.fittrack.model.Socio;
import com.fittrack.model.estadoSocio;
import com.fittrack.servicio.FitTrackServicio;
import com.fittrack.servicio.SocioResumen;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/** Pantalla 5: Perfil del socio (tema claro). */
public class PerfilSocioView {

    private final FitTrackServicio servicio;
    private final Navegador nav;
    private final int idSocio;
    private VBox raiz;

    public PerfilSocioView(FitTrackServicio servicio, Navegador nav, int idSocio) {
        this.servicio = servicio;
        this.nav = nav;
        this.idSocio = idSocio;
    }

    public Node crearVista() {
        raiz = new VBox(16);
        raiz.getStyleClass().add("pantalla-clara");

        SocioResumen r = servicio.buscarSocio(idSocio);
        if (r == null) {
            raiz.getChildren().add(Ui.etiqueta("No se encontró el socio.", "texto-secundario"));
            Platform.runLater(() -> DialogoFitTrack.error(Ui.ventana(raiz), "Socio no encontrado",
                "No existe ningún socio con el ID " + Formato.idSocio(idSocio) + "."));
            return raiz;
        }
        Socio s = r.getSocio();

        Button btnPagos = Ui.boton("Ver pagos", "boton-secundario");
        btnPagos.setOnAction(e -> nav.irPagosDe(idSocio));
        Button btnAccesos = Ui.boton("Ver accesos", "boton-secundario");
        btnAccesos.setOnAction(e -> nav.irAccesosDe(idSocio));

        HBox cuerpo = new HBox(16, panelDatos(s), columnaDerecha(r));
        cuerpo.setAlignment(Pos.TOP_LEFT);

        raiz.getChildren().addAll(
            Ui.filaEncabezado("Socios / " + Formato.idSocio(idSocio), r.nombreCompleto(), btnPagos, btnAccesos),
            cuerpo);
        return raiz;
    }

    private VBox panelDatos(Socio s) {
        TextField txtNombre = new TextField(s.getNombre());
        TextField txtApellido = new TextField(s.getApellido());
        TextField txtTelefono = new TextField(s.getTelefono());
        TextField txtCorreo = new TextField(s.getCorreo());
        TextField txtId = new TextField(Formato.idSocio(s.getIdSocio()));
        txtId.setEditable(false);
        txtId.getStyleClass().add("campo-solo-lectura");
        TextField txtFecha = new TextField(Formato.fecha(s.getFechaRegistro()));
        txtFecha.setEditable(false);
        txtFecha.getStyleClass().add("campo-solo-lectura");

        Button btnGuardar = Ui.boton("Guardar cambios", "boton-primario");
        btnGuardar.setOnAction(e -> {
            String problema = RegistrarSocioView.validar(txtNombre.getText().trim(), txtApellido.getText().trim(),
                txtTelefono.getText().trim(), txtCorreo.getText().trim());
            if (problema != null) {
                DialogoFitTrack.advertencia(Ui.ventana(raiz), "Revisa los datos", problema);
                return;
            }
            try {
                servicio.actualizarSocio(idSocio, txtNombre.getText().trim(), txtApellido.getText().trim(),
                    txtCorreo.getText().trim(), txtTelefono.getText().trim());
                DialogoFitTrack.exito(Ui.ventana(raiz), "Cambios guardados", "Los datos del socio se actualizaron.");
                nav.irPerfil(idSocio);
            } catch (RuntimeException ex) {
                DialogoFitTrack.error(Ui.ventana(raiz), "No se pudo guardar", ex.getMessage());
            }
        });
        HBox pie = new HBox(Ui.espacio(), btnGuardar);

        VBox panel = Ui.panel(14,
            Ui.etiqueta("Datos del socio", "subtitulo-panel"),
            Ui.rejilla(Ui.conEtiqueta("Nombre", txtNombre), Ui.conEtiqueta("Apellido", txtApellido),
                       Ui.conEtiqueta("Teléfono", txtTelefono), Ui.conEtiqueta("Correo", txtCorreo),
                       Ui.conEtiqueta("ID (no editable)", txtId), Ui.conEtiqueta("Fecha de registro", txtFecha)),
            pie);
        HBox.setHgrow(panel, Priority.ALWAYS);
        return panel;
    }

    private VBox columnaDerecha(SocioResumen r) {
        Socio s = r.getSocio();
        Membresia m = r.getMembresia();

        TextField txtMotivo = new TextField();
        HBox botonesEstado = new HBox(8);
        if (s.getEstado() == estadoSocio.ACTIVO) {
            botonesEstado.getChildren().add(botonEstado("Inactivar", estadoSocio.INACTIVO, "boton-marino", txtMotivo, true));
        } else {
            botonesEstado.getChildren().add(botonEstado("Reactivar", estadoSocio.ACTIVO, "boton-marino", txtMotivo, false));
        }
        if (s.getEstado() != estadoSocio.BAJA) {
            botonesEstado.getChildren().add(botonEstado("Dar de baja", estadoSocio.BAJA, "boton-primario", txtMotivo, true));
        }

        VBox panelEstado = Ui.panel(10,
            Ui.etiqueta("Estado del socio", "subtitulo-panel"),
            new HBox(Ui.insigniaSocio(s.getEstado())),
            Ui.conEtiqueta("Motivo del cambio", txtMotivo),
            botonesEstado);

        VBox panelMembresia = Ui.panel(8, Ui.etiqueta("Membresía", "subtitulo-panel"));
        if (m == null) {
            panelMembresia.getChildren().add(Ui.etiqueta("Este socio no tiene membresía.", "texto-secundario"));
            Button btn = Ui.boton("Registrar membresía", "boton-marino");
            btn.setOnAction(e -> nav.irRegistrarMembresia(idSocio));
            btn.setDisable(s.getEstado() != estadoSocio.ACTIVO);
            panelMembresia.getChildren().add(btn);
        } else {
            panelMembresia.getChildren().addAll(
                Ui.filaDato("Tipo", Formato.texto(m.getTipo())),
                Ui.filaDato("Vence", Formato.fecha(m.getFechaVencimiento())),
                Ui.filaDato("Estado", Ui.insigniaMembresia(m.getEstado())));
            if (r.getIdSegundoSocio() != null) {
                panelMembresia.getChildren().add(Ui.filaDato("Segundo socio", Formato.idSocio(r.getIdSegundoSocio())));
            }
            Button btn = Ui.boton("Renovar", "boton-marino");
            btn.setOnAction(e -> nav.irRenovar(idSocio));
            panelMembresia.getChildren().add(btn);
        }

        VBox columna = new VBox(16, panelEstado, panelMembresia);
        columna.setPrefWidth(300);
        columna.setMinWidth(260);
        return columna;
    }

    private Button botonEstado(String texto, estadoSocio nuevo, String clase, TextField motivo, boolean exigeMotivo) {
        Button b = Ui.boton(texto, clase);
        b.setOnAction(e -> {
            String m = motivo.getText().trim();
            if (exigeMotivo && m.isEmpty()) {
                DialogoFitTrack.advertencia(Ui.ventana(raiz), "Falta el motivo",
                    "Escribe el motivo del cambio antes de continuar.");
                return;
            }
            boolean ok = DialogoFitTrack.confirmar(Ui.ventana(raiz), texto + " socio",
                "El estado del socio cambiará a " + Formato.texto(nuevo) + ".", texto);
            if (!ok) {
                return;
            }
            try {
                servicio.cambiarEstadoSocio(idSocio, nuevo, m);
                nav.irPerfil(idSocio);
            } catch (RuntimeException ex) {
                DialogoFitTrack.error(Ui.ventana(raiz), "No se pudo cambiar el estado", ex.getMessage());
            }
        });
        return b;
    }
}
