
package Pruebas;

import com.fittrack.dao.SocioDAO;
import com.fittrack.model.Socio;

public class PruebaSocioDAO {

    public static void probar() {

        SocioDAO socioDAO = new SocioDAO();

        try {
            // 1. Registrar un socio de prueba.
            int idGenerado = socioDAO.registrar(
                "Socio",
                "Prueba",
                "prueba.fittrack@example.com",
                "0000000000"
            );

            System.out.println("SOCIO REGISTRADO");
            System.out.println("ID generado: " + idGenerado);

            // 2. Buscar el socio registrado.
            Socio socio = socioDAO.buscarPorId(idGenerado);

            if (socio != null) {
                System.out.println("\nSOCIO ENCONTRADO");
                System.out.println("ID: " + socio.getIdSocio());
                System.out.println("Nombre: " + socio.getNombre());
                System.out.println("Apellido: " + socio.getApellido());
                System.out.println("Correo: " + socio.getCorreo());
                System.out.println("Teléfono: " + socio.getTelefono());
                System.out.println("Estado: " + socio.getEstado());
                System.out.println("Fecha: " + socio.getFechaRegistro());
            } else {
                System.out.println("No se encontró el socio.");
            }

            // 3. Consultar todos los socios.
            System.out.println("\nTOTAL DE SOCIOS: "
                    + socioDAO.listarTodos().size());

        } catch (Exception e) {
            System.out.println("ERROR EN LA PRUEBA DEL DAO:");
            e.printStackTrace();
        }
    }
}
