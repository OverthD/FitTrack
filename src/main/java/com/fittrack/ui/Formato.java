package com.fittrack.ui;

import com.fittrack.model.estadoMembresia;
import com.fittrack.model.estadoSocio;
import com.fittrack.model.rolUsuario;
import com.fittrack.model.tipoMembresia;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Convierte datos del modelo al texto que se muestra en pantalla. */
public final class Formato {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");
    private static final Locale MX = Locale.of("es", "MX");

    private Formato() { }

    public static String idSocio(int id) {
        return String.format("FT-%06d", id);
    }

    /** Acepta "FT-000128" o "128". Lanza NumberFormatException si no es válido. */
    public static int leerIdSocio(String texto) {
        String limpio = texto.trim().toUpperCase();
        if (limpio.startsWith("FT-")) {
            limpio = limpio.substring(3);
        }
        return Integer.parseInt(limpio);
    }

    public static String idUsuario(int id) {
        return String.format("U-%02d", id);
    }

    public static String dinero(double monto) {
        return String.format(MX, "$%,.2f", monto);
    }

    public static String fecha(LocalDate fecha) {
        return fecha == null ? "—" : fecha.format(FECHA);
    }

    public static String hora(LocalTime hora) {
        return hora == null ? "—" : hora.format(HORA);
    }

    public static String texto(estadoSocio e) {
        return capitalizar(e.name());
    }

    public static String texto(estadoMembresia e) {
        return capitalizar(e.name());
    }

    public static String texto(rolUsuario r) {
        return capitalizar(r.name());
    }

    public static String texto(tipoMembresia t) {
        return t == tipoMembresia.DOSPERSONAS ? "Para dos" : capitalizar(t.name());
    }

    private static String capitalizar(String s) {
        return s.charAt(0) + s.substring(1).toLowerCase();
    }
}
