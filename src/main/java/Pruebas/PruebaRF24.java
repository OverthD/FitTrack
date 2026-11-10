
package Pruebas;

import com.fittrack.model.UsuarioSistema;
import com.fittrack.service.RespaldoService;
import com.fittrack.service.UsuarioSistemaService;
import com.fittrack.util.SesionUsuario;

import java.nio.file.Files;
import java.nio.file.Path;

public class PruebaRF24 {

    public static void main(String[] args) {

        System.out.println("===== DIAGNOSTICO RF-24 =====");

        try {

            RespaldoService respaldoService =
                    new RespaldoService();

            // PRUEBA 1: Intentar generar respaldo sin sesión.
            System.out.println("\n--- PRUEBA 1: SIN SESION ---");

            try {
                Path destino = Path.of(
                        System.getProperty("user.home"),
                        "Documents",
                        "FitTrackRespaldos"
                );

                Files.createDirectories(destino);

                respaldoService.generarRespaldo(destino);

                System.out.println(
                        "ERROR: Se permitió generar un respaldo sin sesión."
                );

            } catch (SecurityException e) {
                System.out.println(
                        "Correcto: se rechazó la operación sin sesión."
                );
                System.out.println(e.getMessage());
            }

            // PRUEBA 2: Iniciar sesión administrativa.
            System.out.println("\n--- PRUEBA 2: SESION ADMINISTRATIVA ---");

            UsuarioSistemaService usuarioService =
                    new UsuarioSistemaService();

            // Sustituir por las credenciales administrativas locales.
            UsuarioSistema usuario = usuarioService.iniciarSesion(
                    "admin_prueba",
                    "FitTrack2026!"
            );

            if (usuario == null) {
                System.out.println(
                        "ERROR: No se pudo iniciar sesión."
                );
                return;
            }

            if (!SesionUsuario.esAdministrador()) {
                System.out.println(
                        "ERROR: La cuenta no tiene rol de administrador."
                );
                return;
            }

            System.out.println("Sesión administrativa iniciada.");
            System.out.println(
                    "ID administrador: "
                    + SesionUsuario.obtenerIdEmpleado()
            );

            // PRUEBA 3: Generar respaldo.
            System.out.println("\n--- PRUEBA 3: GENERAR RESPALDO ---");

            Path destino = Path.of(
                    System.getProperty("user.home"),
                    "Documentos",
                    "FitTrackRespaldos"
            );

            Files.createDirectories(destino);

            Path archivo = respaldoService.generarRespaldo(destino);

            System.out.println("Respaldo generado:");
            System.out.println(archivo.toAbsolutePath());

            // PRUEBA 4: Verificar el archivo.
            System.out.println("\n--- PRUEBA 4: VERIFICAR ARCHIVO ---");

            System.out.println(
                    "Existe: " + Files.exists(archivo)
            );

            System.out.println(
                    "Tamaño: " + Files.size(archivo) + " bytes"
            );

            System.out.println(
                    "Extensión SQL: "
                    + archivo.getFileName().toString().endsWith(".sql")
            );

            String contenido = Files.readString(archivo);

            System.out.println(
                    "Contiene CREATE TABLE: "
                    + contenido.contains("CREATE TABLE")
            );

            System.out.println(
                    "Contiene INSERT INTO: "
                    + contenido.contains("INSERT INTO")
            );

            // PRUEBA 5: Finalizar.
            System.out.println("\n--- RESULTADO ---");
            System.out.println("Pruebas de generación finalizadas.");

        } catch (Exception e) {

            System.out.println(
                    "\nERROR DURANTE LA PRUEBA:"
            );

            e.printStackTrace();

        } finally {

            SesionUsuario.cerrar();

            System.out.println("\nSesión cerrada.");
            System.out.println("==============================");
        }
    }
}
