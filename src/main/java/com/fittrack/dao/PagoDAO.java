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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fittrack.model.PagoReporte;
import com.fittrack.model.tipoMembresia;

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

        try (Connection conexion = Conexion.conectar(); PreparedStatement ps = conexion.prepareStatement(sql)) {

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

        try (Connection conexion = Conexion.conectar(); PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, idMembresia);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    pagos.add(mapearPago(rs));
                }
            }
        }

        return pagos;
    }

    // Lista el historial de pagos de un socio.
    // Incluye los pagos de sus membresías individuales y compartidas.
    public List<Pago> listarPorSocio(int idSocio)
            throws SQLException, IOException {

        List<Pago> pagos = new ArrayList<>();

        String sql = """
            SELECT p.idPago, p.idMembresia, p.idEmpleado,
                   p.fechaPago, p.monto, p.metodoPago
            FROM pago p
            INNER JOIN sociomembresia sm
                ON p.idMembresia = sm.idMembresia
            WHERE sm.idSocio = ?
            ORDER BY p.fechaPago DESC, p.idPago DESC
            """;

        try (Connection conexion = Conexion.conectar(); PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, idSocio);

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

    // Calcular los ingresos totales dentro de un periodo.
    public BigDecimal calcularIngresos(
            LocalDate fechaInicio,
            LocalDate fechaFin)
            throws SQLException, IOException {

        String sql = """
            SELECT COALESCE(SUM(monto), 0) AS total
            FROM pago
            WHERE fechaPago BETWEEN ? AND ?
              AND monto IS NOT NULL
            """;

        try (Connection conexion = Conexion.conectar(); PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(fechaInicio));
            ps.setDate(2, Date.valueOf(fechaFin));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal("total");
                }
            }
        }

        return BigDecimal.ZERO;
    }

    // Obtener el desglose de ingresos por método de pago.
    public Map<String, BigDecimal> calcularIngresosPorMetodo(
            LocalDate fechaInicio,
            LocalDate fechaFin)
            throws SQLException, IOException {

        Map<String, BigDecimal> ingresos = new LinkedHashMap<>();

        String sql = """
            SELECT COALESCE(metodoPago, 'NO ESPECIFICADO') AS metodo,
                   SUM(monto) AS total
            FROM pago
            WHERE fechaPago BETWEEN ? AND ?
              AND monto IS NOT NULL
            GROUP BY metodoPago
            ORDER BY metodo
            """;

        try (Connection conexion = Conexion.conectar(); PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(fechaInicio));
            ps.setDate(2, Date.valueOf(fechaFin));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ingresos.put(
                            rs.getString("metodo"),
                            rs.getBigDecimal("total")
                    );
                }
            }
        }

        return ingresos;
    }

    // Consultar el detalle de pagos dentro de un periodo.
    public List<Pago> listarPorPeriodo(
            LocalDate fechaInicio,
            LocalDate fechaFin)
            throws SQLException, IOException {

        List<Pago> pagos = new ArrayList<>();

        String sql = """
            SELECT idPago, idMembresia, idEmpleado,
                   fechaPago, monto, metodoPago
            FROM pago
            WHERE fechaPago BETWEEN ? AND ?
              AND monto IS NOT NULL
            ORDER BY fechaPago DESC, idPago DESC
            """;

        try (Connection conexion = Conexion.conectar(); PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(fechaInicio));
            ps.setDate(2, Date.valueOf(fechaFin));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    pagos.add(mapearPago(rs));
                }
            }
        }

        return pagos;
    }

    // Cuenta las membresías vendidas por tipo dentro de un periodo.
    public Map<String, Integer> contarVentasPorTipo(
            LocalDate fechaInicio,
            LocalDate fechaFin)
            throws SQLException, IOException {

        Map<String, Integer> ventas = new LinkedHashMap<>();

        String sql = """
        SELECT COALESCE(m.tipo, 'NO ESPECIFICADO') AS tipo,
               COUNT(DISTINCT p.idPago) AS cantidad
        FROM pago p
        INNER JOIN membresia m
            ON p.idMembresia = m.idMembresia
        WHERE p.fechaPago BETWEEN ? AND ?
        GROUP BY m.tipo
        ORDER BY tipo
        """;

        try (Connection conexion = Conexion.conectar(); PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(fechaInicio));
            ps.setDate(2, Date.valueOf(fechaFin));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ventas.put(
                            rs.getString("tipo"),
                            rs.getInt("cantidad")
                    );
                }
            }
        }

        return ventas;
    }

// Calcula el importe vendido por tipo de membresía.
    public Map<String, BigDecimal> calcularImportesPorTipo(
            LocalDate fechaInicio,
            LocalDate fechaFin)
            throws SQLException, IOException {

        Map<String, BigDecimal> importes = new LinkedHashMap<>();

        String sql = """
        SELECT COALESCE(m.tipo, 'NO ESPECIFICADO') AS tipo,
               SUM(p.monto) AS total
        FROM pago p
        INNER JOIN membresia m
            ON p.idMembresia = m.idMembresia
        WHERE p.fechaPago BETWEEN ? AND ?
          AND p.monto IS NOT NULL
        GROUP BY m.tipo
        ORDER BY tipo
        """;

        try (Connection conexion = Conexion.conectar(); PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(fechaInicio));
            ps.setDate(2, Date.valueOf(fechaFin));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    importes.put(
                            rs.getString("tipo"),
                            rs.getBigDecimal("total")
                    );
                }
            }
        }

        return importes;
    }

// Cuenta el total de membresías vendidas durante un periodo.
    public int contarVentas(
            LocalDate fechaInicio,
            LocalDate fechaFin)
            throws SQLException, IOException {

        String sql = """
        SELECT COUNT(DISTINCT idPago) AS total
        FROM pago
        WHERE fechaPago BETWEEN ? AND ?
          AND idMembresia IS NOT NULL
        """;

        try (Connection conexion = Conexion.conectar(); PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(fechaInicio));
            ps.setDate(2, Date.valueOf(fechaFin));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
            }
        }

        return 0;
    }

    // Obtiene el detalle de pagos para el reporte RF-20.
    // Agrupa los socios de una membresía compartida sin duplicar el pago.
    public List<PagoReporte> listarDatosReporte(
            LocalDate fechaInicio,
            LocalDate fechaFin)
            throws SQLException, IOException {

        Map<Integer, DatosPagoReporte> pagosAgrupados = new LinkedHashMap<>();

        String sql = """
            SELECT
                p.idPago,
                m.idMembresia,
                m.tipo,
                p.fechaPago,
                p.monto,
                p.metodoPago,
                s.idSocio,
                CONCAT_WS(' ', s.nombre, s.apellido) AS nombreSocio
            FROM pago p
            INNER JOIN membresia m
                ON p.idMembresia = m.idMembresia
            INNER JOIN sociomembresia sm
                ON m.idMembresia = sm.idMembresia
            INNER JOIN socio s
                ON sm.idSocio = s.idSocio
            WHERE p.fechaPago BETWEEN ? AND ?
              AND p.monto IS NOT NULL
            ORDER BY p.fechaPago DESC, p.idPago DESC, s.idSocio
            """;

        try (Connection conexion = Conexion.conectar(); PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(fechaInicio));
            ps.setDate(2, Date.valueOf(fechaFin));

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    int idPago = rs.getInt("idPago");

                    DatosPagoReporte datos = pagosAgrupados.get(idPago);

                    if (datos == null) {

                        String tipo = rs.getString("tipo");

                        datos = new DatosPagoReporte(
                                idPago,
                                rs.getInt("idMembresia"),
                                tipo == null
                                        ? null
                                        : tipoMembresia.valueOf(tipo),
                                rs.getDate("fechaPago").toLocalDate(),
                                rs.getBigDecimal("monto"),
                                rs.getString("metodoPago")
                        );

                        pagosAgrupados.put(idPago, datos);
                    }

                    datos.idsSocios.add(rs.getInt("idSocio"));
                    datos.nombresSocios.add(
                            rs.getString("nombreSocio")
                    );
                }
            }
        }

        List<PagoReporte> resultado = new ArrayList<>();

        for (DatosPagoReporte datos : pagosAgrupados.values()) {

            resultado.add(new PagoReporte(
                    datos.idPago,
                    datos.idsSocios,
                    datos.nombresSocios,
                    datos.idMembresia,
                    datos.tipoMembresia,
                    datos.fechaPago,
                    datos.monto,
                    datos.metodoPago
            ));
        }

        return resultado;
    }

    // Estructura auxiliar para agrupar los socios de cada pago.
    private static class DatosPagoReporte {

        private final int idPago;
        private final int idMembresia;
        private final tipoMembresia tipoMembresia;
        private final LocalDate fechaPago;
        private final BigDecimal monto;
        private final String metodoPago;

        private final List<Integer> idsSocios = new ArrayList<>();
        private final List<String> nombresSocios = new ArrayList<>();

        private DatosPagoReporte(
                int idPago,
                int idMembresia,
                tipoMembresia tipoMembresia,
                LocalDate fechaPago,
                BigDecimal monto,
                String metodoPago) {

            this.idPago = idPago;
            this.idMembresia = idMembresia;
            this.tipoMembresia = tipoMembresia;
            this.fechaPago = fechaPago;
            this.monto = monto;
            this.metodoPago = metodoPago;
        }
    }

    // Lista todos los pagos registrados en el sistema.
    public List<Pago> listarTodos()
            throws SQLException, IOException {

        List<Pago> pagos = new ArrayList<>();

        String sql = """
        SELECT idPago, idMembresia, idEmpleado,
               fechaPago, monto, metodoPago
        FROM pago
        WHERE monto IS NOT NULL
        ORDER BY fechaPago DESC, idPago DESC
        """;

        try (Connection conexion = Conexion.conectar(); PreparedStatement ps = conexion.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                pagos.add(mapearPago(rs));
            }
        }

        return pagos;
    }

}
