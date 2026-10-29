
package com.fittrack.model;

public class Administrador extends UsuarioSistema {

    public Administrador(int idEmpleado, String usuario,
                         String contrasenaHash, boolean activo) {
        super(
            idEmpleado,
            rolUsuario.ADMINISTRADOR,
            usuario,
            contrasenaHash,
            activo
        );
    }

    public double consultarIngresos() {
        throw new UnsupportedOperationException(
            "Consulta de ingresos pendiente de implementar."
        );
    }

    public double consultarVentas() {
        throw new UnsupportedOperationException(
            "Consulta de ventas pendiente de implementar."
        );
    }

    public void gestionarUsuarios() {
        throw new UnsupportedOperationException(
            "Gestión de usuarios pendiente de implementar."
        );
    }

    public void generarRespaldo() {
        throw new UnsupportedOperationException(
            "Generación de respaldos pendiente de implementar."
        );
    }
}
