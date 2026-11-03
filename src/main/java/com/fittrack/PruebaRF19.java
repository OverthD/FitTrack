
package com.fittrack;

import com.fittrack.model.MembresiaReporte;
import com.fittrack.model.UsuarioSistema;
import com.fittrack.model.estadoMembresia;
import com.fittrack.service.MembresiaService;
import com.fittrack.service.UsuarioSistemaService;
import com.fittrack.util.SesionUsuario;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class PruebaRF19 {

    public static void main(String[] args) throws Exception {

        // Introducir las credenciales del administrador local.
        String usuarioAdmin = "admin_prueba";
        String contrasenaAdmin = "FitTrack2026!";

        UsuarioSistemaService usuarioService =
                new UsuarioSistemaService();

        MembresiaService membresiaService =
                new MembresiaService();

        System.out.println("===== DIAGNÓSTICO RF-19 =====");

        // Iniciar sesión administrativa.
        UsuarioSistema usuario = usuarioService.iniciarSesion(
                usuarioAdmin,
                contrasenaAdmin
        );

        if (usuario == null) {
            System.out.println(
                    "ERROR: No se pudo iniciar sesión como administrador."
            );
            return;
        }

        System.out.println("Sesión administrativa iniciada.");
        System.out.println(
                "ID administrador actual: "
                + SesionUsuario.obtenerIdEmpleado()
        );

        boolean todasLasPruebasCorrectas = true;

        try {

            // PRUEBA 1: Generar reporte completo.
            System.out.println("\n--- PRUEBA 1: REPORTE COMPLETO ---");

            List<MembresiaReporte> reporteCompleto =
                    membresiaService.generarReporteMembresias();

            System.out.println(
                    "Registros obtenidos: " + reporteCompleto.size()
            );

            Map<estadoMembresia, Integer> conteos =
                    new EnumMap<>(estadoMembresia.class);

            for (estadoMembresia estado : estadoMembresia.values()) {
                conteos.put(estado, 0);
            }

            for (MembresiaReporte registro : reporteCompleto) {

                if (registro.getEstado() == null) {
                    todasLasPruebasCorrectas = false;
                    continue;
                }

                conteos.put(
                        registro.getEstado(),
                        conteos.get(registro.getEstado()) + 1
                );

                System.out.println(
                        "Socio: " + registro.getIdSocio()
                        + " | " + registro.getNombre()
                        + " " + registro.getApellido()
                        + " | Membresía: " + registro.getIdMembresia()
                        + " | Tipo: " + registro.getTipo()
                        + " | Inicio: " + registro.getFechaInicio()
                        + " | Vencimiento: " + registro.getFechaVencimiento()
                        + " | Precio: " + registro.getPrecio()
                        + " | Estado: " + registro.getEstado()
                );
            }

            System.out.println("\nConteo por estado:");

            for (estadoMembresia estado : estadoMembresia.values()) {
                System.out.println(
                        estado + ": " + conteos.get(estado)
                );
            }

            System.out.println(
                    reporteCompleto.stream().allMatch(
                            registro -> registro.getEstado() != null
                    )
                    ? "CORRECTO: Todos los registros tienen estado."
                    : "ERROR: Se encontraron registros sin estado."
            );

            // PRUEBAS 2 A 5: Filtrar por cada estado.
            System.out.println("\n--- PRUEBAS DE FILTRADO ---");

            for (estadoMembresia estado : estadoMembresia.values()) {

                List<MembresiaReporte> filtradas =
                        membresiaService.generarReporteMembresias(estado);

                int cantidadEsperada = conteos.get(estado);

                boolean estadosCorrectos = filtradas.stream()
                        .allMatch(registro ->
                                registro.getEstado() == estado
                        );

                boolean cantidadCorrecta =
                        filtradas.size() == cantidadEsperada;

                boolean pruebaCorrecta =
                        estadosCorrectos && cantidadCorrecta;

                System.out.println("\nFiltro: " + estado);
                System.out.println(
                        "Registros obtenidos: " + filtradas.size()
                );
                System.out.println(
                        "Registros esperados: " + cantidadEsperada
                );

                if (pruebaCorrecta) {
                    System.out.println("CORRECTO: Filtro validado.");
                } else {
                    System.out.println("ERROR: El filtro no coincide.");
                    todasLasPruebasCorrectas = false;
                }
            }

            // PRUEBA 6: Comprobar que los filtros incluyen
            // todos los registros del reporte completo.
            System.out.println(
                    "\n--- PRUEBA 6: INTEGRIDAD DE LOS FILTROS ---"
            );

            int totalFiltrado = 0;

            for (Integer cantidad : conteos.values()) {
                totalFiltrado += cantidad;
            }

            boolean integridadCorrecta =
                    totalFiltrado == reporteCompleto.size();

            System.out.println(
                    "Total del reporte: " + reporteCompleto.size()
            );
            System.out.println(
                    "Total de los cuatro filtros: " + totalFiltrado
            );

            if (integridadCorrecta) {
                System.out.println(
                        "CORRECTO: Los filtros cubren todo el reporte."
                );
            } else {
                System.out.println(
                        "ERROR: Los conteos de los filtros no coinciden."
                );
                todasLasPruebasCorrectas = false;
            }

        } finally {
            // Cerrar la sesión de prueba.
            SesionUsuario.cerrar();
            System.out.println("\nSesión de prueba cerrada.");
        }

        System.out.println("\n====================================");

        if (todasLasPruebasCorrectas) {
            System.out.println("RESULTADO: TODAS LAS PRUEBAS APROBADAS.");
        } else {
            System.out.println("RESULTADO: SE DETECTARON ERRORES.");
        }

        System.out.println("===== FIN DIAGNÓSTICO RF-19 =====");
    }
}
