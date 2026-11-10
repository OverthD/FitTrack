package com.fittrack.ui;

import com.fittrack.model.Acceso;
import com.fittrack.model.Membresia;
import com.fittrack.model.Socio;
import com.fittrack.model.estadoMembresia;
import com.fittrack.model.estadoSocio;
import com.fittrack.servicio.FilaAcceso;
import com.fittrack.servicio.FitTrackServicio;
import com.fittrack.servicio.SocioResumen;
import java.util.List;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/** Pantalla 2: Validar acceso (tema oscuro). */
public class ValidarAccesoView {

    private final FitTrackServicio servicio;
    private final Navegador nav;
    private final Integer idInicial;

    private final VBox raiz = new VBox(16);
    private final TextField txtId = Ui.campo("FT-000128", true);
    private final VBox panelResultado = new VBox(12);
    private final VBox listaUltimos = new VBox(10);

    public ValidarAccesoView(FitTrackServicio servicio, Navegador nav, Integer idInicial) {
        this.servicio = servicio;
        this.nav = nav;
        this.idInicial = idInicial;
    }

    public Node crearVista() {
        raiz.getStyleClass().add("pantalla-oscura");

        Button btnBuscar = Ui.boton("Buscar socio", "boton-marino");
        btnBuscar.setOnAction(e -> buscar());
        txtId.setOnAction(e -> buscar());
        HBox.setHgrow(txtId, Priority.ALWAYS);
        HBox barra = new HBox(10, txtId, btnBuscar);

        panelResultado.getStyleClass().add("panel");
        panelResultado.getChildren().add(Ui.etiqueta("Escribe el ID del socio para validar su acceso.", "texto-secundario"));
        HBox.setHgrow(panelResultado, Priority.ALWAYS);

        Label tituloUltimos = Ui.etiqueta("Últimos accesos", "subtitulo-panel");
        VBox panelUltimos = Ui.panel(12, tituloUltimos, listaUltimos);
        panelUltimos.setPrefWidth(300);
        panelUltimos.setMinWidth(260);
        panelUltimos.setMaxHeight(Double.MAX_VALUE);

        HBox cuerpo = new HBox(16, panelResultado, panelUltimos);
        cuerpo.setAlignment(Pos.TOP_LEFT);

        raiz.getChildren().addAll(Ui.encabezado("Recepción", "Validar acceso"), barra, cuerpo);
        refrescarUltimos();

        if (idInicial != null) {
            txtId.setText(Formato.idSocio(idInicial));
            buscar();
        }
        return raiz;
    }

    private void buscar() {
        int id;
        try {
            id = Formato.leerIdSocio(txtId.getText());
        } catch (NumberFormatException ex) {
            DialogoFitTrack.advertencia(Ui.ventana(raiz), "ID no válido",
                "Escribe el ID así: FT-000128, o solo el número.");
            return;
        }
        SocioResumen r = servicio.buscarSocio(id);
        if (r == null) {
            DialogoFitTrack.error(Ui.ventana(raiz), "Socio no encontrado",
                "No existe ningún socio con el ID " + Formato.idSocio(id) + ".");
            return;
        }
        mostrarResultado(r);
    }

    private void mostrarResultado(SocioResumen r) {
        Socio s = r.getSocio();
        Membresia m = r.getMembresia();

        boolean socioActivo = s.getEstado() == estadoSocio.ACTIVO;
        boolean enTolerancia = socioActivo && m != null && m.getEstado() == estadoMembresia.TOLERANCIA;
        boolean permitido = socioActivo && m != null && m.permitirAcceso();
        String motivo = null;
        if (!socioActivo) {
            motivo = "El socio está " + Formato.texto(s.getEstado()).toLowerCase() + ".";
        } else if (m == null) {
            motivo = "El socio no tiene membresía.";
        } else if (!m.permitirAcceso()) {
            motivo = m.getEstado() == estadoMembresia.PROGRAMADA
                ? "La membresía empieza el " + Formato.fecha(m.getFechaInicio()) + "."
                : "La membresía venció" + (m.getFechaVencimiento() == null
                    ? " o no tiene vencimiento definido."
                    : " el " + Formato.fecha(m.getFechaVencimiento()) + ".");
        }

        Label nombre = Ui.etiqueta(r.nombreCompleto(), "nombre-grande");
        String tipo = m == null ? "Sin membresía" : Formato.texto(m.getTipo());
        Label sub = Ui.etiqueta(Formato.idSocio(s.getIdSocio()) + " · " + tipo, "texto-secundario");
        Node insignia = m == null ? Ui.insignia("Sin membresía", "insignia-inactivo")
                                  : Ui.insigniaMembresia(m.getEstado());
        HBox cabecera = new HBox(10, new VBox(2, nombre, sub), Ui.espacio(), insignia);
        cabecera.setAlignment(Pos.CENTER_LEFT);

        HBox datos = new HBox(10,
            miniDato("Inicio", m == null ? "—" : Formato.fecha(m.getFechaInicio())),
            miniDato("Vencimiento", m == null ? "—" : Formato.fecha(m.getFechaVencimiento())),
            miniDato("Socio", Formato.texto(s.getEstado())));

        Label banner = new Label(permitido ? (enTolerancia ? "EN TOLERANCIA" : "ACCESO PERMITIDO") : "ACCESO DENEGADO");
        banner.getStyleClass().addAll("banner",
            permitido ? (enTolerancia ? "banner-tolerancia" : "banner-permitido") : "banner-denegado");
        banner.setMaxWidth(Double.MAX_VALUE);
        VBox bloqueBanner = new VBox(4, banner);
        if (motivo != null) {
            bloqueBanner.getChildren().add(Ui.etiqueta(motivo, "texto-secundario"));
        }

        HBox botones = new HBox(10);
        Label aviso = new Label();
        aviso.getStyleClass().add("mensaje-exito");
        aviso.setVisible(false);
        aviso.setManaged(false);
        aviso.setMaxWidth(Double.MAX_VALUE);

        if (permitido) {
            Button btnEntrada = Ui.boton("Registrar entrada", "boton-primario");
            btnEntrada.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(btnEntrada, Priority.ALWAYS);
            btnEntrada.setOnAction(e -> registrarEntrada(r, enTolerancia, aviso));
            botones.getChildren().add(btnEntrada);
        } else if (socioActivo && m != null) {
            Button btn = Ui.boton("Renovar membresía", "boton-primario");
            btn.setOnAction(e -> nav.irRenovar(s.getIdSocio()));
            botones.getChildren().add(btn);
        } else if (socioActivo) {
            Button btn = Ui.boton("Registrar membresía", "boton-primario");
            btn.setOnAction(e -> nav.irRegistrarMembresia(s.getIdSocio()));
            botones.getChildren().add(btn);
        }
        Button btnPerfil = Ui.boton("Ver perfil", "boton-secundario");
        btnPerfil.setOnAction(e -> nav.irPerfil(s.getIdSocio()));
        botones.getChildren().add(btnPerfil);

        panelResultado.getChildren().setAll(cabecera, datos, bloqueBanner, aviso, botones);
    }

    private VBox miniDato(String etiqueta, String valor) {
        Label v = Ui.etiqueta(valor, "dato-valor");
        v.setStyle("-fx-font-size: 16px;");
        VBox t = Ui.tarjetaDato(etiqueta, v);
        HBox.setHgrow(t, Priority.ALWAYS);
        t.setMaxWidth(Double.MAX_VALUE);
        return t;
    }

    private void registrarEntrada(SocioResumen r, boolean enTolerancia, Label aviso) {
        if (enTolerancia) {
            boolean seguir = DialogoFitTrack.confirmar(Ui.ventana(raiz), "Membresía en tolerancia",
                r.nombreCompleto() + " venció el " + Formato.fecha(r.getMembresia().getFechaVencimiento())
                    + ". Puedes permitir el acceso y avisarle que renueve.", "Permitir entrada");
            if (!seguir) {
                return;
            }
        }
        try {
            Acceso a = servicio.registrarEntrada(r.getSocio().getIdSocio());
            aviso.setText("Entrada registrada a las " + Formato.hora(a.getHoraEntrada()) + ".");
            aviso.setVisible(true);
            aviso.setManaged(true);
            txtId.clear();
            refrescarUltimos();
        } catch (IllegalStateException | IllegalArgumentException ex) {
            DialogoFitTrack.advertencia(Ui.ventana(raiz), "No se registró la entrada", ex.getMessage());
        }
    }

    private void refrescarUltimos() {
        listaUltimos.getChildren().clear();
        List<FilaAcceso> filas = servicio.ultimosAccesos(6);
        if (filas.isEmpty()) {
            listaUltimos.getChildren().add(Ui.etiqueta("Todavía no hay accesos.", "texto-secundario"));
            return;
        }
        for (FilaAcceso f : filas) {
            boolean salio = f.acceso().getHoraSalida() != null;
            String hora = Formato.hora(salio ? f.acceso().getHoraSalida() : f.acceso().getHoraEntrada());
            Label nombre = Ui.etiqueta(f.socio().getNombre() + " " + f.socio().getApellido(), "dato-fila-valor");
            Label detalle = Ui.etiqueta(hora + " · " + (salio ? "Salida" : "Entrada"), "dato-fila-etiqueta");
            HBox fila = new HBox(8, nombre, Ui.espacio(), detalle);
            fila.setAlignment(Pos.CENTER_LEFT);
            listaUltimos.getChildren().add(fila);
        }
    }
}
