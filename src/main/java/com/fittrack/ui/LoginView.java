package com.fittrack.ui;

import com.fittrack.model.UsuarioSistema;
import java.net.URL;
import java.util.function.Consumer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;

/**
 * Pantalla 1: Login.
 * No contiene lógica: recibe un Autenticador (aquí se conecta el servicio/DAO)
 * y un Consumer que se ejecuta cuando el inicio de sesión es correcto.
 */
public class LoginView {

    /** Devuelve el usuario si las credenciales son válidas, o null si no lo son. */
    @FunctionalInterface
    public interface Autenticador {
        UsuarioSistema autenticar(String usuario, String contrasena) throws Exception;
    }

    private static final String LEMA = "CONTROLA CADA ENTRADA";

    private final Autenticador autenticador;
    private final Consumer<UsuarioSistema> alIngresar;

    private final TextField txtUsuario = new TextField();
    private final PasswordField txtContrasena = new PasswordField();
    private final Label lblError = new Label();
    private final Button btnEntrar = new Button("Entrar");
    private HBox raiz;

    public LoginView(Autenticador autenticador, Consumer<UsuarioSistema> alIngresar) {
        this.autenticador = autenticador;
        this.alIngresar = alIngresar;
    }

    public Parent crearVista() {
        Node izquierda = construirIzquierda();
        Node derecha = construirDerecha();
        HBox.setHgrow(izquierda, Priority.ALWAYS);
        raiz = new HBox(izquierda, derecha);
        return raiz;
    }

    private Node construirIzquierda() {
        Pane diagonal = new Pane();
        diagonal.setMouseTransparent(true);
        Polygon franja = new Polygon();
        franja.setFill(Color.web(Tema.ROJO));
        diagonal.getChildren().add(franja);
        diagonal.widthProperty().addListener((o, a, n) -> ajustarFranja(franja, diagonal));
        diagonal.heightProperty().addListener((o, a, n) -> ajustarFranja(franja, diagonal));

        Label lema = new Label(LEMA);
        lema.getStyleClass().add("login-lema");

        VBox centro = new VBox(26, crearLogo(), lema);
        centro.setAlignment(Pos.CENTER);

        StackPane izquierda = new StackPane(diagonal, centro);
        izquierda.getStyleClass().add("login-izquierda");
        izquierda.setMinWidth(480);
        return izquierda;
    }

    private static void ajustarFranja(Polygon franja, Pane contenedor) {
        double w = contenedor.getWidth();
        double h = contenedor.getHeight();
        franja.getPoints().setAll(w * 0.90, 0.0, w, 0.0, w, h, w * 0.74, h);
    }

    /** Usa /com/fittrack/img/logo.png si existe; si no, dibuja el nombre con texto. */
    private Node crearLogo() {
        VBox tarjeta = new VBox();
        tarjeta.getStyleClass().add("login-logo");
        tarjeta.setAlignment(Pos.CENTER);
        tarjeta.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        URL url = getClass().getResource("/com/fittrack/img/logo.png");
        if (url != null) {
            ImageView imagen = new ImageView(new Image(url.toExternalForm()));
            imagen.setFitWidth(240);
            imagen.setPreserveRatio(true);
            tarjeta.getChildren().add(imagen);
        } else {
            Label fit = new Label("FIT");
            fit.getStyleClass().add("logo-fit");
            Label track = new Label("TRACK");
            track.getStyleClass().add("logo-track");
            HBox nombre = new HBox(8, fit, track);
            nombre.setAlignment(Pos.CENTER);
            tarjeta.getChildren().add(nombre);
        }
        return tarjeta;
    }

    private Node construirDerecha() {
        Label titulo = new Label("Iniciar sesión");
        titulo.getStyleClass().add("login-titulo");

        Label lblUsuario = new Label("Usuario");
        lblUsuario.getStyleClass().add("etiqueta-oscura");
        txtUsuario.getStyleClass().add("campo-oscuro");
        txtUsuario.setPromptText("Tu usuario");

        Label lblContrasena = new Label("Contraseña");
        lblContrasena.getStyleClass().add("etiqueta-oscura");
        txtContrasena.getStyleClass().add("campo-oscuro");
        txtContrasena.setPromptText("Tu contraseña");

        lblError.getStyleClass().add("mensaje-error");
        lblError.setMaxWidth(Double.MAX_VALUE);
        lblError.setWrapText(true);
        ocultarError();

        btnEntrar.getStyleClass().add("boton-primario");
        btnEntrar.setMaxWidth(Double.MAX_VALUE);
        btnEntrar.setDefaultButton(true);
        btnEntrar.setOnAction(e -> intentarIngreso());

        txtUsuario.textProperty().addListener((o, a, n) -> ocultarError());
        txtContrasena.textProperty().addListener((o, a, n) -> ocultarError());

        VBox derecha = new VBox(8, titulo, lblUsuario, txtUsuario, lblContrasena,
                                txtContrasena, lblError, btnEntrar);
        VBox.setMargin(lblUsuario, new Insets(10, 0, 0, 0));
        VBox.setMargin(btnEntrar, new Insets(10, 0, 0, 0));
        derecha.getStyleClass().add("login-derecha");
        derecha.setAlignment(Pos.CENTER_LEFT);
        derecha.setPadding(new Insets(56));
        derecha.setPrefWidth(460);
        derecha.setMinWidth(400);
        return derecha;
    }

    private void intentarIngreso() {
        String usuario = txtUsuario.getText().trim();
        String contrasena = txtContrasena.getText();

        if (usuario.isEmpty() || contrasena.isEmpty()) {
            mostrarError("Escribe tu usuario y tu contraseña.");
            return;
        }

        btnEntrar.setDisable(true);
        try {
            UsuarioSistema usuarioValido = autenticador.autenticar(usuario, contrasena);
            if (usuarioValido == null) {
                txtContrasena.clear();
                mostrarError("Usuario o contraseña incorrectos.");
                return;
            }
            alIngresar.accept(usuarioValido);
        } catch (Exception ex) {
            DialogoFitTrack.error(raiz.getScene().getWindow(),
                "No se pudo iniciar sesión",
                "Revisa la conexión con la base de datos e inténtalo de nuevo.");
        } finally {
            btnEntrar.setDisable(false);
        }
    }

    private void mostrarError(String texto) {
        lblError.setText(texto);
        lblError.setVisible(true);
        lblError.setManaged(true);
    }

    private void ocultarError() {
        lblError.setVisible(false);
        lblError.setManaged(false);
    }
}
