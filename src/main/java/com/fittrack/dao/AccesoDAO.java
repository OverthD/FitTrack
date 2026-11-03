package com.fittrack.dao;

import com.fittrack.model.Acceso;
import com.fittrack.model.AccesoReporte;
import java.io.IOException;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;

import java.util.ArrayList;
import java.util.List;

public class AccesoDAO {

    // Registrar un nuevo acceso.
    public int registrar(Connection conexion, Acceso acceso)
            throws SQLException {

        String sql = """
            INSERT INTO acceso (
                idSocio,
                idEmpleado,
                fecha,
                horaEntrada,
                horaSalida
            )
            VALUES (?, ?, ?, ?, ?)
        """;

        try (PreparedStatement sentencia
                = conexion.prepareStatement(
                        sql,
                        Statement.RETURN_GENERATED_KEYS
                )) {

                    sentencia.setInt(1, acceso.getIdSocio());

                    if (acceso.getIdEmpleado() == null) {
                        sentencia.setNull(2, java.sql.Types.INTEGER);
                    } else {
                        sentencia.setInt(2, acceso.getIdEmpleado());
                    }

                    sentencia.setDate(
                            3,
                            Date.valueOf(acceso.getFecha())
                    );

                    sentencia.setTime(
                            4,
                            Time.valueOf(acceso.getHoraEntrada())
                    );

                    if (acceso.getHoraSalida() == null) {
                        sentencia.setNull(5, java.sql.Types.TIME);
                    } else {
                        sentencia.setTime(
                                5,
                                Time.valueOf(acceso.getHoraSalida())
                        );
                    }

                    sentencia.executeUpdate();

                    try (ResultSet resultado
                            = sentencia.getGeneratedKeys()) {

                        if (resultado.next()) {
                            return resultado.getInt(1);
                        }

                        throw new SQLException(
                                "No se pudo obtener el identificador del acceso."
                        );
                    }
                }
    }

    // Buscar un acceso por su identificador.
    public Acceso buscarPorId(int idAcceso) throws SQLException, IOException {

        String sql = """
            SELECT *
            FROM acceso
            WHERE idAcceso = ?
        """;

        try (Connection conexion
                = com.fittrack.database.Conexion.conectar(); PreparedStatement sentencia
                = conexion.prepareStatement(sql)) {

            sentencia.setInt(1, idAcceso);

            try (ResultSet resultado = sentencia.executeQuery()) {

                if (resultado.next()) {
                    return mapearAcceso(resultado);
                }

                return null;
            }
        }
    }

    // Listar todos los accesos de un socio.
    public List<Acceso> listarPorSocio(int idSocio)
            throws SQLException, IOException {

        String sql = """
            SELECT *
            FROM acceso
            WHERE idSocio = ?
            ORDER BY fecha DESC, horaEntrada DESC
        """;

        List<Acceso> accesos = new ArrayList<>();

        try (Connection conexion
                = com.fittrack.database.Conexion.conectar(); PreparedStatement sentencia
                = conexion.prepareStatement(sql)) {

            sentencia.setInt(1, idSocio);

            try (ResultSet resultado = sentencia.executeQuery()) {

                while (resultado.next()) {
                    accesos.add(mapearAcceso(resultado));
                }
            }
        }

        return accesos;
    }

    // Buscar el acceso que todavía no tiene salida registrada.
    public Acceso buscarAccesoAbiertoPorSocio(int idSocio)
            throws SQLException, IOException {

        String sql = """
            SELECT *
            FROM acceso
            WHERE idSocio = ?
              AND horaSalida IS NULL
            ORDER BY fecha DESC, horaEntrada DESC
            LIMIT 1
        """;

        try (Connection conexion
                = com.fittrack.database.Conexion.conectar(); PreparedStatement sentencia
                = conexion.prepareStatement(sql)) {

            sentencia.setInt(1, idSocio);

            try (ResultSet resultado = sentencia.executeQuery()) {

                if (resultado.next()) {
                    return mapearAcceso(resultado);
                }

                return null;
            }
        }
    }

    // Registrar la hora de salida.
    public boolean registrarSalida(
            int idAcceso,
            Time horaSalida
    ) throws SQLException, IOException {

        String sql = """
            UPDATE acceso
            SET horaSalida = ?
            WHERE idAcceso = ?
              AND horaSalida IS NULL
        """;

        try (Connection conexion
                = com.fittrack.database.Conexion.conectar(); PreparedStatement sentencia
                = conexion.prepareStatement(sql)) {

            sentencia.setTime(1, horaSalida);
            sentencia.setInt(2, idAcceso);

            return sentencia.executeUpdate() == 1;
        }
    }

    // Convertir un registro SQL en un objeto Acceso.
    private Acceso mapearAcceso(ResultSet resultado)
            throws SQLException {

        Date fecha = resultado.getDate("fecha");
        Time horaEntrada = resultado.getTime("horaEntrada");
        Time horaSalida = resultado.getTime("horaSalida");

        int idEmpleadoResultado
                = resultado.getInt("idEmpleado");

        Integer idEmpleado
                = resultado.wasNull()
                ? null
                : idEmpleadoResultado;

        return new Acceso(
                resultado.getInt("idAcceso"),
                resultado.getInt("idSocio"),
                idEmpleado,
                fecha.toLocalDate(),
                horaEntrada.toLocalTime(),
                horaSalida == null
                        ? null
                        : horaSalida.toLocalTime()
        );
    }

    // Obtener los datos de accesos para el reporte RF-21.
    public List<AccesoReporte> listarDatosReporte(
            java.time.LocalDate fechaInicio,
            java.time.LocalDate fechaFin)
            throws SQLException, IOException {

        List<AccesoReporte> accesos = new ArrayList<>();

        String sql = """
            SELECT
                s.idSocio,
                CONCAT_WS(' ', s.nombre, s.apellido) AS nombreSocio,
                a.fecha,
                a.horaEntrada,
                a.horaSalida
            FROM acceso a
            INNER JOIN socio s
                ON a.idSocio = s.idSocio
            WHERE a.fecha BETWEEN ? AND ?
            ORDER BY a.fecha DESC, a.horaEntrada DESC
            """;

        try (Connection conexion
                = com.fittrack.database.Conexion.conectar(); PreparedStatement sentencia
                = conexion.prepareStatement(sql)) {

            sentencia.setDate(1, Date.valueOf(fechaInicio));
            sentencia.setDate(2, Date.valueOf(fechaFin));

            try (ResultSet resultado = sentencia.executeQuery()) {

                while (resultado.next()) {

                    Date fecha = resultado.getDate("fecha");
                    Time horaEntrada
                            = resultado.getTime("horaEntrada");
                    Time horaSalida
                            = resultado.getTime("horaSalida");

                    accesos.add(new AccesoReporte(
                            resultado.getInt("idSocio"),
                            resultado.getString("nombreSocio"),
                            fecha.toLocalDate(),
                            horaEntrada.toLocalTime(),
                            horaSalida == null
                                    ? null
                                    : horaSalida.toLocalTime()
                    ));
                }
            }
        }

        return accesos;
    }

}
