package Pruebas;

import com.fittrack.service.SocioService;
import com.fittrack.service.UsuarioSistemaService;
import com.fittrack.util.SesionUsuario;

public class PruebaRF22 {

    public static void main(String[] args) {

        System.out.println("===== DIAGNOSTICO RF-22 =====");

        UsuarioSistemaService usuarioService
                = new UsuarioSistemaService();

        SocioService socioService = new SocioService();

        try {
            // Iniciar sesión administrativa.
            var usuario = usuarioService.iniciarSesion(
                    "admin_prueba",
                    "FitTrack2026!"
            );

            if (usuario == null) {
                System.out.println("No se pudo iniciar sesión.");
                return;
            }

            System.out.println("Sesión iniciada correctamente.");

            // PRUEBA 1: Identificar un socio existente.
            System.out.println("\n--- PRUEBA 1: ID EXISTENTE ---");

            boolean existente = socioService.identificarSocio(1);

            System.out.println("ID 1 identificado: " + existente);

            if (!existente) {
                throw new AssertionError(
                        "No se identificó un socio existente."
                );
            }

            System.out.println("Resultado: CORRECTO");

            // PRUEBA 2: Identificar un socio inexistente.
            System.out.println("\n--- PRUEBA 2: ID INEXISTENTE ---");

            boolean inexistente
                    = socioService.identificarSocio(999999);

            System.out.println(
                    "ID 999999 identificado: " + inexistente
            );

            if (inexistente) {
                throw new AssertionError(
                        "Se identificó un socio inexistente."
                );
            }

            System.out.println("Resultado: CORRECTO");

            // PRUEBA 3: Identificadores inválidos.
            System.out.println("\n--- PRUEBA 3: ID INVALIDOS ---");

            probarIdInvalido(socioService, 0);
            probarIdInvalido(socioService, -1);

            System.out.println("Resultado: CORRECTO");

            // PRUEBA 4: Identificación sin sesión.
            System.out.println("\n--- PRUEBA 4: SIN SESION ---");

            usuarioService.cerrarSesion();

            try {
                socioService.identificarSocio(1);

                throw new AssertionError(
                        "Se permitió identificar sin sesión."
                );

            } catch (IllegalStateException e) {
                System.out.println(
                        "Identificación sin sesión: RECHAZADA"
                );
            }

            System.out.println("Resultado: CORRECTO");

            //System.out.println("Resultado: CORRECTO");

            System.out.println(
                    "\nTODAS LAS PRUEBAS COMPLETADAS"
            );

        } catch (Exception e) {
            System.err.println(
                    "ERROR EN LA PRUEBA: " + e.getMessage()
            );
            e.printStackTrace();

        } finally {
            if (SesionUsuario.haySesionActiva()) {
                usuarioService.cerrarSesion();
            }

            System.out.println("\nDiagnóstico finalizado.");
        }
    }

    private static void probarIdInvalido(
            SocioService socioService, int id) throws Exception {

        try {
            socioService.identificarSocio(id);

            throw new AssertionError(
                    "Se aceptó un identificador inválido: " + id
            );

        } catch (IllegalArgumentException e) {
            System.out.println(
                    "ID " + id + ": RECHAZADO"
            );
        }
    }
}
