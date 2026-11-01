package com.fittrack.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SocioMembresiaDAO {

    // Asociar un socio con una membresía.
    public void asociar(Connection conexion,
            int idSocio,
            int idMembresia) throws SQLException {

        String sql = """
            INSERT INTO sociomembresia
            (idSocio, idMembresia)
            VALUES (?, ?)
            """;

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, idSocio);
            ps.setInt(2, idMembresia);

            ps.executeUpdate();
        }
    }

    // Consultar los identificadores de las membresías de un socio.
    public List<Integer> listarIdsMembresiasPorSocio(int idSocio)
            throws SQLException, java.io.IOException {

        List<Integer> idsMembresias = new ArrayList<>();

        String sql = """
            SELECT idMembresia
            FROM sociomembresia
            WHERE idSocio = ?
            """;

        try (Connection conexion = com.fittrack.database.Conexion.conectar(); PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, idSocio);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    idsMembresias.add(rs.getInt("idMembresia"));
                }
            }
        }

        return idsMembresias;
    }

    // Consultar los socios asociados a una membresía.
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
