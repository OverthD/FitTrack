package com.fittrack.dao;

import com.fittrack.database.Conexion;
import com.fittrack.model.Membresia;
import com.fittrack.model.MembresiaReporte;
import com.fittrack.model.tipoMembresia;
import com.fittrack.model.estadoMembresia;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MembresiaDAO {

    public int registrar(Membresia membresia)
            throws SQLException, IOException {

        try (Connection conexion = Conexion.conectar()) {
            return registrar(conexion, membresia);
        }
    }

    public int registrar(Connection conexion, Membresia membresia)
            throws SQLException {

        String sql = """
        INSERT INTO membresia
        (tipo, fechaInicio, fechaVencimiento, precio, estado)
        VALUES (?, ?, ?, ?, ?)
        """;

        try (PreparedStatement ps = conexion.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, membresia.getTipo().name());
            ps.setDate(2, Date.valueOf(membresia.getFechaInicio()));
            ps.setDate(3, Date.valueOf(membresia.getFechaVencimiento()));
            ps.setBigDecimal(4, membresia.getPrecio());
            ps.setString(5, membresia.getEstado().name());

            ps.executeUpdate();

            try (ResultSet resultado = ps.getGeneratedKeys()) {
                if (resultado.next()) {
                    return resultado.getInt(1);
                }
            }
        }

        throw new SQLException(
                "No se pudo obtener el identificador de la membresía."
        );
    }

    public Membresia buscarPorId(int idMembresia)
            throws SQLException, IOException {

        String sql = """
            SELECT * FROM membresia
            WHERE idMembresia = ?
            """;

        try (Connection conexion = Conexion.conectar(); PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, idMembresia);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return convertirMembresia(rs);
                }
            }
        }

        return null;
    }

    public List<Membresia> listarTodas()
            throws SQLException, IOException {

        List<Membresia> membresias = new ArrayList<>();

        String sql = """
            SELECT * FROM membresia
            ORDER BY idMembresia
            """;

        try (Connection conexion = Conexion.conectar(); PreparedStatement ps = conexion.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                membresias.add(convertirMembresia(rs));
            }
        }

        return membresias;
    }

    // Consultar membresías próximas a vencer dentro de un periodo.
    public List<Membresia> listarProximasAVencer(
            LocalDate fechaConsulta,
            int dias)
            throws SQLException, IOException {

        List<Membresia> membresias = new ArrayList<>();

        String sql = """
            SELECT *
            FROM membresia
            WHERE fechaInicio <= ?
              AND fechaVencimiento >= ?
              AND fechaVencimiento <= ?
            ORDER BY fechaVencimiento ASC
            """;

        LocalDate fechaLimite = fechaConsulta.plusDays(dias);

        try (Connection conexion = Conexion.conectar(); PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(fechaConsulta));
            ps.setDate(2, Date.valueOf(fechaConsulta));
            ps.setDate(3, Date.valueOf(fechaLimite));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    membresias.add(convertirMembresia(rs));
                }
            }
        }

        return membresias;
    }

    public boolean actualizarEstado(
            int idMembresia,
            estadoMembresia estado)
            throws SQLException, IOException {

        String sql = """
            UPDATE membresia
            SET estado = ?
            WHERE idMembresia = ?
            """;

        try (Connection conexion = Conexion.conectar(); PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, estado.name());
            ps.setInt(2, idMembresia);

            return ps.executeUpdate() > 0;
        }
    }

    private Membresia convertirMembresia(ResultSet rs)
            throws SQLException {

        Date inicioSQL = rs.getDate("fechaInicio");
        Date vencimientoSQL = rs.getDate("fechaVencimiento");

        return new Membresia(
                rs.getInt("idMembresia"),
                tipoMembresia.valueOf(rs.getString("tipo")),
                inicioSQL != null ? inicioSQL.toLocalDate() : null,
                vencimientoSQL != null ? vencimientoSQL.toLocalDate() : null,
                rs.getBigDecimal("precio"),
                estadoMembresia.valueOf(rs.getString("estado"))
        );
    }

    public List<Integer> listarIdsSociosPorMembresia(int idMembresia)
            throws SQLException, IOException {

        List<Integer> idsSocios = new ArrayList<>();

        String sql = """
        SELECT idSocio
        FROM sociomembresia
        WHERE idMembresia = ?
        ORDER BY idSocio
        """;

        try (Connection conexion = Conexion.conectar(); PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, idMembresia);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    idsSocios.add(rs.getInt("idSocio"));
                }
            }
        }

        return idsSocios;
    }

    // Consultar la membresía más reciente de un socio,
// incluyendo todos los socios asociados a esa membresía.
    public List<MembresiaReporte> consultarMasRecientePorSocio(int idSocio)
            throws SQLException, IOException {

        List<MembresiaReporte> resultado = new ArrayList<>();

        String sql = """
        SELECT
            s.idSocio,
            s.nombre,
            s.apellido,
            m.idMembresia,
            m.tipo,
            m.fechaInicio,
            m.fechaVencimiento,
            m.precio,
            m.estado
        FROM membresia m
        INNER JOIN sociomembresia sm
            ON m.idMembresia = sm.idMembresia
        INNER JOIN socio s
            ON sm.idSocio = s.idSocio
        WHERE m.idMembresia = (
            SELECT sm2.idMembresia
            FROM sociomembresia sm2
            INNER JOIN membresia m2
                ON sm2.idMembresia = m2.idMembresia
            WHERE sm2.idSocio = ?
            ORDER BY m2.fechaInicio DESC, m2.idMembresia DESC
            LIMIT 1
        )
        ORDER BY s.idSocio
        """;

        try (Connection conexion = Conexion.conectar(); PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, idSocio);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    Date inicioSQL = rs.getDate("fechaInicio");
                    Date vencimientoSQL = rs.getDate("fechaVencimiento");

                    LocalDate fechaInicio = inicioSQL != null
                            ? inicioSQL.toLocalDate() : null;

                    LocalDate fechaVencimiento = vencimientoSQL != null
                            ? vencimientoSQL.toLocalDate() : null;

                    resultado.add(new MembresiaReporte(
                            rs.getInt("idSocio"),
                            rs.getString("nombre"),
                            rs.getString("apellido"),
                            rs.getInt("idMembresia"),
                            tipoMembresia.valueOf(rs.getString("tipo")),
                            fechaInicio,
                            fechaVencimiento,
                            rs.getBigDecimal("precio"),
                            estadoMembresia.valueOf(rs.getString("estado"))
                    ));
                }
            }
        }

        return resultado;
    }

    // Obtener los datos necesarios para el reporte de membresías.
    public List<MembresiaReporte> listarDatosReporte()
            throws SQLException, IOException {

        List<MembresiaReporte> reporte = new ArrayList<>();

        String sql = """
            SELECT
                s.idSocio,
                s.nombre,
                s.apellido,
                m.idMembresia,
                m.tipo,
                m.fechaInicio,
                m.fechaVencimiento,
                m.precio,
                m.estado
            FROM socio s
            INNER JOIN sociomembresia sm
                ON s.idSocio = sm.idSocio
            INNER JOIN membresia m
                ON sm.idMembresia = m.idMembresia
            ORDER BY m.idMembresia, s.idSocio
            """;

        try (Connection conexion = Conexion.conectar(); PreparedStatement ps = conexion.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                int idSocio = rs.getInt("idSocio");
                String nombre = rs.getString("nombre");
                String apellido = rs.getString("apellido");

                int idMembresia = rs.getInt("idMembresia");

                tipoMembresia tipo = tipoMembresia.valueOf(
                        rs.getString("tipo")
                );

                Date inicioSQL = rs.getDate("fechaInicio");
                Date vencimientoSQL = rs.getDate("fechaVencimiento");

                LocalDate fechaInicio = inicioSQL != null
                        ? inicioSQL.toLocalDate() : null;

                LocalDate fechaVencimiento = vencimientoSQL != null
                        ? vencimientoSQL.toLocalDate() : null;

                BigDecimal precio = rs.getBigDecimal("precio");

                estadoMembresia estadoPersistido
                        = estadoMembresia.valueOf(rs.getString("estado"));

                // Calcular el estado actual para el reporte.
                estadoMembresia estadoActual = estadoPersistido;

                if (fechaInicio != null && fechaVencimiento != null) {

                    Membresia membresia = new Membresia(
                            idMembresia,
                            tipo,
                            fechaInicio,
                            fechaVencimiento,
                            precio,
                            estadoPersistido
                    );

                    estadoActual = membresia.calcularEstado(LocalDate.now());
                }

                reporte.add(new MembresiaReporte(
                        idSocio,
                        nombre,
                        apellido,
                        idMembresia,
                        tipo,
                        fechaInicio,
                        fechaVencimiento,
                        precio,
                        estadoActual
                ));
            }
        }

        return reporte;
    }

    // Consultar los datos del reporte de membresías por periodo y estado.
    public List<MembresiaReporte> listarDatosReporte(
            LocalDate desde,
            LocalDate hasta,
            estadoMembresia estadoFiltro)
            throws SQLException, IOException {

        if (desde == null || hasta == null) {
            throw new IllegalArgumentException(
                    "Debe especificar las fechas del reporte."
            );
        }

        if (desde.isAfter(hasta)) {
            throw new IllegalArgumentException(
                    "La fecha inicial no puede ser posterior a la fecha final."
            );
        }

        List<MembresiaReporte> reporteCompleto = listarDatosReporte();
        List<MembresiaReporte> resultado = new ArrayList<>();

        for (MembresiaReporte membresia : reporteCompleto) {

            LocalDate fechaInicio = membresia.getFechaInicio();

            // Filtrar por fecha de inicio.
            if (fechaInicio == null
                    || fechaInicio.isBefore(desde)
                    || fechaInicio.isAfter(hasta)) {
                continue;
            }

            // Si se seleccionó un estado, aplicar el filtro.
            if (estadoFiltro != null
                    && membresia.getEstado() != estadoFiltro) {
                continue;
            }

            resultado.add(membresia);
        }

        return resultado;
    }

    // Verificar si una membresía corresponde a una renovación.
    public boolean tieneMembresiaAnterior(int idMembresia)
            throws SQLException, IOException {

        String sql = """
        SELECT EXISTS (
            SELECT 1
            FROM sociomembresia actual
            INNER JOIN membresia membresiaActual
                ON actual.idMembresia = membresiaActual.idMembresia
            INNER JOIN sociomembresia anterior
                ON anterior.idSocio = actual.idSocio
            INNER JOIN membresia membresiaAnterior
                ON anterior.idMembresia = membresiaAnterior.idMembresia
            WHERE actual.idMembresia = ?
              AND (
                  membresiaAnterior.fechaInicio < membresiaActual.fechaInicio
                  OR (
                      membresiaAnterior.fechaInicio = membresiaActual.fechaInicio
                      AND membresiaAnterior.idMembresia < membresiaActual.idMembresia
                  )
              )
        ) AS esRenovacion
        """;

        try (Connection conexion = Conexion.conectar(); PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, idMembresia);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean("esRenovacion");
                }
            }
        }

        return false;
    }
}
