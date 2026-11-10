
package Pruebas;

import com.fittrack.dao.AcreditacionEstudianteDAO;
import com.fittrack.model.AcreditacionEstudiante;
import com.fittrack.service.AcreditacionEstudianteService;
import com.fittrack.service.UsuarioSistemaService;
import com.fittrack.util.SesionUsuario;

import java.time.LocalDate;
import java.util.List;

public class PruebaAcreditacionEstudiante {

    public static void main(String[] args) throws Exception {

        UsuarioSistemaService usuarioService =
                new UsuarioSistemaService();

        AcreditacionEstudianteService acreditacionService =
                new AcreditacionEstudianteService();

        AcreditacionEstudianteDAO acreditacionDAO =
                new AcreditacionEstudianteDAO();

        int idSocio = 1;

        try {

            System.out.println(
                    "=== PRUEBAS DEL MODULO DE ACREDITACION ESTUDIANTIL ==="
            );

            // 1. Iniciar sesión.
            System.out.println("\nPRUEBA 1: INICIO DE SESION");

            usuarioService.iniciarSesion(
                    "admin_prueba",
                    "FitTrack2026!"
            );

            if (!SesionUsuario.haySesionActiva()) {
                throw new IllegalStateException(
                        "No se pudo iniciar sesión."
                );
            }

            System.out.println("Sesion iniciada correctamente.");

            // 2. Registrar una credencial escolar.
            System.out.println("\nPRUEBA 2: REGISTRAR CREDENCIAL ESCOLAR");

            int idCredencial = acreditacionService.registrarAcreditacion(
                    idSocio,
                    LocalDate.now(),
                    LocalDate.now().plusMonths(3),
                    "Credencial escolar"
            );

            System.out.println(
                    "Credencial registrada. ID: " + idCredencial
            );

            // 3. Registrar una constancia independiente.
            System.out.println("\nPRUEBA 3: REGISTRAR CONSTANCIA DE ESTUDIOS");

            int idConstancia = acreditacionService.registrarAcreditacion(
                    idSocio,
                    LocalDate.now(),
                    LocalDate.now().plusMonths(6),
                    "Constancia de estudios"
            );

            System.out.println(
                    "Constancia registrada. ID: " + idConstancia
            );

            if (idCredencial == idConstancia) {
                throw new IllegalStateException(
                        "Los registros no tienen identificadores diferentes."
                );
            }

            // 4. Consultar una acreditación.
            System.out.println("\nPRUEBA 4: CONSULTAR POR ID");

            AcreditacionEstudiante acreditacion =
                    acreditacionService.buscarPorId(idCredencial);

            if (acreditacion == null) {
                throw new IllegalStateException(
                        "No se encontró la acreditación."
                );
            }

            System.out.println(
                    "ID: " + acreditacion.getIdAcreditacion()
            );
            System.out.println(
                    "Socio: " + acreditacion.getIdSocio()
            );
            System.out.println(
                    "Comprobante: " + acreditacion.getTipoComprobante()
            );
            System.out.println(
                    "Verificación: " + acreditacion.getFechaVerificacion()
            );
            System.out.println(
                    "Vigencia: " + acreditacion.getFechaVigencia()
            );

            // 5. Consultar historial.
            System.out.println("\nPRUEBA 5: HISTORIAL DE ACREDITACIONES");

            List<AcreditacionEstudiante> historial =
                    acreditacionService.listarPorSocio(idSocio);

            for (AcreditacionEstudiante a : historial) {
                System.out.println(
                        "ID: " + a.getIdAcreditacion()
                        + " | Comprobante: " + a.getTipoComprobante()
                        + " | Vigencia: " + a.getFechaVigencia()
                );
            }

            if (historial.size() < 2) {
                throw new IllegalStateException(
                        "No se conservaron ambos registros."
                );
            }

            // 6. Consultar acreditación vigente.
            System.out.println(
                    "\nPRUEBA 6: CONSULTAR ACREDITACION VIGENTE"
            );

            AcreditacionEstudiante vigente =
                    acreditacionService.consultarAcreditacionVigente(
                            idSocio
                    );

            if (vigente == null) {
                throw new IllegalStateException(
                        "No se encontró una acreditación vigente."
                );
            }

            System.out.println(
                    "Acreditación vigente: "
                    + vigente.getIdAcreditacion()
            );

            // 7. Rechazar fecha de verificación futura.
            System.out.println(
                    "\nPRUEBA 7: FECHA DE VERIFICACION FUTURA"
            );

            try {
                acreditacionService.registrarAcreditacion(
                        idSocio,
                        LocalDate.now().plusDays(1),
                        LocalDate.now().plusMonths(3),
                        "Credencial escolar"
                );

                throw new IllegalStateException(
                        "Se aceptó incorrectamente una fecha futura."
                );

            } catch (IllegalArgumentException e) {
                System.out.println(
                        "Fecha futura rechazada correctamente."
                );
            }

            // 8. Rechazar vigencia anterior a la verificación.
            System.out.println(
                    "\nPRUEBA 8: FECHA DE VIGENCIA INVALIDA"
            );

            try {
                acreditacionService.registrarAcreditacion(
                        idSocio,
                        LocalDate.now(),
                        LocalDate.now().minusDays(1),
                        "Constancia de estudios"
                );

                throw new IllegalStateException(
                        "Se aceptó incorrectamente una vigencia inválida."
                );

            } catch (IllegalArgumentException e) {
                System.out.println(
                        "Vigencia inválida rechazada correctamente."
                );
            }

            // 9. Rechazar socio inexistente.
            System.out.println("\nPRUEBA 9: SOCIO INEXISTENTE");

            try {
                acreditacionService.registrarAcreditacion(
                        999999,
                        LocalDate.now(),
                        LocalDate.now().plusMonths(3),
                        "Credencial escolar"
                );

                throw new IllegalStateException(
                        "Se aceptó un socio inexistente."
                );

            } catch (IllegalArgumentException e) {
                System.out.println(
                        "Socio inexistente rechazado correctamente."
                );
            }

            System.out.println(
                    "\n=== TODAS LAS PRUEBAS FINALIZARON CORRECTAMENTE ==="
            );

        } finally {
            usuarioService.cerrarSesion();
        }
    }
}
