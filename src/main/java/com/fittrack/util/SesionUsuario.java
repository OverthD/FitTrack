
package com.fittrack.util;

import com.fittrack.model.UsuarioSistema;

public class SesionUsuario {

    private static UsuarioSistema usuarioActual;

    private SesionUsuario() {
    }

    // Iniciar una sesión.
    public static void iniciar(UsuarioSistema usuario) {

        if (usuario == null || !usuario.isActivo()) {
            throw new IllegalArgumentException(
                "No se puede iniciar una sesión con este usuario."
            );
        }

        usuarioActual = usuario;
    }

    // Obtener el usuario autenticado.
    public static UsuarioSistema obtenerUsuarioActual() {
        return usuarioActual;
    }

    // Obtener el identificador del empleado autenticado.
    public static int obtenerIdEmpleado() {

        if (usuarioActual == null) {
            throw new IllegalStateException(
                "No hay una sesión iniciada."
            );
        }

        return usuarioActual.getIdEmpleado();
    }

    // Comprobar si existe una sesión activa.
    public static boolean haySesionActiva() {
        return usuarioActual != null;
    }

    // Comprobar si el usuario actual es administrador.
    public static boolean esAdministrador() {

        return usuarioActual != null
            && usuarioActual.getRol()
                == com.fittrack.model.rolUsuario.ADMINISTRADOR;
    }

    // Cerrar la sesión.
    public static void cerrar() {
        usuarioActual = null;
    }
}
