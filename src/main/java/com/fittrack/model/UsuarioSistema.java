package com.fittrack.model;

import java.util.List;

public class UsuarioSistema {

    private int idEmpleado;
    private rolUsuario rol;
    private String usuario;
    private String contrasenaHash;
    private boolean activo;

    public UsuarioSistema(int idEmpleado, rolUsuario rol,
            String usuario, String contrasenaHash,
            boolean activo) {
        this.idEmpleado = idEmpleado;
        this.rol = rol;
        this.usuario = usuario;
        this.contrasenaHash = contrasenaHash;
        this.activo = activo;
    }

    public boolean iniciarSesion(String usuario, String contrasenaHash) {
        if (!activo) {
            return false;
        }

        return this.usuario.equals(usuario)
                && this.contrasenaHash.equals(contrasenaHash);
    }

    public List<Acceso> consultarAccesos() {
        // La consulta real se implementará con el DAO.
        throw new UnsupportedOperationException(
                "Consulta de accesos pendiente de implementar."
        );
    }

    public List<Pago> consultaPagos() {
        // La consulta real se implementará con el DAO.
        throw new UnsupportedOperationException(
                "Consulta de pagos pendiente de implementar."
        );
    }

    public List<Membresia> consultarMembresiasProximasVencer() {
        // La consulta real se implementará con el DAO.
        throw new UnsupportedOperationException(
                "Consulta de membresías pendientes de implementar."
        );
    }

    public int getIdEmpleado() {
        return idEmpleado;
    }

    public rolUsuario getRol() {
        return rol;
    }

    public String getUsuario() {
        return usuario;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public boolean verificarContrasenaHash(String hash) {
        return contrasenaHash.equals(hash);
    }

    public String getContrasenaHash() {
        return contrasenaHash;
    }

    public void setIdEmpleado(int idEmpleado) {
        this.idEmpleado = idEmpleado;
    }
}
