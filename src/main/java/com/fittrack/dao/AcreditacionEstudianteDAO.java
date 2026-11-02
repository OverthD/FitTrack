
package com.fittrack.dao;

import com.fittrack.database.Conexion;
import com.fittrack.model.AcreditacionEstudiante;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class AcreditacionEstudianteDAO {

    // Registrar una nueva acreditación.
    public int registrar(AcreditacionEstudiante acreditacion)
            throws SQLException, IOException {

        if (acreditacion == null) {
            throw new IllegalArgumentException(
                    "La acreditación no puede ser nula."
            );
        }

        String sql = """
            INSERT INTO acreditacionestudiante
            (idSocio, fechaVerificacion, fechaVigencia, tipoComprobante)
            VALUES (?, ?, ?, ?)
            """;

        try (Connection conexion = Conexion.conectar();
             PreparedStatement sentencia = conexion.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            sentencia.setInt(1, acreditacion.getIdSocio());
            sentencia.setDate(
                    2, Date.valueOf(acreditacion.getFechaVerificacion())
            );
            sentencia.setDate(
                    3, Date.valueOf(acreditacion.getFechaVigencia())
            );
            sentencia.setString(
                    4, acreditacion.getTipoComprobante()
            );

            int filas = sentencia.executeUpdate();

            if (filas == 0) {
                throw new SQLException(
                        "No se pudo registrar la acreditación."
                );
            }

            try (ResultSet resultado = sentencia.getGeneratedKeys()) {

                if (resultado.next()) {
                    return resultado.getInt(1);
                }

                throw new SQLException(
                        "No se obtuvo el identificador de la acreditación."
                );
            }
        }
    }

    // Buscar una acreditación por su identificador.
    public AcreditacionEstudiante buscarPorId(int idAcreditacion)
            throws SQLException, IOException {

        String sql = """
            SELECT *
            FROM acreditacionestudiante
            WHERE idAcreditacion = ?
            """;

        try (Connection conexion = Conexion.conectar();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setInt(1, idAcreditacion);

            try (ResultSet resultado = sentencia.executeQuery()) {

                if (resultado.next()) {
                    return convertirAcreditacion(resultado);
                }
            }
        }

        return null;
    }

    // Listar todas las acreditaciones de un socio.
    public List<AcreditacionEstudiante> listarPorSocio(int idSocio)
            throws SQLException, IOException {

        List<AcreditacionEstudiante> acreditaciones = new ArrayList<>();

        String sql = """
            SELECT *
            FROM acreditacionestudiante
            WHERE idSocio = ?
            ORDER BY fechaVerificacion DESC, idAcreditacion DESC
            """;

        try (Connection conexion = Conexion.conectar();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setInt(1, idSocio);

            try (ResultSet resultado = sentencia.executeQuery()) {

                while (resultado.next()) {
                    acreditaciones.add(
                            convertirAcreditacion(resultado)
                    );
                }
            }
        }

        return acreditaciones;
    }

    // Buscar la acreditación vigente más reciente de un socio.
    public AcreditacionEstudiante buscarVigentePorSocio(int idSocio)
            throws SQLException, IOException {

        String sql = """
            SELECT *
            FROM acreditacionestudiante
            WHERE idSocio = ?
            AND fechaVigencia >= CURRENT_DATE
            ORDER BY fechaVigencia DESC,
                     fechaVerificacion DESC,
                     idAcreditacion DESC
            LIMIT 1
            """;

        try (Connection conexion = Conexion.conectar();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setInt(1, idSocio);

            try (ResultSet resultado = sentencia.executeQuery()) {

                if (resultado.next()) {
                    return convertirAcreditacion(resultado);
                }
            }
        }

        return null;
    }

    // Convertir un registro de MySQL en un objeto.
    private AcreditacionEstudiante convertirAcreditacion(ResultSet rs)
            throws SQLException {

        int idAcreditacion = rs.getInt("idAcreditacion");
        int idSocio = rs.getInt("idSocio");

        java.time.LocalDate fechaVerificacion =
                rs.getDate("fechaVerificacion").toLocalDate();

        java.time.LocalDate fechaVigencia =
                rs.getDate("fechaVigencia").toLocalDate();

        String tipoComprobante = rs.getString("tipoComprobante");

        return new AcreditacionEstudiante(
                idAcreditacion,
                idSocio,
                fechaVerificacion,
                fechaVigencia,
                tipoComprobante
        );
    }
}
