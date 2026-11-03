
package com.fittrack;

import com.fittrack.model.PagoReporte;
import com.fittrack.model.ReportePagos;
import com.fittrack.model.tipoMembresia;
import com.fittrack.service.PagoService;
import com.fittrack.service.UsuarioSistemaService;
import com.fittrack.util.SesionUsuario;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

public class PruebaRF20 {

    public static void main(String[] args) throws Exception {

        System.out.println("===== DIAGNOSTICO RF-20 =====");

        // Introducir las credenciales del administrador localmente.
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

        PagoService pagoService = new PagoService();

        try {

            // PRUEBA 1: Reporte completo.
            System.out.println("\n--- PRUEBA 1: REPORTE COMPLETO ---");

            LocalDate inicio = LocalDate.of(2000, 1, 1);
            LocalDate fin = LocalDate.of(2100, 12, 31);

            ReportePagos reporte =
                    pagoService.generarReportePagos(inicio, fin);

            System.out.println("Periodo: " + inicio + " a " + fin);
            System.out.println(
                    "Pagos encontrados: " + reporte.getPagos().size()
            );

            for (PagoReporte pago : reporte.getPagos()) {

                System.out.println("------------------------------");
                System.out.println("ID pago: " + pago.getIdPago());
                System.out.println("Socios: " + pago.getIdsSocios());
                System.out.println(
                        "Nombres: " + pago.getNombresSocios()
                );
                System.out.println(
                        "ID membresia: " + pago.getIdMembresia()
                );
                System.out.println(
                        "Tipo: " + pago.getTipoMembresia()
                );
                System.out.println("Fecha: " + pago.getFechaPago());
                System.out.println("Monto: " + pago.getMonto());
                System.out.println(
                        "Metodo: " + pago.getMetodoPago()
                );
            }

            System.out.println("------------------------------");
            System.out.println(
                    "TOTAL ACUMULADO: "
                    + reporte.getTotalAcumulado()
            );

            // PRUEBA 2: Integridad y ausencia de duplicados.
            System.out.println("\n--- PRUEBA 2: INTEGRIDAD ---");

            Set<Integer> idsPagos = new HashSet<>();
            BigDecimal sumaDetalle = BigDecimal.ZERO;

            for (PagoReporte pago : reporte.getPagos()) {

                if (!idsPagos.add(pago.getIdPago())) {
                    throw new IllegalStateException(
                            "ERROR: Se encontro un pago duplicado."
                    );
                }

                if (pago.getIdsSocios().isEmpty()
                        || pago.getIdsSocios().size()
                        != pago.getNombresSocios().size()) {

                    throw new IllegalStateException(
                            "ERROR: Datos de socios inconsistentes."
                    );
                }

                if (pago.getFechaPago().isBefore(inicio)
                        || pago.getFechaPago().isAfter(fin)) {

                    throw new IllegalStateException(
                            "ERROR: Pago fuera del periodo."
                    );
                }

                sumaDetalle = sumaDetalle.add(pago.getMonto());
            }

            if (sumaDetalle.compareTo(
                    reporte.getTotalAcumulado()) != 0) {

                throw new IllegalStateException(
                        "ERROR: La suma del detalle no coincide "
                        + "con el total acumulado."
                );
            }

            System.out.println("Pagos sin duplicados: CORRECTO");
            System.out.println("Datos de socios: CORRECTO");
            System.out.println("Fechas del periodo: CORRECTO");
            System.out.println("Suma del detalle: CORRECTO");

            // PRUEBA 3: Membresias DOSPERSONAS.
            System.out.println(
                    "\n--- PRUEBA 3: MEMBRESIAS COMPARTIDAS ---"
            );

            int pagosCompartidos = 0;

            for (PagoReporte pago : reporte.getPagos()) {

                if (pago.getTipoMembresia()
                        == tipoMembresia.DOSPERSONAS) {

                    pagosCompartidos++;

                    if (pago.getIdsSocios().size() != 2) {
                        throw new IllegalStateException(
                                "ERROR: Una membresia DOSPERSONAS "
                                + "no tiene exactamente dos socios."
                        );
                    }

                    System.out.println(
                            "Pago " + pago.getIdPago()
                            + " - Socios: " + pago.getIdsSocios()
                    );
                }
            }

            System.out.println(
                    "Pagos compartidos encontrados: "
                    + pagosCompartidos
            );

            System.out.println(
                    "Membresias compartidas: CORRECTO"
            );

            // PRUEBA 4: Filtrado por una fecha exacta.
            System.out.println("\n--- PRUEBA 4: FILTRO POR FECHA ---");

            if (!reporte.getPagos().isEmpty()) {

                LocalDate fechaPrueba =
                        reporte.getPagos().get(0).getFechaPago();

                ReportePagos reporteFecha =
                        pagoService.generarReportePagos(
                                fechaPrueba,
                                fechaPrueba
                        );

                long pagosEsperados = reporte.getPagos()
                        .stream()
                        .filter(p -> p.getFechaPago()
                                .equals(fechaPrueba))
                        .count();

                if (reporteFecha.getPagos().size()
                        != pagosEsperados) {

                    throw new IllegalStateException(
                            "ERROR: El filtro por fecha no coincide."
                    );
                }

                BigDecimal totalEsperado = reporte.getPagos()
                        .stream()
                        .filter(p -> p.getFechaPago()
                                .equals(fechaPrueba))
                        .map(PagoReporte::getMonto)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                if (reporteFecha.getTotalAcumulado()
                        .compareTo(totalEsperado) != 0) {

                    throw new IllegalStateException(
                            "ERROR: El total del filtro es incorrecto."
                    );
                }

                System.out.println("Fecha consultada: " + fechaPrueba);
                System.out.println(
                        "Pagos encontrados: "
                        + reporteFecha.getPagos().size()
                );
                System.out.println(
                        "Total del dia: "
                        + reporteFecha.getTotalAcumulado()
                );
                System.out.println("Filtro por fecha: CORRECTO");

            } else {
                System.out.println(
                        "Sin pagos para comprobar el filtro por fecha."
                );
            }

            // PRUEBA 5: Periodo sin pagos.
            System.out.println("\n--- PRUEBA 5: REPORTE VACIO ---");

            ReportePagos reporteVacio =
                    pagoService.generarReportePagos(
                            LocalDate.of(1900, 1, 1),
                            LocalDate.of(1900, 12, 31)
                    );

            if (!reporteVacio.getPagos().isEmpty()
                    || reporteVacio.getTotalAcumulado()
                            .compareTo(BigDecimal.ZERO) != 0) {

                throw new IllegalStateException(
                        "ERROR: El reporte vacio contiene resultados."
                );
            }

            System.out.println("Lista vacia: CORRECTO");
            System.out.println(
                    "Total acumulado: "
                    + reporteVacio.getTotalAcumulado()
            );

            System.out.println(
                    "\nTODAS LAS PRUEBAS APROBADAS"
            );

        } finally {
            SesionUsuario.cerrar();
            System.out.println("\nSesion administrativa cerrada.");
        }
    }
}
