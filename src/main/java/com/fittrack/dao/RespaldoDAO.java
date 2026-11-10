
package com.fittrack.dao;

import com.fittrack.database.Conexion;
import com.fittrack.model.Respaldo;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

public class RespaldoDAO {

    // Registrar la generación de un respaldo.
    public Respaldo registrar(
            LocalDate fecha,
            int idAdministrador)
            throws SQLException, IOException {

        String sql = """
            INSERT INTO respaldo (
                idAdministrador,
                fecha
            )
            VALUES (?, ?)
            """;

        try (Connection conexion = Conexion.conectar();
             PreparedStatement ps = conexion.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, idAdministrador);
            ps.setDate(2, Date.valueOf(fecha));

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return new Respaldo(
                            rs.getInt(1),
                            fecha
                    );
                }
            }
        }

        throw new SQLException(
                "No se pudo registrar la generación del respaldo."
        );
    }
}
