
package com.fittrack.dao;

import com.fittrack.database.Conexion;
import com.fittrack.model.Pago;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class PagoDAO {

    // Registra un pago utilizando una conexión existente.
    // Se utiliza cuando el pago forma parte de una transacción.
    public int registrar(Connection conexion, Pago pago)
            throws SQLException {

        String sql = """
            INSERT INTO pago (
                idMembresia,
                idEmpleado,
                fechaPago,
                monto,
                metodoPago
            )
            VALUES (?, ?, ?, ?, ?)
            """;

        try (PreparedStatement ps = conexion.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, pago.getIdMembresia());

            if (pago.getIdEmpleado() != null) {
                ps.setInt(2, pago.getIdEmpleado());
            } else {
                ps.setNull(2, Types.INTEGER);
            }

            ps.setDate(3, Date.valueOf(pago.getFechaPago()));
            ps.setBigDecimal(4, pago.getMonto());
            ps.setString(5, pago.getMetodoPago());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int idGenerado = rs.getInt(1);
                    pago.setIdPago(idGenerado);
                    return idGenerado;
                }
            }
        }

        throw new SQLException(
            "No se pudo obtener el identificador del pago registrado."
        );
    }

    // Registra un pago utilizando una conexión propia.
    public int registrar(Pago pago)
            throws SQLException, IOException {

        try (Connection conexion = Conexion.conectar()) {
            return registrar(conexion, pago);
        }
    }

    // Consulta un pago mediante su identificador.
    public Pago buscarPorId(int idPago)
            throws SQLException, IOException {

        String sql = """
            SELECT idPago, idMembresia, idEmpleado,
                   fechaPago, monto, metodoPago
            FROM pago
            WHERE idPago = ?
            """;

        try (Connection conexion = Conexion.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, idPago);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearPago(rs);
                }
            }
        }

        return null;
    }

    // Lista todos los pagos asociados a una membresía.
    public List<Pago> listarPorMembresia(int idMembresia)
            throws SQLException, IOException {

        List<Pago> pagos = new ArrayList<>();

        String sql = """
            SELECT idPago, idMembresia, idEmpleado,
                   fechaPago, monto, metodoPago
            FROM pago
            WHERE idMembresia = ?
            ORDER BY fechaPago DESC, idPago DESC
            """;

        try (Connection conexion = Conexion.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, idMembresia);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    pagos.add(mapearPago(rs));
                }
            }
        }

        return pagos;
    }

    // Convierte un registro de la tabla pago en un objeto Pago.
    private Pago mapearPago(ResultSet rs) throws SQLException {

        int idEmpleado = rs.getInt("idEmpleado");
        Integer empleado = rs.wasNull() ? null : idEmpleado;

        Pago pago = new Pago(
            rs.getInt("idMembresia"),
            empleado,
            rs.getDate("fechaPago").toLocalDate(),
            rs.getBigDecimal("monto"),
            rs.getString("metodoPago")
        );

        pago.setIdPago(rs.getInt("idPago"));

        return pago;
    }
}
