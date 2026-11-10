
package Pruebas;

import com.fittrack.dao.UsuarioSistemaDAO;
import com.fittrack.model.UsuarioSistema;
import com.fittrack.model.rolUsuario;
import com.fittrack.service.UsuarioSistemaService;
import com.fittrack.util.PasswordUtil;
import com.fittrack.util.SesionUsuario;

public class PruebaRF17 {

    public static void main(String[] args) throws Exception {

        UsuarioSistemaService servicio =
                new UsuarioSistemaService();

        UsuarioSistemaDAO dao =
                new UsuarioSistemaDAO();

        // Credenciales del administrador.
        String usuarioAdmin = "admin_prueba";
        String contrasenaAdmin = "FitTrack2026!";

        int idEmpleadoPrueba = 2;

        String usuarioTemporal = "empleado_rf17_temporal";
        String contrasenaTemporal = "FitTrack2026!";

        System.out.println("\n===== PRUEBAS RF-17 =====");

        try {

            // 1. Iniciar sesión administrativa.
            UsuarioSistema admin = servicio.iniciarSesion(
                    usuarioAdmin,
                    contrasenaAdmin
            );

            if (admin == null ||
                    admin.getRol() != rolUsuario.ADMINISTRADOR) {
                throw new IllegalStateException(
                        "No se pudo iniciar sesión como administrador."
                );
            }

            System.out.println("1. Inicio administrativo: CORRECTO");

            // 2. Consultar empleado y guardar datos originales.
            UsuarioSistema empleadoOriginal =
                    servicio.buscarPorId(idEmpleadoPrueba);

            if (empleadoOriginal == null) {
                throw new IllegalStateException(
                        "No existe el empleado de prueba."
                );
            }

            String usuarioOriginal =
                    empleadoOriginal.getUsuario();

            String hashOriginal =
                    empleadoOriginal.getContrasenaHash();

            rolUsuario rolOriginal =
                    empleadoOriginal.getRol();

            boolean estadoOriginal =
                    empleadoOriginal.isActivo();

            System.out.println("2. Consulta por ID: CORRECTO");

            try {

                // 3. Modificar nombre de usuario.
                boolean modificado = servicio.actualizarDatos(
                        idEmpleadoPrueba,
                        rolUsuario.EMPLEADO,
                        usuarioTemporal
                );

                if (!modificado) {
                    throw new IllegalStateException(
                            "No se modificaron los datos."
                    );
                }

                System.out.println("3. Modificación: CORRECTO");

                // 4. Cambiar contraseña.
                boolean contrasenaCambiada =
                        servicio.cambiarContrasena(
                                idEmpleadoPrueba,
                                contrasenaTemporal
                        );

                if (!contrasenaCambiada) {
                    throw new IllegalStateException(
                            "No se pudo cambiar la contraseña."
                    );
                }

                UsuarioSistema empleadoActual =
                        dao.buscarPorId(idEmpleadoPrueba);

                boolean nuevaValida =
                        PasswordUtil.verificarContrasena(
                                contrasenaTemporal,
                                empleadoActual.getContrasenaHash()
                        );

                boolean anteriorValida =
                        PasswordUtil.verificarContrasena(
                                "contraseña_incorrecta",
                                empleadoActual.getContrasenaHash()
                        );

                if (!nuevaValida || anteriorValida) {
                    throw new IllegalStateException(
                            "La validación de contraseña falló."
                    );
                }

                System.out.println("4. Cambio de contraseña: CORRECTO");

                // 5. Desactivar empleado.
                servicio.cambiarEstado(idEmpleadoPrueba, false);

                UsuarioSistema empleadoDesactivado =
                        dao.buscarPorId(idEmpleadoPrueba);

                if (empleadoDesactivado.isActivo()) {
                    throw new IllegalStateException(
                            "El empleado continúa activo."
                    );
                }

                System.out.println("5. Desactivación: CORRECTO");

                // 6. Comprobar que no pueda iniciar sesión.
                servicio.cerrarSesion();

                UsuarioSistema intentoLogin =
                        servicio.iniciarSesion(
                                usuarioTemporal,
                                contrasenaTemporal
                        );

                if (intentoLogin != null) {
                    throw new IllegalStateException(
                            "Un usuario inactivo pudo iniciar sesión."
                    );
                }

                System.out.println("6. Bloqueo de acceso: CORRECTO");

                // Recuperar sesión administrativa.
                servicio.iniciarSesion(
                        usuarioAdmin,
                        contrasenaAdmin
                );

                // 7. Reactivar empleado.
                servicio.cambiarEstado(idEmpleadoPrueba, true);

                UsuarioSistema empleadoReactivado =
                        dao.buscarPorId(idEmpleadoPrueba);

                if (!empleadoReactivado.isActivo()) {
                    throw new IllegalStateException(
                            "El empleado no fue reactivado."
                    );
                }

                System.out.println("7. Reactivación: CORRECTO");

                // 8. Intentar desactivar al administrador actual.
                boolean bloqueoDesactivacion = false;

                try {
                    servicio.cambiarEstado(admin.getIdEmpleado(), false);
                } catch (IllegalArgumentException ex) {
                    bloqueoDesactivacion = true;
                }

                if (!bloqueoDesactivacion) {
                    throw new IllegalStateException(
                            "Se permitió desactivar al administrador actual."
                    );
                }

                System.out.println(
                        "8. Protección de administrador: CORRECTO"
                );

                // 9. Intentar cambiar el propio rol.
                boolean bloqueoRol = false;

                try {
                    servicio.actualizarDatos(
                            admin.getIdEmpleado(),
                            rolUsuario.EMPLEADO,
                            usuarioAdmin
                    );
                } catch (IllegalArgumentException ex) {
                    bloqueoRol = true;
                }

                if (!bloqueoRol) {
                    throw new IllegalStateException(
                            "Se permitió modificar el rol propio."
                    );
                }

                System.out.println(
                        "9. Protección de rol administrativo: CORRECTO"
                );

            } finally {

                // Restaurar los datos originales del empleado.
                dao.actualizarDatos(
                        idEmpleadoPrueba,
                        rolOriginal,
                        usuarioOriginal
                );

                dao.actualizarContrasena(
                        idEmpleadoPrueba,
                        hashOriginal
                );

                dao.cambiarEstado(
                        idEmpleadoPrueba,
                        estadoOriginal
                );

                System.out.println(
                        "\nDatos originales del empleado restaurados."
                );
            }

        } finally {
            servicio.cerrarSesion();
        }

        System.out.println("\n===== FIN DE PRUEBAS RF-17 =====");
    }
}
