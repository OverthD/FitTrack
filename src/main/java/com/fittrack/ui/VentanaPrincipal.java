package com.fittrack.ui;

import com.fittrack.model.UsuarioSistema;
import com.fittrack.model.rolUsuario;
import com.fittrack.servicio.FitTrackServicio;
import com.fittrack.util.SesionUsuario;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Ventana principal: menú lateral fijo + zona central donde se cambian las pantallas.
 * Implementa Navegador: las pantallas le piden cambiar de pantalla por medio de esa interfaz.
 */
public class VentanaPrincipal implements Navegador {

    private record Item(String clave, String texto) { }

    private static final List<Item> RECEPCION = List.of(
        new Item("validar-acceso", "Validar acceso"),
        new Item("socios", "Socios"),
        new Item("membresias", "Membresías"),
        new Item("pagos", "Pagos"),
        new Item("accesos", "Accesos"));

    private static final List<Item> ADMINISTRACION = List.of(
        new Item("ingresos", "Ingresos y ventas"),
        new Item("reportes", "Reportes"),
        new Item("usuarios", "Usuarios"),
        new Item("respaldo", "Respaldo"));

    private static final Set<String> SOLO_ADMIN = Set.of("ingresos", "reportes", "usuarios", "respaldo");

    private final UsuarioSistema usuario;
    private final FitTrackServicio servicio;
    private final Runnable alCerrarSesion;
    private final BorderPane raiz = new BorderPane();
    private final StackPane contenido = new StackPane();
    private final Map<String, Button> botones = new LinkedHashMap<>();

    public VentanaPrincipal(UsuarioSistema usuario, FitTrackServicio servicio, Runnable alCerrarSesion) {
        this.usuario = usuario;
        this.servicio = servicio;
        this.alCerrarSesion = alCerrarSesion;
        raiz.setLeft(construirMenu());
        raiz.setCenter(contenido);
    }

    public Parent getRaiz() {
        return raiz;
    }

    // ------------------------------------------------------------------
    // Navegación
    // ------------------------------------------------------------------

    @Override
    public void ir(String clave) {
        if (SOLO_ADMIN.contains(clave) && usuario.getRol() != rolUsuario.ADMINISTRADOR) {
            DialogoFitTrack.advertencia(raiz.getScene().getWindow(), "Sin permiso",
                "Esta sección es solo para administradores.");
            return;
        }
        Node vista = switch (clave) {
            case "validar-acceso" -> new ValidarAccesoView(servicio, this, null).crearVista();
            case "socios" -> new SociosView(servicio, this).crearVista();
            case "membresias" -> new ProximasVencerView(servicio, this).crearVista();
            case "pagos" -> new HistorialPagosView(servicio, this, null).crearVista();
            case "accesos" -> new HistorialAccesosView(servicio, this, null).crearVista();
            case "ingresos" -> new IngresosView(servicio).crearVista();
            case "reportes" -> new ReportesView(servicio).crearVista();
            case "usuarios" -> new UsuariosView(servicio).crearVista();
            case "respaldo" -> new RespaldoView(servicio).crearVista();
            default -> throw new IllegalArgumentException("Pantalla desconocida: " + clave);
        };
        mostrarVista(vista, clave);
    }

    @Override
    public void irNuevoSocio() {
        mostrarVista(new RegistrarSocioView(servicio, this).crearVista(), "socios");
    }

    @Override
    public void irPerfil(int idSocio) {
        mostrarVista(new PerfilSocioView(servicio, this, idSocio).crearVista(), "socios");
    }

    @Override
    public void irRegistrarMembresia(Integer idSocio) {
        mostrarVista(new RegistrarMembresiaView(servicio, this, idSocio).crearVista(), "membresias");
    }

    @Override
    public void irRenovar(int idSocio) {
        mostrarVista(new RenovarMembresiaView(servicio, this, idSocio).crearVista(), "membresias");
    }

    @Override
    public void irRegistrarPago(Integer idSocio, Double monto) {
        mostrarVista(new RegistrarPagoView(servicio, this, idSocio, monto).crearVista(), "pagos");
    }

    @Override
    public void irValidarAcceso(Integer idSocio) {
        mostrarVista(new ValidarAccesoView(servicio, this, idSocio).crearVista(), "validar-acceso");
    }

    @Override
    public void irPagosDe(int idSocio) {
        mostrarVista(new HistorialPagosView(servicio, this, Formato.idSocio(idSocio)).crearVista(), "pagos");
    }

    @Override
    public void irAccesosDe(int idSocio) {
        mostrarVista(new HistorialAccesosView(servicio, this, Formato.idSocio(idSocio)).crearVista(), "accesos");
    }

    /** Muestra la pantalla dentro de un ScrollPane (por si la ventana es chica) y resalta el menú. */
    private void mostrarVista(Node vista, String claveMenu) {
        ScrollPane scroll = new ScrollPane(vista);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.getStyleClass().add("scroll-pantalla");
        contenido.getChildren().setAll(scroll);

        botones.values().forEach(b -> b.getStyleClass().remove("menu-item-activo"));
        Button activo = botones.get(claveMenu);
        if (activo != null) {
            activo.getStyleClass().add("menu-item-activo");
        }
    }

    // ------------------------------------------------------------------
    // Menú lateral
    // ------------------------------------------------------------------

    private Node construirMenu() {
        VBox menu = new VBox(4);
        menu.getStyleClass().add("menu-lateral");
        menu.setPrefWidth(230);
        menu.setMinWidth(230);

        menu.getChildren().add(crearLogo());
        menu.getChildren().add(crearSeccion("RECEPCIÓN"));
        for (Item item : RECEPCION) {
            menu.getChildren().add(crearBoton(item));
        }

        if (usuario.getRol() == rolUsuario.ADMINISTRADOR) {
            menu.getChildren().add(crearSeccion("ADMINISTRACIÓN"));
            for (Item item : ADMINISTRACION) {
                menu.getChildren().add(crearBoton(item));
            }
        }

        Region espacio = new Region();
        VBox.setVgrow(espacio, Priority.ALWAYS);
        menu.getChildren().addAll(espacio, crearPie());
        return menu;
    }

    private Node crearLogo() {
        HBox tarjeta = new HBox();
        tarjeta.getStyleClass().add("menu-logo");
        tarjeta.setAlignment(Pos.CENTER);

        URL url = getClass().getResource("/com/fittrack/img/logo.png");
        if (url != null) {
            ImageView imagen = new ImageView(new Image(url.toExternalForm()));
            imagen.setFitWidth(150);
            imagen.setPreserveRatio(true);
            tarjeta.getChildren().add(imagen);
        } else {
            Label fit = new Label("FIT");
            fit.getStyleClass().add("logo-fit");
            fit.setStyle("-fx-font-size: 20px;");
            Label track = new Label("TRACK");
            track.getStyleClass().add("logo-track");
            track.setStyle("-fx-font-size: 20px;");
            HBox.setMargin(track, new Insets(0, 0, 0, 5));
            tarjeta.getChildren().addAll(fit, track);
        }
        return tarjeta;
    }

    private Label crearSeccion(String texto) {
        Label etiqueta = new Label(texto);
        etiqueta.getStyleClass().add("menu-seccion");
        return etiqueta;
    }

    private Button crearBoton(Item item) {
        Button boton = new Button(item.texto());
        boton.getStyleClass().add("menu-item");
        boton.setMaxWidth(Double.MAX_VALUE);
        boton.setOnAction(e -> ir(item.clave()));
        botones.put(item.clave(), boton);
        return boton;
    }

    private Node crearPie() {
        Label estado = new Label("Sesión activa");
        estado.getStyleClass().add("menu-pie-estado");

        Label nombre = new Label(usuario.getUsuario() + " · " + Formato.texto(usuario.getRol()));
        nombre.getStyleClass().add("menu-pie-usuario");

        Button cerrar = new Button("Cerrar sesión");
        cerrar.getStyleClass().add("menu-cerrar");
        cerrar.setMaxWidth(Double.MAX_VALUE);
        cerrar.setOnAction(e -> cerrarSesion());

        VBox pie = new VBox(4, estado, nombre, cerrar);
        VBox.setMargin(cerrar, new Insets(6, 0, 0, 0));
        return pie;
    }

    private void cerrarSesion() {
        boolean confirmado = DialogoFitTrack.confirmar(
            raiz.getScene().getWindow(),
            "Cerrar sesión",
            "Se cerrará la sesión de " + usuario.getUsuario() + " en este equipo.",
            "Cerrar sesión");
        if (confirmado) {
            SesionUsuario.cerrar();
            alCerrarSesion.run();
        }
    }
}
