package com.fittrack.ui;

import com.fittrack.model.estadoSocio;
import com.fittrack.servicio.FitTrackServicio;
import com.fittrack.servicio.SocioResumen;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/** Pantalla 3: Listado de socios (tema oscuro). */
public class SociosView {

    private final FitTrackServicio servicio;
    private final Navegador nav;

    private final TextField txtBuscar = Ui.campo("Buscar por ID o nombre", true);
    private final ToggleGroup filtros = new ToggleGroup();
    private final TableView<SocioResumen> tabla = Ui.tabla("No hay socios que coincidan con la búsqueda.");

    public SociosView(FitTrackServicio servicio, Navegador nav) {
        this.servicio = servicio;
        this.nav = nav;
    }

    public Node crearVista() {
        VBox raiz = new VBox(16);
        raiz.getStyleClass().add("pantalla-oscura");

        Button btnNuevo = Ui.boton("Nuevo socio", "boton-primario");
        btnNuevo.setOnAction(e -> nav.irNuevoSocio());

        Button btnBuscar = Ui.boton("Buscar", "boton-marino");
        btnBuscar.setOnAction(e -> recargar());
        txtBuscar.setOnAction(e -> recargar());
        HBox.setHgrow(txtBuscar, Priority.ALWAYS);
        HBox barra = new HBox(10, txtBuscar, btnBuscar);

        HBox pildoras = new HBox(8);
        pildoras.setAlignment(Pos.CENTER_LEFT);
        agregarFiltro(pildoras, "Todos", null, true);
        agregarFiltro(pildoras, "Activos", estadoSocio.ACTIVO, false);
        agregarFiltro(pildoras, "Inactivos", estadoSocio.INACTIVO, false);
        agregarFiltro(pildoras, "Baja", estadoSocio.BAJA, false);
        filtros.selectedToggleProperty().addListener((o, anterior, nuevo) -> {
            if (nuevo == null) {
                anterior.setSelected(true);
            } else {
                recargar();
            }
        });

        tabla.getColumns().add(Ui.<SocioResumen>columna("ID", r -> Formato.idSocio(r.getSocio().getIdSocio())));
        tabla.getColumns().add(Ui.<SocioResumen>columna("Nombre", SocioResumen::nombreCompleto));
        tabla.getColumns().add(Ui.<SocioResumen>columna("Teléfono", r -> r.getSocio().getTelefono()));
        tabla.getColumns().add(Ui.<SocioResumen>columna("Membresía",
            r -> r.getMembresia() == null ? "Sin membresía" : Formato.texto(r.getMembresia().getTipo())));
        tabla.getColumns().add(Ui.<SocioResumen>columnaNodo("Estado", r -> Ui.insigniaSocio(r.getSocio().getEstado())));
        tabla.setRowFactory(tv -> {
            TableRow<SocioResumen> fila = new TableRow<>();
            fila.setStyle("-fx-cursor: hand;");
            fila.setOnMouseClicked(e -> {
                if (!fila.isEmpty()) {
                    nav.irPerfil(fila.getItem().getSocio().getIdSocio());
                }
            });
            return fila;
        });

        raiz.getChildren().addAll(Ui.filaEncabezado("Socios", "Listado de socios", btnNuevo), barra, pildoras, tabla);
        recargar();
        return raiz;
    }

    private void agregarFiltro(HBox contenedor, String texto, estadoSocio estado, boolean seleccionado) {
        ToggleButton t = Ui.pildora(texto);
        t.setUserData(estado);
        t.setToggleGroup(filtros);
        t.setSelected(seleccionado);
        contenedor.getChildren().add(t);
    }

    private void recargar() {
        estadoSocio filtro = filtros.getSelectedToggle() == null
            ? null : (estadoSocio) filtros.getSelectedToggle().getUserData();
        tabla.setItems(FXCollections.observableArrayList(servicio.listarSocios(txtBuscar.getText(), filtro)));
    }
}
