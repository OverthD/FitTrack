
package com.fittrack.database;

import java.sql.Connection;

public class PruebaConexion {

    public static void probar() {

        try (Connection conexion = Conexion.conectar()) {

            if (conexion != null && conexion.isValid(2)) {
                System.out.println(
                    "CONEXION EXITOSA CON MYSQL"
                );
                System.out.println(
                    "Base de datos: "
                    + conexion.getCatalog()
                );
            }

        } catch (Exception e) {
            System.out.println(
                "ERROR AL CONECTAR CON MYSQL:"
            );
            e.printStackTrace();
        }
    }
}
