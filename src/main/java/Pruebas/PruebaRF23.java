
package Pruebas;

import com.fittrack.model.Acceso;
import com.fittrack.model.Socio;
import com.fittrack.model.UsuarioSistema;
import com.fittrack.model.estadoSocio;
import com.fittrack.model.rolUsuario;

import com.fittrack.service.AccesoService;
import com.fittrack.service.SocioService;
import com.fittrack.service.UsuarioSistemaService;

import com.fittrack.util.SesionUsuario;

import java.util.List;

public class PruebaRF23 {

    public static void main(String[] args) {

        System.out.println("===== DIAGNOSTICO RF-23 =====");

        UsuarioSistemaService usuarioService =
                new UsuarioSistemaService();

        SocioService socioService = new SocioService();
        AccesoService accesoService = new AccesoService();

        int idSocio = 1;

        String usuarioEmpleado = "empleado_prueba";
        String contrasenaEmpleado = "Empleado2026!";

        String usuarioAdmin = "admin_prueba";
        String contrasenaAdmin = "FitTrack2026!";

        estadoSocio estadoOriginal = null;
        boolean estadoRestaurado = false;

        try {

            // =====================================================
            // INICIAR SESION COMO EMPLEADO
            // =====================================================

            UsuarioSistema empleado = usuarioService.iniciarSesion(
                    usuarioEmpleado,
                    contrasenaEmpleado
            );

            if (empleado == null
                    || empleado.getRol() != rolUsuario.EMPLEADO) {

                throw new AssertionError(
                        "No se pudo iniciar sesión como empleado."
                );
            }

            System.out.println("Sesión de empleado iniciada.");

            // Obtener el socio y guardar su estado original.
            Socio socioOriginal = socioService.consultarSocio(idSocio);

            if (socioOriginal == null) {
                throw new AssertionError(
                        "No existe el socio de prueba."
                );
            }

            estadoOriginal = socioOriginal.getEstado();

            System.out.println("Socio de prueba: " + idSocio);
            System.out.println("Estado original: " + estadoOriginal);

            // Guardar historial antes de modificar el estado.
            List<Acceso> historialInicial =
                    accesoService.consultarHistorial(idSocio);

            System.out.println(
                    "Accesos históricos iniciales: "
                    + historialInicial.size()
            );

            // =====================================================
            // PRUEBA 1: DESACTIVAR SOCIO
            // =====================================================

            System.out.println("\n--- PRUEBA 1: DESACTIVAR SOCIO ---");

            boolean actualizado = socioService.cambiarEstadoSocio(
                    idSocio,
                    estadoSocio.INACTIVO
            );

            Socio socioInactivo =
                    socioService.consultarSocio(idSocio);

            System.out.println("Actualización: " + actualizado);
            System.out.println(
                    "Estado obtenido: " + socioInactivo.getEstado()
            );

            if (!actualizado
                    || socioInactivo.getEstado() != estadoSocio.INACTIVO) {

                throw new AssertionError(
                        "No se pudo desactivar el socio."
                );
            }

            System.out.println("Resultado: CORRECTO");

            // =====================================================
            // PRUEBA 2: RECHAZAR ACCESO DE SOCIO INACTIVO
            // =====================================================

            System.out.println(
                    "\n--- PRUEBA 2: ACCESO DE SOCIO INACTIVO ---"
            );

            probarAccesoDenegado(accesoService, idSocio);

            // =====================================================
            // PRUEBA 3: DAR DE BAJA
            // =====================================================

            System.out.println("\n--- PRUEBA 3: DAR DE BAJA ---");

            actualizado = socioService.cambiarEstadoSocio(
                    idSocio,
                    estadoSocio.BAJA
            );

            Socio socioBaja =
                    socioService.consultarSocio(idSocio);

            System.out.println("Actualización: " + actualizado);
            System.out.println(
                    "Estado obtenido: " + socioBaja.getEstado()
            );

            if (!actualizado
                    || socioBaja.getEstado() != estadoSocio.BAJA) {

                throw new AssertionError(
                        "No se pudo dar de baja al socio."
                );
            }

            System.out.println("Resultado: CORRECTO");

            // =====================================================
            // PRUEBA 4: RECHAZAR ACCESO DE SOCIO DADO DE BAJA
            // =====================================================

            System.out.println(
                    "\n--- PRUEBA 4: ACCESO DE SOCIO DADO DE BAJA ---"
            );

            probarAccesoDenegado(accesoService, idSocio);

            // =====================================================
            // PRUEBA 5: REACTIVAR SOCIO
            // =====================================================

            System.out.println("\n--- PRUEBA 5: REACTIVAR SOCIO ---");

            actualizado = socioService.cambiarEstadoSocio(
                    idSocio,
                    estadoSocio.ACTIVO
            );

            Socio socioReactivado =
                    socioService.consultarSocio(idSocio);

            System.out.println("Actualización: " + actualizado);
            System.out.println(
                    "Estado obtenido: " + socioReactivado.getEstado()
            );

            if (!actualizado
                    || socioReactivado.getEstado() != estadoSocio.ACTIVO) {

                throw new AssertionError(
                        "No se pudo reactivar el socio."
                );
            }

            System.out.println("Resultado: CORRECTO");

            // =====================================================
            // PRUEBA 6: CONSERVACION DEL HISTORIAL
            // =====================================================

            System.out.println(
                    "\n--- PRUEBA 6: CONSERVACION DEL HISTORIAL ---"
            );

            List<Acceso> historialFinal =
                    accesoService.consultarHistorial(idSocio);

            boolean historialConservado =
                    historialInicial.size() == historialFinal.size();

            if (historialConservado) {

                for (int i = 0; i < historialInicial.size(); i++) {

                    if (historialInicial.get(i).getIdAcceso()
                            != historialFinal.get(i).getIdAcceso()) {

                        historialConservado = false;
                        break;
                    }
                }
            }

            System.out.println(
                    "Accesos iniciales: " + historialInicial.size()
            );

            System.out.println(
                    "Accesos finales: " + historialFinal.size()
            );

            System.out.println(
                    "Historial conservado: " + historialConservado
            );

            if (!historialConservado) {
                throw new AssertionError(
                        "El historial del socio fue modificado."
                );
            }

            // Comprobar que la información personal sigue existiendo.
            Socio socioConsultado =
                    socioService.consultarSocio(idSocio);

            if (socioConsultado == null
                    || !socioConsultado.getNombre().equals(
                            socioOriginal.getNombre())
                    || !socioConsultado.getApellido().equals(
                            socioOriginal.getApellido())) {

                throw new AssertionError(
                        "La información del socio no se conservó."
                );
            }

            System.out.println("Datos personales conservados: CORRECTO");
            System.out.println("Resultado: CORRECTO");

            // Restaurar estado original antes de continuar.
            socioService.cambiarEstadoSocio(
                    idSocio,
                    estadoOriginal
            );

            estadoRestaurado = true;

            // =====================================================
            // PRUEBA 7: ADMINISTRADOR
            // =====================================================

            System.out.println(
                    "\n--- PRUEBA 7: PERMISOS DE ADMINISTRADOR ---"
            );

            usuarioService.cerrarSesion();

            UsuarioSistema administrador =
                    usuarioService.iniciarSesion(
                            usuarioAdmin,
                            contrasenaAdmin
                    );

            if (administrador == null
                    || administrador.getRol()
                            != rolUsuario.ADMINISTRADOR) {

                throw new AssertionError(
                        "No se pudo iniciar sesión como administrador."
                );
            }

            estadoSocio estadoTemporal =
                    estadoOriginal == estadoSocio.ACTIVO
                            ? estadoSocio.INACTIVO
                            : estadoSocio.ACTIVO;

            actualizado = socioService.cambiarEstadoSocio(
                    idSocio,
                    estadoTemporal
            );

            if (!actualizado
                    || socioService.consultarSocio(idSocio).getEstado()
                            != estadoTemporal) {

                throw new AssertionError(
                        "El administrador no pudo cambiar el estado."
                );
            }

            // Restaurar inmediatamente el estado original.
            socioService.cambiarEstadoSocio(
                    idSocio,
                    estadoOriginal
            );

            System.out.println(
                    "Administrador pudo cambiar el estado: CORRECTO"
            );

            // =====================================================
            // PRUEBA 8: SOCIO INEXISTENTE
            // =====================================================

            System.out.println(
                    "\n--- PRUEBA 8: SOCIO INEXISTENTE ---"
            );

            boolean resultado = socioService.cambiarEstadoSocio(
                    999999,
                    estadoSocio.ACTIVO
            );

            System.out.println(
                    "Actualización de ID inexistente: " + resultado
            );

            if (resultado) {
                throw new AssertionError(
                        "Se modificó un socio inexistente."
                );
            }

            System.out.println("Resultado: CORRECTO");

            // =====================================================
            // PRUEBA 9: IDENTIFICADORES Y ESTADOS INVALIDOS
            // =====================================================

            System.out.println(
                    "\n--- PRUEBA 9: DATOS INVALIDOS ---"
            );

            probarDatosInvalidos(socioService);

            System.out.println("Resultado: CORRECTO");

            // =====================================================
            // PRUEBA 10: SIN SESION
            // =====================================================

            System.out.println(
                    "\n--- PRUEBA 10: SIN SESION ---"
            );

            usuarioService.cerrarSesion();

            try {

                socioService.cambiarEstadoSocio(
                        idSocio,
                        estadoSocio.INACTIVO
                );

                throw new AssertionError(
                        "Se permitió cambiar el estado sin sesión."
                );

            } catch (IllegalStateException e) {

                System.out.println(
                        "Operación sin sesión: RECHAZADA"
                );
            }

            System.out.println("Resultado: CORRECTO");

            System.out.println(
                    "\nTODAS LAS PRUEBAS COMPLETADAS"
            );

        } catch (Exception e) {

            System.err.println(
                    "ERROR EN LA PRUEBA: " + e.getMessage()
            );

            e.printStackTrace();

        } finally {

            // Intentar restaurar el estado original si hubo algún error.
            if (estadoOriginal != null && !estadoRestaurado) {

                try {

                    if (!SesionUsuario.haySesionActiva()) {

                        usuarioService.iniciarSesion(
                                usuarioAdmin,
                                contrasenaAdmin
                        );
                    }

                    if (SesionUsuario.haySesionActiva()) {

                        socioService.cambiarEstadoSocio(
                                idSocio,
                                estadoOriginal
                        );

                        System.out.println(
                                "Estado original del socio restaurado."
                        );
                    }

                } catch (Exception e) {

                    System.err.println(
                            "ADVERTENCIA: No se pudo restaurar "
                            + "el estado original: " + e.getMessage()
                    );
                }
            }

            if (SesionUsuario.haySesionActiva()) {
                usuarioService.cerrarSesion();
            }

            System.out.println("\nDiagnóstico finalizado.");
        }
    }

    // Comprobar que el acceso sea rechazado.
    private static void probarAccesoDenegado(
            AccesoService accesoService,
            int idSocio) throws Exception {

        try {

            accesoService.registrarEntrada(idSocio);

            throw new AssertionError(
                    "Se permitió el acceso a un socio no activo."
            );

        } catch (IllegalArgumentException e) {

            System.out.println(
                    "Acceso rechazado: " + e.getMessage()
            );
        }

        System.out.println("Resultado: CORRECTO");
    }

    // Comprobar validación de ID y estado.
    private static void probarDatosInvalidos(
            SocioService socioService) throws Exception {

        try {

            socioService.cambiarEstadoSocio(
                    0,
                    estadoSocio.ACTIVO
            );

            throw new AssertionError(
                    "Se aceptó un ID igual a cero."
            );

        } catch (IllegalArgumentException e) {

            System.out.println("ID 0: RECHAZADO");
        }

        try {

            socioService.cambiarEstadoSocio(
                    -1,
                    estadoSocio.ACTIVO
            );

            throw new AssertionError(
                    "Se aceptó un ID negativo."
            );

        } catch (IllegalArgumentException e) {

            System.out.println("ID -1: RECHAZADO");
        }

        try {

            socioService.cambiarEstadoSocio(
                    1,
                    null
            );

            throw new AssertionError(
                    "Se aceptó un estado nulo."
            );

        } catch (IllegalArgumentException e) {

            System.out.println("Estado nulo: RECHAZADO");
        }
    }
}
