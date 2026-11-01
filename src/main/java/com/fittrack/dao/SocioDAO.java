
package com.fittrack.dao;

import com.fittrack.database.Conexion;
import com.fittrack.model.Socio;
import com.fittrack.model.estadoSocio;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class SocioDAO {

    // Registrar un socio y devolver su identificador generado.
    public int registrar(String nombre, String apellido,
                         String correo, String telefono)
                         throws SQLException, java.io.IOException {

        String sql = """
            INSERT INTO socio
            (nombre, apellido, correo, telefono,
             fechaRegistro, estado)
            VALUES (?, ?, ?, ?, CURDATE(), 'ACTIVO')
            """;

        try (Connection conexion = Conexion.conectar();
             PreparedStatement ps = conexion.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, nombre);
            ps.setString(2, apellido);
            ps.setString(3, correo);
            ps.setString(4, telefono);

            ps.executeUpdate();

            try (ResultSet resultado = ps.getGeneratedKeys()) {
                if (resultado.next()) {
                    return resultado.getInt(1);
                }
            }
        }

        throw new SQLException(
            "No se pudo obtener el identificador del socio."
        );
    }

    // Buscar un socio por su identificador.
    public Socio buscarPorId(int idSocio)
            throws SQLException, java.io.IOException {

        String sql = "SELECT * FROM socio WHERE idSocio = ?";

        try (Connection conexion = Conexion.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, idSocio);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return convertirSocio(rs);
                }
            }
        }

        return null;
    }

    // Consultar todos los socios.
    public List<Socio> listarTodos()
            throws SQLException, java.io.IOException {

        List<Socio> socios = new ArrayList<>();

        String sql = "SELECT * FROM socio ORDER BY idSocio";

        try (Connection conexion = Conexion.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                socios.add(convertirSocio(rs));
            }
        }

        return socios;
    }

    // Actualizar los datos personales permitidos.
    public boolean actualizarDatos(int idSocio,
                                   String nombre,
                                   String apellido,
                                   String correo,
                                   String telefono)
            throws SQLException, java.io.IOException {

        String sql = """
            UPDATE socio
            SET nombre = ?,
                apellido = ?,
                correo = ?,
                telefono = ?
            WHERE idSocio = ?
            """;

        try (Connection conexion = Conexion.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, nombre);
            ps.setString(2, apellido);
            ps.setString(3, correo);
            ps.setString(4, telefono);
            ps.setInt(5, idSocio);

            return ps.executeUpdate() > 0;
        }
    }

    // Cambiar el estado de un socio.
    public boolean cambiarEstado(int idSocio, estadoSocio estado)
            throws SQLException, java.io.IOException {

        String sql = """
            UPDATE socio
            SET estado = ?
            WHERE idSocio = ?
            """;

        try (Connection conexion = Conexion.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, estado.name());
            ps.setInt(2, idSocio);

            return ps.executeUpdate() > 0;
        }
    }

    // Convertir un registro SQL en un objeto Socio.
    private Socio convertirSocio(ResultSet rs)
            throws SQLException {

        java.sql.Date fechaSQL = rs.getDate("fechaRegistro");

        java.time.LocalDate fechaRegistro =
                fechaSQL != null ? fechaSQL.toLocalDate() : null;

        estadoSocio estado = estadoSocio.valueOf(
                rs.getString("estado")
        );

        return new Socio(
            rs.getInt("idSocio"),
            rs.getString("nombre"),
            rs.getString("apellido"),
            rs.getString("correo"),
            rs.getString("telefono"),
            fechaRegistro,
            estado
        );
    }
}
