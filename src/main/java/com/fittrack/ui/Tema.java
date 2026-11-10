package com.fittrack.ui;

import javafx.scene.Scene;

/** Paleta oficial de FitTrack y carga de la hoja de estilos. */
public final class Tema {

    public static final String AZUL_MARINO = "#0B2D5B";
    public static final String AZUL_MEDIO = "#1F4E8C";
    public static final String ROJO = "#E10600";
    public static final String FONDO = "#F5F7FA";
    public static final String PANEL = "#FFFFFF";
    public static final String BORDE = "#D9E1EA";
    public static final String TEXTO = "#1F2937";
    public static final String TEXTO_SECUNDARIO = "#6B7280";
    public static final String EXITO = "#16A34A";
    public static final String ADVERTENCIA = "#F59E0B";
    public static final String ERROR = "#DC2626";

    private static final String CSS =
        Tema.class.getResource("/com/fittrack/css/fittrack.css").toExternalForm();

    private Tema() { }

    public static void aplicar(Scene scene) {
        if (!scene.getStylesheets().contains(CSS)) {
            scene.getStylesheets().add(CSS);
        }
    }
}
