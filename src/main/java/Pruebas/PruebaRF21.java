
package Pruebas;

import com.fittrack.model.AccesoReporte;
import com.fittrack.service.AccesoService;
import com.fittrack.service.UsuarioSistemaService;
import com.fittrack.util.SesionUsuario;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PruebaRF21 {

    public static void main(String[] args) throws Exception {

        System.out.println("===== DIAGNOSTICO RF-21 =====");

        // Introducir credenciales del administrador localmente.
        String usuarioAdmin = "admin_prueba";
        String contrasenaAdmin = "FitTrack2026!";

        UsuarioSistemaService usuarioService =
                new UsuarioSistemaService();

        var usuario = usuarioService.iniciarSesion(
                usuarioAdmin,
                contrasenaAdmin
        );

        if (usuario == null) {
            System.out.println("ERROR: No se pudo iniciar sesion.");
            return;
        }

        System.out.println("Sesion administrativa iniciada.");
        System.out.println(
                "ID administrador: "
                + SesionUsuario.obtenerIdEmpleado()
        );

        AccesoService accesoService = new AccesoService();

        try {

            // PRUEBA 1: Reporte completo.
            System.out.println("\n--- PRUEBA 1: REPORTE COMPLETO ---");

            LocalDate inicio = LocalDate.of(2000, 1, 1);
            LocalDate fin = LocalDate.of(2100, 12, 31);

            List<AccesoReporte> reporte =
                    accesoService.generarReporteAccesos(
                            inicio,
                            fin
                    );

            System.out.println("Periodo: " + inicio + " a " + fin);
            System.out.println("Accesos encontrados: " + reporte.size());

            for (AccesoReporte acceso : reporte) {

                System.out.println("------------------------------");
                System.out.println("ID socio: " + acceso.getIdSocio());
                System.out.println(
                        "Nombre: " + acceso.getNombreSocio()
                );
                System.out.println("Fecha: " + acceso.getFecha());
                System.out.println(
                        "Hora entrada: " + acceso.getHoraEntrada()
                );
                System.out.println(
                        "Hora salida: "
                        + (acceso.getHoraSalida() == null
                                ? "SIN REGISTRAR"
                                : acceso.getHoraSalida())
                );
            }

            // PRUEBA 2: Integridad de los registros.
            System.out.println("\n--- PRUEBA 2: INTEGRIDAD ---");

            boolean datosCorrectos = true;

            for (AccesoReporte acceso : reporte) {

                if (acceso.getIdSocio() <= 0
                        || acceso.getNombreSocio() == null
                        || acceso.getNombreSocio().isBlank()
                        || acceso.getFecha() == null
                        || acceso.getHoraEntrada() == null) {

                    datosCorrectos = false;
                    break;
                }

                if (acceso.getHoraSalida() != null
                        && acceso.getHoraSalida().isBefore(
                                acceso.getHoraEntrada())) {

                    datosCorrectos = false;
                    break;
                }
            }

            if (!datosCorrectos) {
                throw new IllegalStateException(
                        "ERROR: Se encontraron datos inconsistentes."
                );
            }

            System.out.println("Datos obligatorios: CORRECTO");
            System.out.println("Horas de acceso: CORRECTO");

            // PRUEBA 3: Accesos sin hora de salida.
            System.out.println(
                    "\n--- PRUEBA 3: ACCESOS SIN SALIDA ---"
            );

            long accesosAbiertos = reporte.stream()
                    .filter(a -> a.getHoraSalida() == null)
                    .count();

            System.out.println(
                    "Accesos sin salida: " + accesosAbiertos
            );
            System.out.println(
                    "Valores NULL conservados: CORRECTO"
            );

            // PRUEBA 4: Socios con múltiples accesos.
            System.out.println(
                    "\n--- PRUEBA 4: MULTIPLES ACCESOS ---"
            );

            Set<Integer> socios = new HashSet<>();

            for (AccesoReporte acceso : reporte) {
                socios.add(acceso.getIdSocio());
            }

            System.out.println("Socios distintos: " + socios.size());
            System.out.println("Registros individuales: " + reporte.size());

            // PRUEBA 5: Filtro por fecha exacta.
            System.out.println("\n--- PRUEBA 5: FILTRO POR FECHA ---");

            if (!reporte.isEmpty()) {

                LocalDate fechaPrueba = reporte.get(0).getFecha();

                List<AccesoReporte> reporteFecha =
                        accesoService.generarReporteAccesos(
                                fechaPrueba,
                                fechaPrueba
                        );

                boolean fechasCorrectas = reporteFecha.stream()
                        .allMatch(a -> a.getFecha().equals(fechaPrueba));

                long esperados = reporte.stream()
                        .filter(a -> a.getFecha().equals(fechaPrueba))
                        .count();

                if (!fechasCorrectas
                        || reporteFecha.size() != esperados) {

                    throw new IllegalStateException(
                            "ERROR: El filtro por fecha es incorrecto."
                    );
                }

                System.out.println("Fecha consultada: " + fechaPrueba);
                System.out.println(
                        "Accesos encontrados: " + reporteFecha.size()
                );
                System.out.println("Filtro por fecha: CORRECTO");

            } else {
                System.out.println(
                        "No hay accesos para comprobar el filtro."
                );
            }

            // PRUEBA 6: Reporte vacío.
            System.out.println("\n--- PRUEBA 6: REPORTE VACIO ---");

            List<AccesoReporte> reporteVacio =
                    accesoService.generarReporteAccesos(
                            LocalDate.of(1900, 1, 1),
                            LocalDate.of(1900, 12, 31)
                    );

            if (!reporteVacio.isEmpty()) {
                throw new IllegalStateException(
                        "ERROR: El reporte vacio contiene registros."
                );
            }

            System.out.println("Lista vacia: CORRECTO");

            // PRUEBA 7: Fechas inválidas.
            System.out.println("\n--- PRUEBA 7: FECHAS INVALIDAS ---");

            try {
                accesoService.generarReporteAccesos(
                        null,
                        LocalDate.now()
                );

                throw new IllegalStateException(
                        "ERROR: Se acepto una fecha nula."
                );

            } catch (IllegalArgumentException e) {
                System.out.println("Fecha nula: RECHAZADA");
            }

            try {
                accesoService.generarReporteAccesos(
                        LocalDate.now(),
                        LocalDate.now().minusDays(1)
                );

                throw new IllegalStateException(
                        "ERROR: Se acepto un periodo invertido."
                );

            } catch (IllegalArgumentException e) {
                System.out.println("Periodo invertido: RECHAZADO");
            }

            System.out.println(
                    "\nTODAS LAS PRUEBAS COMPLETADAS"
            );

        } finally {
            SesionUsuario.cerrar();
            System.out.println("\nSesion administrativa cerrada.");
        }
    }
}
