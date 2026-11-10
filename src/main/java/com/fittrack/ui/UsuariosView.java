package com.fittrack.ui;

import com.fittrack.model.UsuarioSistema;
import com.fittrack.model.rolUsuario;
import com.fittrack.servicio.FitTrackServicio;
import com.fittrack.util.SesionUsuario;

import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;

/**
 * Pantalla 14: Administración de usuarios del sistema.
 */
public class UsuariosView {

    private final FitTrackServicio servicio;

    private final TableView<UsuarioSistema> tabla
            = Ui.tabla("Todavía no hay usuarios.");

    private VBox raiz;
    private VBox contenido;

    // Formularios de creación.
    private final TextField txtIdCrear = new TextField();
    private final ComboBox<rolUsuario> cmbRolCrear = new ComboBox<>();
    private final TextField txtUsuarioCrear = new TextField();
    private final PasswordField txtContrasenaCrear = new PasswordField();

    // Formularios de modificación.
    private final TextField txtIdEditar = new TextField();
    private final ComboBox<rolUsuario> cmbRolEditar = new ComboBox<>();
    private final TextField txtUsuarioEditar = new TextField();
    private final PasswordField txtContrasenaNueva = new PasswordField();

    private UsuarioSistema usuarioSeleccionado;

    private final ToggleGroup grupoSecciones = new ToggleGroup();

    private ToggleButton btnUsuarios;
    private ToggleButton btnCrear;
    private ToggleButton btnModificar;

    public UsuariosView(FitTrackServicio servicio) {
        this.servicio = servicio;
    }

    public Node crearVista() {

        raiz = new VBox(16);
        raiz.getStyleClass().add("pantalla-clara");

        configurarTabla();
        configurarFormularios();

        btnUsuarios = Ui.pildora("Usuarios");
        btnCrear = Ui.pildora("Crear usuario");
        btnModificar = Ui.pildora("Modificar usuario");

        btnUsuarios.setToggleGroup(grupoSecciones);
        btnCrear.setToggleGroup(grupoSecciones);
        btnModificar.setToggleGroup(grupoSecciones);

        btnUsuarios.setOnAction(e -> mostrarUsuarios());
        btnCrear.setOnAction(e -> mostrarCrear());

// No permite pulsar directamente esta opción.
        btnModificar.setMouseTransparent(true);
        btnModificar.setFocusTraversable(false);
        btnModificar.setOpacity(0.45);

        HBox navegacion = new HBox(
                8,
                btnUsuarios,
                btnCrear,
                btnModificar
        );

        navegacion.setAlignment(Pos.CENTER_LEFT);

        contenido = new VBox(16);
        VBox.setVgrow(contenido, Priority.ALWAYS);

        raiz.getChildren().addAll(
                Ui.encabezado(
                        "Administración",
                        "Usuarios del sistema"
                ),
                navegacion,
                contenido
        );

        recargar();
        mostrarUsuarios();

        return raiz;
    }

    // --------------------------------------------------
    // CONFIGURACIÓN
    // --------------------------------------------------
    private void configurarTabla() {

        tabla.getColumns().add(
                Ui.<UsuarioSistema>columna(
                        "ID",
                        u -> Formato.idUsuario(u.getIdEmpleado())
                )
        );

        tabla.getColumns().add(
                Ui.<UsuarioSistema>columna(
                        "Usuario",
                        UsuarioSistema::getUsuario
                )
        );

        tabla.getColumns().add(
                Ui.<UsuarioSistema>columna(
                        "Rol",
                        u -> Formato.texto(u.getRol())
                )
        );

        tabla.getColumns().add(
                Ui.<UsuarioSistema>columnaNodo(
                        "Estado",
                        u -> u.isActivo()
                        ? Ui.insignia("Activo", "insignia-activo")
                        : Ui.insignia("Inactivo", "insignia-inactivo")
                )
        );

        tabla.getColumns().add(
                Ui.<UsuarioSistema>columnaNodo(
                        "Modificar",
                        this::botonModificar
                )
        );

        tabla.getColumns().add(
                Ui.<UsuarioSistema>columnaNodo(
                        "Acción",
                        this::botonAccion
                )
        );

        HBox.setHgrow(tabla, Priority.ALWAYS);
    }

    private void configurarFormularios() {

        txtIdCrear.setEditable(false);
        txtIdCrear.getStyleClass().add("campo-solo-lectura");

        txtIdEditar.setEditable(false);
        txtIdEditar.getStyleClass().add("campo-solo-lectura");

        configurarCombo(cmbRolCrear);
        configurarCombo(cmbRolEditar);

        cmbRolCrear.setValue(rolUsuario.EMPLEADO);

        txtUsuarioCrear.setPromptText("recepcion03");
        txtContrasenaCrear.setPromptText("Mínimo 8 caracteres");

        txtUsuarioEditar.setPromptText("Nombre de usuario");
        txtContrasenaNueva.setPromptText("Nueva contraseña (mínimo 8 caracteres)");
    }

    private void configurarCombo(ComboBox<rolUsuario> combo) {

        combo.getItems().setAll(rolUsuario.values());

        combo.setConverter(new StringConverter<>() {

            @Override
            public String toString(rolUsuario rol) {
                return rol == null ? "" : Formato.texto(rol);
            }

            @Override
            public rolUsuario fromString(String texto) {
                return null;
            }
        });
    }

    // --------------------------------------------------
    // NAVEGACIÓN SUPERIOR
    // --------------------------------------------------
    private void mostrarUsuarios() {

        usuarioSeleccionado = null;

        actualizarBotones("usuarios");

        VBox.setVgrow(tabla, Priority.ALWAYS);

        contenido.getChildren().setAll(tabla);
    }

    /*
    panelInformacion.setPrefWidth (

    300);
    panelInformacion.setMinWidth (
    260);

        HBox cuerpo = new HBox(16, tabla, panelInformacion);

    cuerpo.setAlignment (Pos.TOP_LEFT);

    contenido.getChildren () 
        .setAll(cuerpo);
    }
     */
    private void mostrarCrear() {

        actualizarBotones("crear");

        Button btnCrearCuenta = Ui.boton(
                "Crear cuenta",
                "boton-primario"
        );

        btnCrearCuenta.setOnAction(e -> crear());

        HBox pie = new HBox(
                Ui.espacio(),
                btnCrearCuenta
        );

        VBox formulario = Ui.panel(16,
                Ui.etiqueta("Nuevo usuario", "subtitulo-panel"),
                Ui.rejilla(
                        Ui.conEtiqueta("ID (automático)", txtIdCrear),
                        Ui.conEtiqueta("Rol", cmbRolCrear),
                        Ui.conEtiqueta("Usuario", txtUsuarioCrear),
                        Ui.conEtiqueta("Contraseña", txtContrasenaCrear)
                ),
                pie
        );

        formulario.setMaxWidth(Double.MAX_VALUE);

        contenido.getChildren().setAll(formulario);
    }

    private void mostrarModificar(UsuarioSistema usuario) {

        if (usuario == null) {
            return;
        }

        usuarioSeleccionado = usuario;

        txtIdEditar.setText(
                Formato.idUsuario(usuario.getIdEmpleado())
        );

        txtUsuarioEditar.setText(usuario.getUsuario());
        cmbRolEditar.setValue(usuario.getRol());
        txtContrasenaNueva.clear();

        actualizarBotones("modificar");

        Button btnGuardar = Ui.boton(
                "Guardar cambios",
                "boton-primario"
        );

        btnGuardar.setOnAction(e -> guardarDatos());

        Button btnCambiarContrasena = Ui.boton(
                "Cambiar contraseña",
                "boton-marino"
        );

        btnCambiarContrasena.setOnAction(e -> cambiarContrasena());

        VBox formulario = Ui.panel(16,
                Ui.etiqueta("Datos del usuario", "subtitulo-panel"),
                Ui.rejilla(
                        Ui.conEtiqueta("ID (no editable)", txtIdEditar),
                        Ui.conEtiqueta("Rol", cmbRolEditar),
                        Ui.conEtiqueta("Nombre de usuario", txtUsuarioEditar)
                ),
                new HBox(Ui.espacio(), btnGuardar),
                Ui.etiqueta("Seguridad", "subtitulo-panel"),
                Ui.rejilla(
                        Ui.conEtiqueta("Nueva contraseña", txtContrasenaNueva)
                ),
                new HBox(Ui.espacio(), btnCambiarContrasena)
        );

        formulario.setMaxWidth(Double.MAX_VALUE);

        contenido.getChildren().setAll(formulario);
    }

    private void actualizarBotones(String apartado) {

        btnUsuarios.setSelected(apartado.equals("usuarios"));
        btnCrear.setSelected(apartado.equals("crear"));
        btnModificar.setSelected(apartado.equals("modificar"));

        // Modificar siempre es inaccesible mediante clic.
        btnModificar.setMouseTransparent(true);
        btnModificar.setFocusTraversable(false);

        // Atenuar cuando no está seleccionado.
        btnModificar.setOpacity(
                apartado.equals("modificar") ? 1.0 : 0.45
        );
    }

    // --------------------------------------------------
    // ACCIONES DE LA TABLA
    // --------------------------------------------------
    private Node botonModificar(UsuarioSistema usuario) {

        Button boton = Ui.boton(
                "Modificar",
                "boton-secundario"
        );

        boton.setStyle("-fx-padding: 5 14;");
        boton.setOnAction(e -> mostrarModificar(usuario));

        return boton;
    }

    private Node botonAccion(UsuarioSistema usuario) {

        Button boton = Ui.boton(
                usuario.isActivo() ? "Desactivar" : "Activar",
                "boton-secundario"
        );

        boton.setStyle("-fx-padding: 5 14;");
        boton.setOnAction(e -> cambiarActivo(usuario));

        return boton;
    }

    private void cambiarActivo(UsuarioSistema usuario) {

        var ventana = Ui.ventana(raiz);
        boolean nuevoEstado = !usuario.isActivo();

        int idUsuarioActual = SesionUsuario.obtenerIdEmpleado();

        if (!nuevoEstado
                && idUsuarioActual == usuario.getIdEmpleado()) {

            DialogoFitTrack.advertencia(
                    ventana,
                    "No puedes desactivarte",
                    "Estás usando esta cuenta. Pide a otro administrador que la desactive."
            );

            return;
        }

        String accion = nuevoEstado ? "Activar" : "Desactivar";

        boolean confirmado = DialogoFitTrack.confirmar(
                ventana,
                accion + " usuario",
                "La cuenta " + usuario.getUsuario()
                + " quedará " + (nuevoEstado ? "activa" : "inactiva") + ".",
                accion
        );

        if (!confirmado) {
            return;
        }

        try {

            servicio.cambiarActivoUsuario(
                    usuario.getIdEmpleado(),
                    nuevoEstado
            );

            recargar();

        } catch (RuntimeException ex) {

            DialogoFitTrack.error(
                    ventana,
                    "No se pudo cambiar el estado",
                    ex.getMessage()
            );
        }
    }

    // --------------------------------------------------
    // CREACIÓN
    // --------------------------------------------------
    private void crear() {

        var ventana = Ui.ventana(raiz);

        String usuario = txtUsuarioCrear.getText().trim();
        String contrasena = txtContrasenaCrear.getText();

        if (usuario.length() < 3 || usuario.contains(" ")) {

            DialogoFitTrack.advertencia(
                    ventana,
                    "Usuario no válido",
                    "El usuario debe tener al menos 3 caracteres y no llevar espacios."
            );

            return;
        }

        if (contrasena.length() < 8) {

            DialogoFitTrack.advertencia(
                    ventana,
                    "Contraseña muy corta",
                    "Usa una contraseña de al menos 8 caracteres."
            );

            return;
        }

        try {

            servicio.crearUsuario(
                    cmbRolCrear.getValue(),
                    usuario,
                    contrasena
            );

            DialogoFitTrack.exito(
                    ventana,
                    "Cuenta creada",
                    "El usuario " + usuario + " ya puede iniciar sesión."
            );

            txtUsuarioCrear.clear();
            txtContrasenaCrear.clear();
            cmbRolCrear.setValue(rolUsuario.EMPLEADO);

            recargar();
            mostrarCrear();

        } catch (RuntimeException ex) {

            DialogoFitTrack.advertencia(
                    ventana,
                    "No se pudo crear la cuenta",
                    ex.getMessage()
            );
        }
    }

    // --------------------------------------------------
    // MODIFICACIÓN
    // --------------------------------------------------
    private void guardarDatos() {

        var ventana = Ui.ventana(raiz);

        if (usuarioSeleccionado == null) {
            return;
        }

        String usuario = txtUsuarioEditar.getText().trim();

        if (usuario.length() < 3 || usuario.contains(" ")) {

            DialogoFitTrack.advertencia(
                    ventana,
                    "Usuario no válido",
                    "El usuario debe tener al menos 3 caracteres y no llevar espacios."
            );

            return;
        }

        boolean confirmado = DialogoFitTrack.confirmar(
                ventana,
                "Guardar cambios",
                "Se actualizarán el nombre de usuario y el rol de esta cuenta.",
                "Guardar"
        );

        if (!confirmado) {
            return;
        }

        try {

            servicio.actualizarDatosUsuario(
                    usuarioSeleccionado.getIdEmpleado(),
                    cmbRolEditar.getValue(),
                    usuario
            );

            DialogoFitTrack.exito(
                    ventana,
                    "Cambios guardados",
                    "Los datos del usuario se actualizaron correctamente."
            );

            int id = usuarioSeleccionado.getIdEmpleado();

            recargar();

            UsuarioSistema actualizado = buscarUsuario(id);

            if (actualizado != null) {
                mostrarModificar(actualizado);
            }

        } catch (RuntimeException ex) {

            DialogoFitTrack.error(
                    ventana,
                    "No se pudieron guardar los cambios",
                    ex.getMessage()
            );
        }
    }

    private void cambiarContrasena() {

        var ventana = Ui.ventana(raiz);

        if (usuarioSeleccionado == null) {
            return;
        }

        String nuevaContrasena = txtContrasenaNueva.getText();

        if (nuevaContrasena.length() < 8) {

            DialogoFitTrack.advertencia(
                    ventana,
                    "Contraseña no válida",
                    "La nueva contraseña debe tener al menos 8 caracteres."
            );

            return;
        }

        boolean confirmado = DialogoFitTrack.confirmar(
                ventana,
                "Cambiar contraseña",
                "Se reemplazará la contraseña actual de "
                + usuarioSeleccionado.getUsuario() + ".",
                "Cambiar"
        );

        if (!confirmado) {
            return;
        }

        try {

            servicio.cambiarContrasenaUsuario(
                    usuarioSeleccionado.getIdEmpleado(),
                    nuevaContrasena
            );

            txtContrasenaNueva.clear();

            DialogoFitTrack.exito(
                    ventana,
                    "Contraseña actualizada",
                    "La contraseña se cambió correctamente."
            );

        } catch (RuntimeException ex) {

            DialogoFitTrack.error(
                    ventana,
                    "No se pudo cambiar la contraseña",
                    ex.getMessage()
            );
        }
    }

    // --------------------------------------------------
    // UTILIDADES
    // --------------------------------------------------
    private UsuarioSistema buscarUsuario(int id) {

        for (UsuarioSistema usuario : tabla.getItems()) {

            if (usuario.getIdEmpleado() == id) {
                return usuario;
            }
        }

        return null;
    }

    private void recargar() {

        tabla.setItems(
                FXCollections.observableArrayList(
                        servicio.listarUsuarios()
                )
        );

        txtIdCrear.setText(
                Formato.idUsuario(servicio.siguienteIdUsuario())
        );
    }
}
