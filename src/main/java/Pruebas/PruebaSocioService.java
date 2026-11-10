package Pruebas;

import com.fittrack.model.Socio;
import com.fittrack.model.estadoSocio;
import com.fittrack.service.SocioService;
import com.fittrack.service.SocioService;
import java.util.List;

public class PruebaSocioService {

    public static void probar() throws Exception {

        SocioService servicio = new SocioService();

        // 1. Registrar socio
        System.out.println("\n--- REGISTRO DE SOCIO ---");

        int id = servicio.registrarSocio(
                "Prueba",
                "Service",
                "prueba.service@example.com",
                "1234567890"
        );

        System.out.println("Socio registrado. ID: " + id);

        // 2. Consultar socio
        System.out.println("\n--- CONSULTA DE SOCIO ---");

        Socio socio = servicio.consultarSocio(id);

        if (socio != null) {
            System.out.println("ID: " + socio.getIdSocio());
            System.out.println("Nombre: " + socio.getNombre());
            System.out.println("Apellido: " + socio.getApellido());
            System.out.println("Correo: " + socio.getCorreo());
            System.out.println("Estado: " + socio.getEstado());
        }

        // 3. Actualizar datos
        System.out.println("\n--- ACTUALIZACIÓN DE DATOS ---");

        boolean actualizado = servicio.actualizarSocio(
                id,
                "Socio",
                "Modificado",
                "socio.modificado@example.com",
                "0987654321"
        );

        System.out.println("Actualización exitosa: " + actualizado);

        // 4. Cambiar estado
        System.out.println("\n--- CAMBIO DE ESTADO ---");

        boolean cambiado = servicio.cambiarEstadoSocio(
                id,
                estadoSocio.INACTIVO
        );

        System.out.println("Cambio de estado exitoso: " + cambiado);

        // 5. Consultar nuevamente
        System.out.println("\n--- VERIFICACIÓN FINAL ---");

        socio = servicio.consultarSocio(id);

        System.out.println("Nombre actualizado: "
                + socio.getNombre() + " " + socio.getApellido());

        System.out.println("Correo actualizado: " + socio.getCorreo());
        System.out.println("Estado actualizado: " + socio.getEstado());

        // 6. Listar socios
        System.out.println("\n--- LISTADO DE SOCIOS ---");

        List<Socio> socios = servicio.listarSocios();

        System.out.println("Total de socios: " + socios.size());

        System.out.println("\nPRUEBAS DE SOCIOS FINALIZADAS");
    }
}