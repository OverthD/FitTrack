
package com.fittrack.model;

public class Empleado extends UsuarioSistema {

    public Empleado(int idEmpleado, String usuario,
                    String contrasenaHash, boolean activo) {
        super(
            idEmpleado,
            rolUsuario.EMPLEADO,
            usuario,
            contrasenaHash,
            activo
        );
    }

    public void registrarSocio() {
        throw new UnsupportedOperationException(
            "Registro de socios pendiente de implementar."
        );
    }

    public Socio consultarSocio() {
        throw new UnsupportedOperationException(
            "Consulta de socios pendiente de implementar."
        );
    }

    public void actualizarSocio() {
        throw new UnsupportedOperationException(
            "Actualización de socios pendiente de implementar."
        );
    }

    public void registrarMembresia() {
        throw new UnsupportedOperationException(
            "Registro de membresías pendiente de implementar."
        );
    }

    public Membresia consultarEstadoMembresia() {
        throw new UnsupportedOperationException(
            "Consulta de membresías pendiente de implementar."
        );
    }

    public void registrarPago() {
        throw new UnsupportedOperationException(
            "Registro de pagos pendiente de implementar."
        );
    }

    public void renovarMembresia() {
        throw new UnsupportedOperationException(
            "Renovación pendiente de implementar."
        );
    }

    public void registrarEntrada() {
        throw new UnsupportedOperationException(
            "Registro de entradas pendiente de implementar."
        );
    }

    public void registrarSalida() {
        throw new UnsupportedOperationException(
            "Registro de salidas pendiente de implementar."
        );
    }

    public void actualizarEstadoSocio() {
        throw new UnsupportedOperationException(
            "Actualización del estado del socio pendiente de implementar."
        );
    }

    public boolean validarEstudiante() {
        throw new UnsupportedOperationException(
            "Validación estudiantil pendiente de implementar."
        );
    }

    public void registrarMembresiaDoble() {
        throw new UnsupportedOperationException(
            "Registro de membresía doble pendiente de implementar."
        );
    }
}
