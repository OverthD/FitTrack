
package com.fittrack;

import com.fittrack.model.Acceso;
import com.fittrack.service.AccesoService;
import com.fittrack.service.UsuarioSistemaService;

import java.util.List;

public class PruebaAccesoService {

    public static void main(String[] args) throws Exception {

        UsuarioSistemaService usuarioService =
                new UsuarioSistemaService();

        AccesoService accesoService =
                new AccesoService();

        int idSocio = 7;
        int idAcceso = 0;

        System.out.println("=== PRUEBAS DE CONTROL DE ACCESOS ===");

        try {

            // =====================================================
            // PRUEBA 1: INICIO DE SESION
            // =====================================================

            System.out.println("\nPRUEBA 1: INICIO DE SESION");

            usuarioService.iniciarSesion(
                    "admin_prueba",
                    "FitTrack2026!"
            );

            System.out.println("Sesion iniciada correctamente.");


            // =====================================================
            // PRUEBA 2: REGISTRAR ENTRADA
            // =====================================================

            System.out.println("\nPRUEBA 2: REGISTRAR ENTRADA");

            idAcceso = accesoService.registrarEntrada(idSocio);

            System.out.println("Entrada registrada correctamente.");
            System.out.println("Identificador del acceso: " + idAcceso);


            // =====================================================
            // PRUEBA 3: RECHAZAR SEGUNDA ENTRADA
            // =====================================================

            System.out.println("\nPRUEBA 3: RECHAZAR SEGUNDA ENTRADA");

            try {

                accesoService.registrarEntrada(idSocio);

                throw new IllegalStateException(
                        "ERROR: Se permitio registrar una segunda entrada."
                );

            } catch (IllegalArgumentException e) {

                System.out.println("Correcto: " + e.getMessage());
            }


            // =====================================================
            // PRUEBA 4: REGISTRAR SALIDA
            // =====================================================

            System.out.println("\nPRUEBA 4: REGISTRAR SALIDA");

            boolean salidaRegistrada =
                    accesoService.registrarSalida(idSocio);

            if (!salidaRegistrada) {
                throw new IllegalStateException(
                        "ERROR: No se pudo registrar la salida."
                );
            }

            System.out.println("Salida registrada correctamente.");


            // =====================================================
            // PRUEBA 5: RECHAZAR SEGUNDA SALIDA
            // =====================================================

            System.out.println("\nPRUEBA 5: RECHAZAR SEGUNDA SALIDA");

            try {

                accesoService.registrarSalida(idSocio);

                throw new IllegalStateException(
                        "ERROR: Se permitio registrar una segunda salida."
                );

            } catch (IllegalArgumentException e) {

                System.out.println("Correcto: " + e.getMessage());
            }


            // =====================================================
            // PRUEBA 6: CONSULTAR HISTORIAL
            // =====================================================

            System.out.println("\nPRUEBA 6: CONSULTAR HISTORIAL");

            List<Acceso> historial =
                    accesoService.consultarHistorial(idSocio);

            if (historial.isEmpty()) {
                throw new IllegalStateException(
                        "ERROR: No se encontro el acceso registrado."
                );
            }

            Acceso accesoRegistrado = null;

            for (Acceso acceso : historial) {

                if (acceso.getIdAcceso() == idAcceso) {
                    accesoRegistrado = acceso;
                    break;
                }
            }

            if (accesoRegistrado == null) {
                throw new IllegalStateException(
                        "ERROR: No se encontro el acceso esperado."
                );
            }

            if (accesoRegistrado.getHoraSalida() == null) {
                throw new IllegalStateException(
                        "ERROR: El acceso no tiene hora de salida."
                );
            }

            System.out.println("Historial consultado correctamente.");
            System.out.println("ID acceso: " + accesoRegistrado.getIdAcceso());
            System.out.println("ID socio: " + accesoRegistrado.getIdSocio());
            System.out.println("ID empleado: " + accesoRegistrado.getIdEmpleado());
            System.out.println("Fecha: " + accesoRegistrado.getFecha());
            System.out.println("Hora entrada: " + accesoRegistrado.getHoraEntrada());
            System.out.println("Hora salida: " + accesoRegistrado.getHoraSalida());


            // =====================================================
            // PRUEBA 7: RECHAZAR SOCIO INEXISTENTE
            // =====================================================

            System.out.println("\nPRUEBA 7: RECHAZAR SOCIO INEXISTENTE");

            try {

                accesoService.registrarEntrada(99999);

                throw new IllegalStateException(
                        "ERROR: Se permitio el acceso de un socio inexistente."
                );

            } catch (IllegalArgumentException e) {

                System.out.println("Correcto: " + e.getMessage());
            }


            // =====================================================
            // PRUEBA 8: RECHAZAR SOCIO INACTIVO
            // =====================================================

            System.out.println("\nPRUEBA 8: RECHAZAR SOCIO INACTIVO");

            try {

                accesoService.registrarEntrada(1);

                throw new IllegalStateException(
                        "ERROR: Se permitio el acceso de un socio inactivo."
                );

            } catch (IllegalArgumentException e) {

                System.out.println("Correcto: " + e.getMessage());
            }


            // =====================================================
            // PRUEBA 9: RECHAZAR MEMBRESIA PROGRAMADA
            // =====================================================

            System.out.println("\nPRUEBA 9: RECHAZAR MEMBRESIA PROGRAMADA");

            try {

                accesoService.registrarEntrada(3);

                throw new IllegalStateException(
                        "ERROR: Se permitio el acceso con una membresia "
                        + "programada y ninguna vigente."
                );

            } catch (IllegalArgumentException e) {

                System.out.println("Correcto: " + e.getMessage());
            }


            // =====================================================
            // PRUEBA 10: RECHAZAR MEMBRESIA VENCIDA
            // =====================================================

            System.out.println("\nPRUEBA 10: RECHAZAR MEMBRESIA VENCIDA");

            try {

                accesoService.registrarEntrada(11);

                throw new IllegalStateException(
                        "ERROR: Se permitio el acceso con una membresia vencida."
                );

            } catch (IllegalArgumentException e) {

                System.out.println("Correcto: " + e.getMessage());
            }


            // =====================================================
            // PRUEBA 11: PERMITIR ACCESO EN TOLERANCIA
            // =====================================================

            System.out.println("\nPRUEBA 11: PERMITIR ACCESO EN TOLERANCIA");

            int idAccesoTolerancia =
                    accesoService.registrarEntrada(10);

            System.out.println(
                    "Correcto: Se permitio la entrada durante la tolerancia."
            );

            System.out.println(
                    "Identificador del acceso: " + idAccesoTolerancia
            );

            boolean salidaTolerancia =
                    accesoService.registrarSalida(10);

            if (!salidaTolerancia) {
                throw new IllegalStateException(
                        "ERROR: No se pudo cerrar el acceso de tolerancia."
                );
            }

            System.out.println(
                    "Salida registrada correctamente."
            );


            // =====================================================
            // RESULTADO FINAL
            // =====================================================

            System.out.println(
                    "\n=========================================="
            );

            System.out.println(
                    "TODAS LAS PRUEBAS FINALIZARON CORRECTAMENTE."
            );

            System.out.println(
                    "=========================================="
            );

        } finally {

            usuarioService.cerrarSesion();
        }
    }
}
