package com.fittrack.ui;

import com.fittrack.model.Socio;
import com.fittrack.servicio.FitTrackServicio;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/** Pantalla 4: Registrar socio (tema claro). */
public class RegistrarSocioView {

    private final FitTrackServicio servicio;
    private final Navegador nav;

    private final TextField txtNombre = new TextField();
    private final TextField txtApellido = new TextField();
    private final TextField txtTelefono = new TextField();
    private final TextField txtCorreo = new TextField();
    private VBox raiz;

    public RegistrarSocioView(FitTrackServicio servicio, Navegador nav) {
        this.servicio = servicio;
        this.nav = nav;
    }

    /** Devuelve el texto del problema, o null si los datos están bien. Lo usa también el perfil. */
    public static String validar(String nombre, String apellido, String telefono, String correo) {
        if (nombre.isBlank() || apellido.isBlank() || telefono.isBlank()) {
            return "Completa nombre, apellido y teléfono para guardar al socio.";
        }
        String digitos = telefono.replaceAll("\\D", "");
        if (digitos.length() < 10) {
            return "El teléfono debe tener al menos 10 dígitos.";
        }
        if (!correo.isBlank() && !correo.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            return "El correo electrónico no tiene un formato válido.";
        }
        return null;
    }

    public Node crearVista() {
        raiz = new VBox(16);
        raiz.getStyleClass().add("pantalla-clara");

        Label nota = Ui.etiqueta("El ID único y la fecha de registro se asignan automáticamente. Estado inicial: Activo.",
            "paso-siguiente");
        nota.setWrapText(true);

        Button btnCancelar = Ui.boton("Cancelar", "boton-secundario");
        btnCancelar.setOnAction(e -> nav.ir("socios"));
        Button btnGuardar = Ui.boton("Guardar socio", "boton-primario");
        btnGuardar.setOnAction(e -> guardar());
        HBox botones = new HBox(10, Ui.espacio(), btnCancelar, btnGuardar);

        VBox formulario = Ui.panel(14,
            Ui.rejilla(Ui.conEtiqueta("Nombre", txtNombre), Ui.conEtiqueta("Apellido", txtApellido),
                       Ui.conEtiqueta("Teléfono", txtTelefono), Ui.conEtiqueta("Correo electrónico", txtCorreo)),
            nota, botones);
        HBox.setHgrow(formulario, Priority.ALWAYS);

        VBox siguientes = Ui.panel(10,
            Ui.etiqueta("Después de guardar", "subtitulo-panel"),
            Ui.etiqueta("Asociar una membresía", "paso-siguiente"),
            Ui.etiqueta("Registrar el primer pago", "paso-siguiente"),
            Ui.etiqueta("Validar su acceso", "paso-siguiente"));
        siguientes.setPrefWidth(280);
        siguientes.setMinWidth(240);
        siguientes.setMaxHeight(javafx.scene.layout.Region.USE_PREF_SIZE);

        HBox cuerpo = new HBox(16, formulario, siguientes);
        cuerpo.setAlignment(Pos.TOP_LEFT);
        raiz.getChildren().addAll(Ui.encabezado("Socios / Nuevo", "Registrar socio"), cuerpo);
        return raiz;
    }


    private void guardar() {
        String problema = validar(txtNombre.getText().trim(), txtApellido.getText().trim(),
            txtTelefono.getText().trim(), txtCorreo.getText().trim());
        if (problema != null) {
            DialogoFitTrack.advertencia(Ui.ventana(raiz), "Revisa los datos", problema);
            return;
        }
        try {
            Socio s = servicio.registrarSocio(txtNombre.getText().trim(), txtApellido.getText().trim(),
                txtCorreo.getText().trim(), txtTelefono.getText().trim());
            boolean asociar = DialogoFitTrack.mostrar(Ui.ventana(raiz), DialogoFitTrack.Tipo.EXITO,
                "Socio registrado",
                "ID asignado: " + Formato.idSocio(s.getIdSocio()) + ". Ahora puedes asociar una membresía.",
                "Asociar membresía", "Cerrar");
            if (asociar) {
                nav.irRegistrarMembresia(s.getIdSocio());
            } else {
                nav.irPerfil(s.getIdSocio());
            }
        } catch (RuntimeException ex) {
            DialogoFitTrack.error(Ui.ventana(raiz), "No se pudo guardar", ex.getMessage());
        }
    }
}
