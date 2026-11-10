package com.fittrack.ui;

import com.fittrack.model.estadoMembresia;
import com.fittrack.model.estadoSocio;
import java.util.function.Function;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

/** Piezas pequeñas que se repiten en todas las pantallas. */
public final class Ui {

    private Ui() { }

    public static Window ventana(Node nodo) {
        return nodo.getScene() == null ? null : nodo.getScene().getWindow();
    }

    public static Region espacio() {
        Region r = new Region();
        HBox.setHgrow(r, Priority.ALWAYS);
        return r;
    }

    public static Label etiqueta(String texto, String... clases) {
        Label l = new Label(texto);
        l.getStyleClass().addAll(clases);
        return l;
    }

    public static Button boton(String texto, String clase) {
        Button b = new Button(texto);
        b.getStyleClass().add(clase);
        return b;
    }

    public static TextField campo(String prompt, boolean oscuro) {
        TextField t = new TextField();
        t.setPromptText(prompt);
        t.getStyleClass().add(oscuro ? "campo-oscuro" : "campo");
        return t;
    }

    /** Etiqueta pequeña arriba y el control abajo. */
    public static VBox conEtiqueta(String texto, Control control) {
        if (!control.getStyleClass().contains("campo") && !control.getStyleClass().contains("campo-oscuro")) {
            control.getStyleClass().add("campo");
        }
        control.setMaxWidth(Double.MAX_VALUE);
        VBox v = new VBox(4, etiqueta(texto, "etiqueta-campo"), control);
        return v;
    }

    public static ToggleButton pildora(String texto) {
        ToggleButton t = new ToggleButton(texto);
        t.getStyleClass().add("pildora");
        return t;
    }

    public static Label insignia(String texto, String clase) {
        Label l = new Label(texto);
        l.getStyleClass().addAll("insignia", clase);
        return l;
    }

    public static Label insigniaSocio(estadoSocio e) {
        return insignia(Formato.texto(e), "insignia-" + e.name().toLowerCase());
    }

    public static Label insigniaMembresia(estadoMembresia e) {
        return insignia(Formato.texto(e), "insignia-" + e.name().toLowerCase());
    }

    /** Ruta pequeña (por ejemplo "Socios / Nuevo") y título grande. */
    public static VBox encabezado(String ruta, String titulo) {
        return new VBox(2, etiqueta(ruta, "encabezado-ruta"), etiqueta(titulo, "titulo-pantalla"));
    }

    public static HBox filaEncabezado(String ruta, String titulo, Node... acciones) {
        HBox fila = new HBox(8, encabezado(ruta, titulo), espacio());
        fila.setAlignment(Pos.CENTER_LEFT);
        fila.getChildren().addAll(acciones);
        return fila;
    }

    public static VBox panel(double espacio, Node... hijos) {
        VBox p = new VBox(espacio, hijos);
        p.getStyleClass().add("panel");
        return p;
    }

    /** Fila "etiqueta ........ valor". */
    public static HBox filaDato(String etiqueta, Node valor) {
        HBox f = new HBox(8, etiqueta(etiqueta, "dato-fila-etiqueta"), espacio(), valor);
        f.setAlignment(Pos.CENTER_LEFT);
        return f;
    }

    public static HBox filaDato(String etiqueta, String valor) {
        return filaDato(etiqueta, Ui.etiqueta(valor, "dato-fila-valor"));
    }

    public static VBox tarjetaDato(String etiqueta, Label valor) {
        valor.getStyleClass().add("dato-valor");
        VBox t = new VBox(4, etiqueta(etiqueta, "dato-etiqueta"), valor);
        t.getStyleClass().add("tarjeta-dato");
        return t;
    }

    public static Insets margen(double arriba) {
        return new Insets(arriba, 0, 0, 0);
    }

    // ---------- Tablas ----------

    @SuppressWarnings("deprecation")
    public static <S> TableView<S> tabla(String textoVacio) {
        TableView<S> t = new TableView<>();
        t.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        t.setPlaceholder(new Label(textoVacio));
        VBox.setVgrow(t, Priority.ALWAYS);
        return t;
    }

    public static <S> TableColumn<S, String> columna(String titulo, Function<S, String> valor) {
        TableColumn<S, String> c = new TableColumn<>(titulo);
        c.setCellValueFactory(d -> new ReadOnlyStringWrapper(valor.apply(d.getValue())));
        c.setSortable(false);
        return c;
    }

    /** Columna cuyo contenido es un nodo (insignia, botón...). */
    public static <S> TableColumn<S, S> columnaNodo(String titulo, Function<S, Node> fabrica) {
        TableColumn<S, S> c = new TableColumn<>(titulo);
        c.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue()));
        c.setSortable(false);
        c.setCellFactory(col -> new TableCell<S, S>() {
            @Override
            protected void updateItem(S item, boolean vacio) {
                super.updateItem(item, vacio);
                setText(null);
                setGraphic(vacio || item == null ? null : fabrica.apply(item));
            }
        });
        return c;
    }

    // ---------- Lectura de datos escritos ----------

    /** Convierte "$1,200.50" o "1200.5" en número. Devuelve null si no es válido. */
    public static Double leerMonto(String texto) {
        if (texto == null) {
            return null;
        }
        String limpio = texto.replace("$", "").replace(",", "").trim();
        try {
            return Double.parseDouble(limpio);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static String monto(double valor) {
        return String.format(java.util.Locale.US, "%.2f", valor);
    }

    /** Acomoda los nodos en dos columnas iguales. */
    public static javafx.scene.layout.GridPane rejilla(Node... celdas) {
        javafx.scene.layout.GridPane g = new javafx.scene.layout.GridPane();
        g.setHgap(12);
        g.setVgap(12);
        for (int i = 0; i < 2; i++) {
            javafx.scene.layout.ColumnConstraints c = new javafx.scene.layout.ColumnConstraints();
            c.setPercentWidth(50);
            g.getColumnConstraints().add(c);
        }
        for (int i = 0; i < celdas.length; i++) {
            g.add(celdas[i], i % 2, i / 2);
        }
        return g;
    }
}
