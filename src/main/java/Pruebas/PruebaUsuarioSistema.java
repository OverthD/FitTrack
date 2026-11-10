
package Pruebas;

import com.fittrack.dao.UsuarioSistemaDAO;
import com.fittrack.model.UsuarioSistema;
import com.fittrack.service.UsuarioSistemaService;
import com.fittrack.util.PasswordUtil;
import com.fittrack.util.SesionUsuario;

public class PruebaUsuarioSistema {

    public static void main(String[] args) throws Exception {

        UsuarioSistemaDAO dao = new UsuarioSistemaDAO();
        UsuarioSistemaService servicio =
                new UsuarioSistemaService();

        String usuarioAdmin = "admin_prueba";
        String contrasenaAdmin = "FitTrack2026!";

        String usuarioEmpleado = "empleado_prueba";
        String contrasenaEmpleado = "Empleado2026!";

        System.out.println("=== PRUEBA DEL MÓDULO DE USUARIOS ===");

        // 1. Comprobar que la tabla esté vacía.
        if (!dao.listarTodos().isEmpty()) {
            throw new IllegalStateException(
                "La tabla usuariosistema contiene registros. "
                + "Esta prueba inicial requiere una tabla vacía."
            );
        }

        System.out.println("\nPRUEBA 1: REGISTRO DEL ADMINISTRADOR");

        int idAdmin = servicio.registrarAdministradorInicial(
            usuarioAdmin,
            contrasenaAdmin
        );

        System.out.println("Administrador registrado.");
        System.out.println("ID generado: " + idAdmin);

        // 2. Comprobar almacenamiento del hash.
        System.out.println("\nPRUEBA 2: ALMACENAMIENTO DE CONTRASEÑA");

        UsuarioSistema administrador =
                dao.buscarPorUsuario(usuarioAdmin);

        if (administrador == null) {
            throw new AssertionError(
                "No se encontró el administrador registrado."
            );
        }

        String hash = administrador.getContrasenaHash();

        if (hash.equals(contrasenaAdmin)
                || !PasswordUtil.verificarContrasena(
                    contrasenaAdmin, hash)) {
            throw new AssertionError(
                "El hash de la contraseña no es correcto."
            );
        }

        System.out.println("La contraseña se almacenó como hash.");
        System.out.println("Verificación del hash correcta.");

        // 3. Comprobar inicio de sesión.
        System.out.println("\nPRUEBA 3: INICIO DE SESIÓN");

        UsuarioSistema sesionAdmin =
                servicio.iniciarSesion(
                    usuarioAdmin,
                    contrasenaAdmin
                );

        if (sesionAdmin == null) {
            throw new AssertionError(
                "No se pudo iniciar sesión como administrador."
            );
        }

        System.out.println("Inicio de sesión correcto.");
        System.out.println("ID: "
                + SesionUsuario.obtenerIdEmpleado());
        System.out.println("Rol: " + sesionAdmin.getRol());

        // 4. Comprobar contraseña incorrecta.
        System.out.println("\nPRUEBA 4: CONTRASEÑA INCORRECTA");

        servicio.cerrarSesion();

        UsuarioSistema accesoIncorrecto =
                servicio.iniciarSesion(
                    usuarioAdmin,
                    "ContraseñaIncorrecta"
                );

        if (accesoIncorrecto != null
                || SesionUsuario.haySesionActiva()) {
            throw new AssertionError(
                "Se permitió el acceso con contraseña incorrecta."
            );
        }

        System.out.println(
            "Acceso rechazado correctamente."
        );

        // 5. Registrar un empleado.
        System.out.println("\nPRUEBA 5: REGISTRO DE EMPLEADO");

        servicio.iniciarSesion(
            usuarioAdmin,
            contrasenaAdmin
        );

        int idEmpleado = servicio.registrarEmpleado(
            usuarioEmpleado,
            contrasenaEmpleado
        );

        System.out.println("Empleado registrado.");
        System.out.println("ID generado: " + idEmpleado);

        // 6. Comprobar el rol del empleado.
        UsuarioSistema empleado =
                dao.buscarPorId(idEmpleado);

        if (empleado == null
                || empleado.getRol().name().equals("ADMINISTRADOR")) {
            throw new AssertionError(
                "El empleado no se registró con el rol correcto."
            );
        }

        System.out.println("Rol comprobado: " + empleado.getRol());

        // 7. Comprobar inicio de sesión del empleado.
        System.out.println("\nPRUEBA 6: INICIO DE SESIÓN DE EMPLEADO");

        servicio.cerrarSesion();

        UsuarioSistema sesionEmpleado =
                servicio.iniciarSesion(
                    usuarioEmpleado,
                    contrasenaEmpleado
                );

        if (sesionEmpleado == null) {
            throw new AssertionError(
                "No se pudo iniciar sesión como empleado."
            );
        }

        System.out.println("Inicio de sesión correcto.");
        System.out.println("ID: "
                + SesionUsuario.obtenerIdEmpleado());

        // 8. Desactivar empleado desde administrador.
        System.out.println("\nPRUEBA 7: DESACTIVAR EMPLEADO");

        servicio.cerrarSesion();

        servicio.iniciarSesion(
            usuarioAdmin,
            contrasenaAdmin
        );

        boolean desactivado =
                servicio.cambiarEstado(idEmpleado, false);

        if (!desactivado) {
            throw new AssertionError(
                "No se pudo desactivar el empleado."
            );
        }

        System.out.println("Empleado desactivado.");

        // 9. Comprobar rechazo de cuenta inactiva.
        System.out.println("\nPRUEBA 8: ACCESO DE CUENTA INACTIVA");

        servicio.cerrarSesion();

        UsuarioSistema accesoInactivo =
                servicio.iniciarSesion(
                    usuarioEmpleado,
                    contrasenaEmpleado
                );

        if (accesoInactivo != null
                || SesionUsuario.haySesionActiva()) {
            throw new AssertionError(
                "Se permitió el acceso a una cuenta inactiva."
            );
        }

        System.out.println(
            "Acceso rechazado correctamente."
        );

        // 10. Reactivar empleado.
        System.out.println("\nPRUEBA 9: REACTIVAR EMPLEADO");

        servicio.iniciarSesion(
            usuarioAdmin,
            contrasenaAdmin
        );

        servicio.cambiarEstado(idEmpleado, true);

        servicio.cerrarSesion();

        UsuarioSistema empleadoReactivado =
                servicio.iniciarSesion(
                    usuarioEmpleado,
                    contrasenaEmpleado
                );

        if (empleadoReactivado == null) {
            throw new AssertionError(
                "No se pudo iniciar sesión después de reactivar."
            );
        }

        System.out.println("Empleado reactivado.");
        System.out.println("Inicio de sesión correcto.");

        servicio.cerrarSesion();

        System.out.println(
            "\n=== TODAS LAS PRUEBAS FINALIZARON CORRECTAMENTE ==="
        );
    }
}
