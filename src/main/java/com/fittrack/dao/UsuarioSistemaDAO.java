package com.fittrack.dao;

import com.fittrack.database.Conexion;
import com.fittrack.model.UsuarioSistema;
import com.fittrack.model.Empleado;
import com.fittrack.model.Administrador;
import com.fittrack.model.rolUsuario;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class UsuarioSistemaDAO {

    // Registrar un usuario y devolver su identificador generado.
    public int registrar(UsuarioSistema usuario)
            throws SQLException, IOException {

        String sql = """
            INSERT INTO usuariosistema
            (rol, usuario, `contraseña`, activo)
            VALUES (?, ?, ?, ?)
            """;

        try (Connection conexion = Conexion.conectar(); PreparedStatement ps = conexion.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, usuario.getRol().name());
            ps.setString(2, usuario.getUsuario());
            ps.setString(3, usuario.getContrasenaHash());
            ps.setBoolean(4, usuario.isActivo());

            ps.executeUpdate();

            try (ResultSet resultado = ps.getGeneratedKeys()) {
                if (resultado.next()) {
                    int idGenerado = resultado.getInt(1);
                    usuario.setIdEmpleado(idGenerado);
                    return idGenerado;
                }
            }
        }

        throw new SQLException(
                "No se pudo obtener el identificador del usuario."
        );
    }

    // Buscar un usuario por su identificador.
    public UsuarioSistema buscarPorId(int idEmpleado)
            throws SQLException, IOException {

        String sql = """
            SELECT *
            FROM usuariosistema
            WHERE idEmpleado = ?
            """;

        try (Connection conexion = Conexion.conectar(); PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, idEmpleado);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return convertirUsuario(rs);
                }
            }
        }

        return null;
    }

    // Buscar un usuario por su nombre de usuario.
    public UsuarioSistema buscarPorUsuario(String usuario)
            throws SQLException, IOException {

        String sql = """
            SELECT *
            FROM usuariosistema
            WHERE usuario = ?
            """;

        try (Connection conexion = Conexion.conectar(); PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, usuario);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return convertirUsuario(rs);
                }
            }
        }

        return null;
    }

    // Consultar todos los usuarios.
    public List<UsuarioSistema> listarTodos()
            throws SQLException, IOException {

        List<UsuarioSistema> usuarios = new ArrayList<>();

        String sql = """
            SELECT *
            FROM usuariosistema
            ORDER BY idEmpleado
            """;

        try (Connection conexion = Conexion.conectar(); PreparedStatement ps = conexion.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                usuarios.add(convertirUsuario(rs));
            }
        }

        return usuarios;
    }

    // Actualizar rol y nombre de usuario.
    public boolean actualizarDatos(int idEmpleado,
            rolUsuario rol,
            String usuario)
            throws SQLException, IOException {

        String sql = """
            UPDATE usuariosistema
            SET rol = ?,
                usuario = ?
            WHERE idEmpleado = ?
            """;

        try (Connection conexion = Conexion.conectar(); PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, rol.name());
            ps.setString(2, usuario);
            ps.setInt(3, idEmpleado);

            return ps.executeUpdate() > 0;
        }
    }

    // Actualizar el hash de la contraseña.
    public boolean actualizarContrasena(int idEmpleado,
            String contrasenaHash)
            throws SQLException, IOException {

        String sql = """
            UPDATE usuariosistema
            SET `contraseña` = ?
            WHERE idEmpleado = ?
            """;

        try (Connection conexion = Conexion.conectar(); PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, contrasenaHash);
            ps.setInt(2, idEmpleado);

            return ps.executeUpdate() > 0;
        }
    }

    // Cambiar el estado de una cuenta.
    public boolean cambiarEstado(int idEmpleado, boolean activo)
            throws SQLException, IOException {

        String sql = """
            UPDATE usuariosistema
            SET activo = ?
            WHERE idEmpleado = ?
            """;

        try (Connection conexion = Conexion.conectar(); PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setBoolean(1, activo);
            ps.setInt(2, idEmpleado);

            return ps.executeUpdate() > 0;
        }
    }

    // Convertir un registro SQL en su clase correspondiente.
    private UsuarioSistema convertirUsuario(ResultSet rs)
            throws SQLException {

        int idEmpleado = rs.getInt("idEmpleado");

        rolUsuario rol = rolUsuario.valueOf(
                rs.getString("rol")
        );

        String usuario = rs.getString("usuario");

        String contrasenaHash = rs.getString("contraseña");

        boolean activo = rs.getBoolean("activo");

        switch (rol) {

            case ADMINISTRADOR:
                return new Administrador(
                        idEmpleado,
                        usuario,
                        contrasenaHash,
                        activo
                );

            case EMPLEADO:
                return new Empleado(
                        idEmpleado,
                        usuario,
                        contrasenaHash,
                        activo
                );

            default:
                throw new SQLException(
                        "El rol del usuario no es válido."
                );
        }
    }

    // Contar administradores que tienen su cuenta activa.
    public int contarAdministradoresActivos()
            throws SQLException, IOException {

        String sql = """
        SELECT COUNT(*)
        FROM usuariosistema
        WHERE rol = 'ADMINISTRADOR'
          AND activo = TRUE
        """;

        try (Connection conexion = Conexion.conectar(); PreparedStatement ps = conexion.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        }

        return 0;
    }
}
