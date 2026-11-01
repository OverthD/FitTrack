package com.fittrack.dao;

import com.fittrack.database.Conexion;
import com.fittrack.model.Membresia;
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

    public boolean actualizarEstado(int idMembresia,
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
            throws SQLException, java.io.IOException {

        List<Integer> idsSocios = new ArrayList<>();

        String sql = """
        SELECT idSocio
        FROM sociomembresia
        WHERE idMembresia = ?
        ORDER BY idSocio
        """;

        try (Connection conexion = com.fittrack.database.Conexion.conectar(); PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, idMembresia);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    idsSocios.add(rs.getInt("idSocio"));
                }
            }
        }

        return idsSocios;
    }

}
