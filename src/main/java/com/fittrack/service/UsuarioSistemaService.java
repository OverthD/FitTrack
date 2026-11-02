
package com.fittrack.service;

import com.fittrack.dao.UsuarioSistemaDAO;
import com.fittrack.model.Administrador;
import com.fittrack.model.Empleado;
import com.fittrack.model.UsuarioSistema;
import com.fittrack.model.rolUsuario;
import com.fittrack.util.PasswordUtil;
import com.fittrack.util.SesionUsuario;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class UsuarioSistemaService {

    private final UsuarioSistemaDAO usuarioDAO;

    public UsuarioSistemaService() {
        this.usuarioDAO = new UsuarioSistemaDAO();
    }

    // Registrar el primer administrador del sistema.
    public int registrarAdministradorInicial(
            String usuario,
            String contrasena)
            throws SQLException, IOException {

        if (!usuarioDAO.listarTodos().isEmpty()) {
            throw new IllegalStateException(
                "Ya existen usuarios registrados. "
                + "El administrador inicial ya fue creado."
            );
        }

        validarCredenciales(usuario, contrasena);

        String nombreUsuario = usuario.trim();

        String hash = PasswordUtil.generarHash(contrasena);

        Administrador administrador = new Administrador(
            0,
            nombreUsuario,
            hash,
            true
        );

        return usuarioDAO.registrar(administrador);
    }

    // Registrar un empleado. Requiere un administrador autenticado.
    public int registrarEmpleado(
            String usuario,
            String contrasena)
            throws SQLException, IOException {

        exigirAdministrador();

        validarCredenciales(usuario, contrasena);

        String nombreUsuario = usuario.trim();

        String hash = PasswordUtil.generarHash(contrasena);

        Empleado empleado = new Empleado(
            0,
            nombreUsuario,
            hash,
            true
        );

        return usuarioDAO.registrar(empleado);
    }

    // Iniciar sesión.
    public UsuarioSistema iniciarSesion(
            String usuario,
            String contrasena)
            throws SQLException, IOException {

        if (usuario == null || usuario.isBlank()
                || contrasena == null || contrasena.isEmpty()) {
            return null;
        }

        UsuarioSistema encontrado =
                usuarioDAO.buscarPorUsuario(usuario.trim());

        if (encontrado == null || !encontrado.isActivo()) {
            return null;
        }

        boolean contrasenaValida =
                PasswordUtil.verificarContrasena(
                    contrasena,
                    encontrado.getContrasenaHash()
                );

        if (!contrasenaValida) {
            return null;
        }

        SesionUsuario.iniciar(encontrado);

        return encontrado;
    }

    // Cerrar la sesión actual.
    public void cerrarSesion() {
        SesionUsuario.cerrar();
    }

    // Consultar todos los usuarios.
    public List<UsuarioSistema> listarUsuarios()
            throws SQLException, IOException {

        exigirAdministrador();

        return usuarioDAO.listarTodos();
    }

    // Buscar un usuario por identificador.
    public UsuarioSistema buscarPorId(int idEmpleado)
            throws SQLException, IOException {

        exigirAdministrador();

        if (idEmpleado <= 0) {
            throw new IllegalArgumentException(
                "El identificador del usuario no es válido."
            );
        }

        return usuarioDAO.buscarPorId(idEmpleado);
    }

    // Actualizar rol y nombre de usuario.
    public boolean actualizarDatos(
            int idEmpleado,
            rolUsuario rol,
            String usuario)
            throws SQLException, IOException {

        exigirAdministrador();

        if (idEmpleado <= 0 || rol == null
                || usuario == null || usuario.isBlank()) {
            throw new IllegalArgumentException(
                "Los datos del usuario son inválidos."
            );
        }

        return usuarioDAO.actualizarDatos(
            idEmpleado,
            rol,
            usuario.trim()
        );
    }

    // Cambiar la contraseña de un usuario.
    public boolean cambiarContrasena(
            int idEmpleado,
            String nuevaContrasena)
            throws SQLException, IOException {

        exigirAdministrador();

        if (idEmpleado <= 0) {
            throw new IllegalArgumentException(
                "El identificador del usuario no es válido."
            );
        }

        validarContrasena(nuevaContrasena);

        String hash = PasswordUtil.generarHash(
            nuevaContrasena
        );

        return usuarioDAO.actualizarContrasena(
            idEmpleado,
            hash
        );
    }

    // Activar o desactivar una cuenta.
    public boolean cambiarEstado(
            int idEmpleado,
            boolean activo)
            throws SQLException, IOException {

        exigirAdministrador();

        if (idEmpleado <= 0) {
            throw new IllegalArgumentException(
                "El identificador del usuario no es válido."
            );
        }

        if (SesionUsuario.haySesionActiva()
                && SesionUsuario.obtenerIdEmpleado() == idEmpleado
                && !activo) {
            throw new IllegalArgumentException(
                "No puedes desactivar tu propia cuenta."
            );
        }

        return usuarioDAO.cambiarEstado(
            idEmpleado,
            activo
        );
    }

    // Validar que el usuario tenga permisos administrativos.
    private void exigirAdministrador() {

        if (!SesionUsuario.esAdministrador()) {
            throw new SecurityException(
                "Esta operación requiere permisos de administrador."
            );
        }
    }

    // Validar nombre de usuario y contraseña.
    private void validarCredenciales(
            String usuario,
            String contrasena) {

        if (usuario == null || usuario.isBlank()) {
            throw new IllegalArgumentException(
                "El nombre de usuario es obligatorio."
            );
        }

        validarContrasena(contrasena);
    }

    // Validar requisitos mínimos de contraseña.
    private void validarContrasena(String contrasena) {

        if (contrasena == null || contrasena.length() < 8) {
            throw new IllegalArgumentException(
                "La contraseña debe tener al menos 8 caracteres."
            );
        }

        if (contrasena.length() > 128) {
            throw new IllegalArgumentException(
                "La contraseña no puede superar 128 caracteres."
            );
        }
    }
}
