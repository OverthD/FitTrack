package com.fittrack.ui;

import com.fittrack.servicio.FitTrackServicio;
import com.fittrack.servicio.SocioResumen;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

/**
 * Pantalla 11: Gestión de membresías (tema claro).
 */
public class ProximasVencerView {

    private final FitTrackServicio servicio;
    private final Navegador nav;

    private final ToggleGroup filtros = new ToggleGroup();

    private final ComboBox<Integer> cmbDias = new ComboBox<>();
    private VBox filtroPeriodo;

    private final TableView<SocioResumen> tabla
            = Ui.tabla("No hay membresías que coincidan con la búsqueda.");

    public ProximasVencerView(FitTrackServicio servicio, Navegador nav) {
        this.servicio = servicio;
        this.nav = nav;
    }

    public Node crearVista() {
        VBox raiz = new VBox(16);
        raiz.getStyleClass().add("pantalla-clara");

        Button btnRegistrar = Ui.boton("Registrar membresía", "boton-primario");
        btnRegistrar.setOnAction(e -> nav.irRegistrarMembresia(null));

        // Botones superiores de navegación.
        HBox pildoras = new HBox(8);
        pildoras.setAlignment(Pos.CENTER_LEFT);

        agregarFiltro(pildoras, "Todas", true);
        agregarFiltro(pildoras, "Próximas a vencer", false);

        filtros.selectedToggleProperty().addListener((o, anterior, nuevo) -> {
            if (nuevo == null) {
                anterior.setSelected(true);
            } else {
                actualizarVista();
            }
        });

        // Selector del periodo para próximas a vencer.
        cmbDias.getItems().setAll(3, 7, 15, 30);
        cmbDias.setValue(7);

        cmbDias.setConverter(new StringConverter<>() {
            @Override
            public String toString(Integer dias) {
                return dias == null ? "" : dias + " días";
            }

            @Override
            public Integer fromString(String texto) {
                return null;
            }
        });

        cmbDias.valueProperty().addListener((o, anterior, nuevo) -> {
            if (esVistaProximas() && nuevo != null) {
                recargar();
            }
        });

        filtroPeriodo = Ui.conEtiqueta("Mostrar membresías que vencen en:", cmbDias);

        filtroPeriodo.setMaxWidth(250);

        // Columnas de la tabla.
        tabla.getColumns()
                .setAll(
                        Ui.<SocioResumen>columna(
                                "ID Membresía",
                                r -> String.valueOf(r.getMembresia().getIdMembresia())
                        ),
                        Ui.<SocioResumen>columna(
                                "ID Socio",
                                r -> Formato.idSocio(r.getSocio().getIdSocio())
                        ),
                        Ui.<SocioResumen>columna(
                                "Socio",
                                SocioResumen::nombreCompleto
                        ),
                        Ui.<SocioResumen>columna(
                                "Tipo",
                                r -> Formato.texto(r.getMembresia().getTipo())
                        ),
                        Ui.<SocioResumen>columna(
                                "Vence",
                                r -> Formato.fecha(r.getMembresia().getFechaVencimiento())
                        ),
                        Ui.<SocioResumen>columnaNodo(
                                "Estado",
                                r -> Ui.insigniaMembresia(r.getMembresia().getEstado())
                        ),
                        Ui.<SocioResumen>columnaNodo("", r -> {
                            Button b = Ui.boton("Renovar", "boton-marino");
                            b.setStyle("-fx-padding: 5 14;");
                            b.setOnAction(e -> nav.irRenovar(r.getSocio().getIdSocio()));
                            return b;
                        })
                );

        raiz.getChildren().addAll(
                Ui.filaEncabezado("Membresías", "Administración de membresías", btnRegistrar),
                pildoras,
                filtroPeriodo,
                tabla
        );

        actualizarVista();

        return raiz;
    }

    private void agregarFiltro(HBox contenedor, String texto, boolean seleccionado) {
        ToggleButton boton = Ui.pildora(texto);
        boton.setToggleGroup(filtros);
        boton.setSelected(seleccionado);
        contenedor.getChildren().add(boton);
    }

    private boolean esVistaProximas() {
        return filtros.getSelectedToggle() != null
                && "Próximas a vencer".equals(
                        ((ToggleButton) filtros.getSelectedToggle()).getText()
                );
    }

    private void actualizarVista() {
        boolean proximas = esVistaProximas();

        filtroPeriodo.setVisible(proximas);
        filtroPeriodo.setManaged(proximas);

        recargar();
    }

    private void recargar() {
        if (esVistaProximas()) {
            tabla.setPlaceholder(
                    Ui.tabla("No hay membresías por vencer en este periodo.").getPlaceholder()
            );

            tabla.setItems(FXCollections.observableArrayList(
                    servicio.membresiasProximasAVencer(cmbDias.getValue())
            ));
        } else {
            tabla.setPlaceholder(
                    Ui.tabla("No hay membresías registradas.").getPlaceholder()
            );

            tabla.setItems(FXCollections.observableArrayList(
                    servicio.listarMembresias()
            ));
        }
    }
}
