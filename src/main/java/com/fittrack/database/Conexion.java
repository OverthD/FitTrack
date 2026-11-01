
package com.fittrack.database;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class Conexion {

    private static final String ARCHIVO_CONFIGURACION =
            "/db.properties";

    public static Connection conectar()
            throws SQLException, IOException {

        Properties propiedades = new Properties();

        try (InputStream archivo =
                Conexion.class.getResourceAsStream(
                        ARCHIVO_CONFIGURACION)) {

            if (archivo == null) {
                throw new IOException(
                    "No se encontró el archivo db.properties."
                );
            }

            propiedades.load(archivo);
        }

        String url = propiedades.getProperty("db.url");
        String usuario = propiedades.getProperty("db.user");
        String contrasena = propiedades.getProperty("db.password");

        return DriverManager.getConnection(
                url, usuario, contrasena
        );
    }
}
