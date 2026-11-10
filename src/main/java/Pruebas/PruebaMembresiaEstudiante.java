
package Pruebas;

import com.fittrack.dao.AcreditacionEstudianteDAO;
import com.fittrack.dao.PagoDAO;
import com.fittrack.dao.SocioMembresiaDAO;
import com.fittrack.database.Conexion;
import com.fittrack.model.tipoMembresia;
import com.fittrack.service.AcreditacionEstudianteService;
import com.fittrack.service.MembresiaService;
import com.fittrack.service.UsuarioSistemaService;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.time.LocalDate;

public class PruebaMembresiaEstudiante {

    public static void main(String[] args) throws Exception {

        UsuarioSistemaService usuarioService =
                new UsuarioSistemaService();

        AcreditacionEstudianteService acreditacionService =
                new AcreditacionEstudianteService();

        AcreditacionEstudianteDAO acreditacionDAO =
                new AcreditacionEstudianteDAO();

        MembresiaService membresiaService =
                new MembresiaService();

        SocioMembresiaDAO socioMembresiaDAO =
                new SocioMembresiaDAO();

        PagoDAO pagoDAO = new PagoDAO();

        LocalDate hoy = LocalDate.now();

        int idAcreditacionSocio6 = 0;

        System.out.println("=== PRUEBAS DE MEMBRESIAS ESTUDIANTILES ===");

        try {

            // =====================================================
            // PRUEBA 1: INICIO DE SESION
            // =====================================================

            System.out.println("\nPRUEBA 1: INICIO DE SESION");

            usuarioService.iniciarSesion(
                    "admin_prueba",
                    "FitTrack2026!"
            );

            System.out.println("Sesion iniciada correctamente.");


            // =====================================================
            // PRUEBA 2: REGISTRAR ACREDITACION VENCIDA
            // SOCIO 12
            // =====================================================

            System.out.println(
                    "\nPRUEBA 2: REGISTRAR ACREDITACION VENCIDA"
            );

            int idAcreditacionVencida =
                    acreditacionService.registrarAcreditacion(
                            12,
                            hoy.minusMonths(3),
                            hoy.minusDays(1),
                            "Constancia de estudios"
                    );

            System.out.println(
                    "Acreditacion historica registrada. ID: "
                    + idAcreditacionVencida
            );


            // =====================================================
            // PRUEBA 3: REGISTRAR ACREDITACION VIGENTE
            // SOCIO 10
            // =====================================================

            System.out.println(
                    "\nPRUEBA 3: REGISTRAR ACREDITACION VIGENTE"
            );

            int idAcreditacionSocio10 =
                    acreditacionService.registrarAcreditacion(
                            10,
                            hoy,
                            hoy.plusMonths(3),
                            "Credencial escolar"
                    );

            System.out.println(
                    "Acreditacion registrada para socio 10. ID: "
                    + idAcreditacionSocio10
            );


            // =====================================================
            // PRUEBA 4: REGISTRO ESTUDIANTIL VALIDO
            // SOCIO 10
            // =====================================================

            System.out.println(
                    "\nPRUEBA 4: REGISTRO ESTUDIANTIL VALIDO"
            );

            int idMembresiaEstudiante =
                    membresiaService.registrarMembresia(
                            tipoMembresia.ESTUDIANTE,
                            hoy.plusDays(1),
                            new BigDecimal("500.00"),
                            10,
                            null,
                            "Efectivo"
                    );

            System.out.println(
                    "Membresia estudiantil registrada. ID: "
                    + idMembresiaEstudiante
            );

            System.out.println(
                    "Socio asociado: "
                    + socioMembresiaDAO.listarIdsSociosPorMembresia(
                            idMembresiaEstudiante
                    )
            );

            System.out.println(
                    "Pagos registrados: "
                    + pagoDAO.listarPorMembresia(
                            idMembresiaEstudiante
                    ).size()
            );


            // =====================================================
            // PRUEBA 5: RECHAZAR REGISTRO SIN ACREDITACION
            // SOCIO 11
            // =====================================================

            System.out.println(
                    "\nPRUEBA 5: RECHAZAR REGISTRO SIN ACREDITACION"
            );

            try {

                membresiaService.registrarMembresia(
                        tipoMembresia.ESTUDIANTE,
                        hoy.plusDays(1),
                        new BigDecimal("500.00"),
                        11,
                        null,
                        "Efectivo"
                );

                throw new IllegalStateException(
                        "ERROR: Se permitio registrar una membresia "
                        + "sin acreditacion."
                );

            } catch (IllegalArgumentException e) {

                if (!e.getMessage().contains(
                        "no cuenta con una acreditación estudiantil vigente")) {
                    throw e;
                }

                System.out.println(
                        "Correcto: se rechazo el registro por falta "
                        + "de acreditacion vigente."
                );
            }


            // =====================================================
            // PRUEBA 6: RECHAZAR REGISTRO CON ACREDITACION VENCIDA
            // SOCIO 12
            // =====================================================

            System.out.println(
                    "\nPRUEBA 6: RECHAZAR REGISTRO CON ACREDITACION VENCIDA"
            );

            try {

                membresiaService.registrarMembresia(
                        tipoMembresia.ESTUDIANTE,
                        hoy.plusDays(1),
                        new BigDecimal("500.00"),
                        12,
                        null,
                        "Efectivo"
                );

                throw new IllegalStateException(
                        "ERROR: Se permitio registrar una membresia "
                        + "con acreditacion vencida."
                );

            } catch (IllegalArgumentException e) {

                if (!e.getMessage().contains(
                        "no cuenta con una acreditación estudiantil vigente")) {
                    throw e;
                }

                System.out.println(
                        "Correcto: se rechazo el registro por "
                        + "acreditacion vencida."
                );
            }


            // =====================================================
            // PRUEBA 7: ACREDITACION VIGENTE PARA SOCIO 6
            // =====================================================

            System.out.println(
                    "\nPRUEBA 7: ACREDITACION VIGENTE PARA RENOVACION"
            );

            idAcreditacionSocio6 =
                    acreditacionService.registrarAcreditacion(
                            6,
                            hoy,
                            hoy.plusMonths(3),
                            "Constancia de estudios"
                    );

            System.out.println(
                    "Acreditacion registrada para socio 6. ID: "
                    + idAcreditacionSocio6
            );


            // =====================================================
            // PRUEBA 8: RENOVACION ESTUDIANTIL VALIDA
            // SOCIO 6 - MEMBRESIA ORIGINAL 6
            // =====================================================

            System.out.println(
                    "\nPRUEBA 8: RENOVACION ESTUDIANTIL VALIDA"
            );

            int idMembresiaRenovada =
                    membresiaService.renovarMembresia(
                            6,
                            tipoMembresia.ESTUDIANTE,
                            new BigDecimal("500.00"),
                            "Efectivo"
                    );

            System.out.println(
                    "Renovacion estudiantil registrada. ID: "
                    + idMembresiaRenovada
            );

            System.out.println(
                    "Socios asociados: "
                    + socioMembresiaDAO.listarIdsSociosPorMembresia(
                            idMembresiaRenovada
                    )
            );

            System.out.println(
                    "Pagos registrados: "
                    + pagoDAO.listarPorMembresia(
                            idMembresiaRenovada
                    ).size()
            );


            // =====================================================
            // PRUEBA 9: VERIFICAR QUE EL REGISTRO NO SEA COMPARTIDO
            // =====================================================

            System.out.println(
                    "\nPRUEBA 9: VERIFICAR MEMBRESIAS INDIVIDUALES"
            );

            if (socioMembresiaDAO.listarIdsSociosPorMembresia(
                    idMembresiaEstudiante
            ).size() != 1) {

                throw new IllegalStateException(
                        "ERROR: La membresia estudiantil de socio 10 "
                        + "no es individual."
                );
            }

            if (socioMembresiaDAO.listarIdsSociosPorMembresia(
                    idMembresiaRenovada
            ).size() != 1) {

                throw new IllegalStateException(
                        "ERROR: La renovacion estudiantil de socio 6 "
                        + "no es individual."
                );
            }

            System.out.println(
                    "Correcto: ambas membresias estudiantiles "
                    + "tienen un solo socio asociado."
            );


            // =====================================================
            // PRUEBA 10: VERIFICAR PAGOS
            // =====================================================

            System.out.println(
                    "\nPRUEBA 10: VERIFICAR PAGOS"
            );

            if (pagoDAO.listarPorMembresia(
                    idMembresiaEstudiante
            ).isEmpty()) {

                throw new IllegalStateException(
                        "ERROR: No se registro el pago de la membresia."
                );
            }

            if (pagoDAO.listarPorMembresia(
                    idMembresiaRenovada
            ).isEmpty()) {

                throw new IllegalStateException(
                        "ERROR: No se registro el pago de la renovacion."
                );
            }

            System.out.println(
                    "Correcto: ambas operaciones registraron su pago."
            );


            // =====================================================
            // PRUEBA 11: RECHAZAR RENOVACION CON ACREDITACION VENCIDA
            // SOCIO 6 - MEMBRESIA ORIGINAL 6
            // =====================================================

            System.out.println(
                    "\nPRUEBA 11: RECHAZAR RENOVACION CON "
                    + "ACREDITACION VENCIDA"
            );

            try {

                actualizarVigenciaAcreditacion(
                        idAcreditacionSocio6,
                        hoy.minusDays(1)
                );

                try {

                    membresiaService.renovarMembresia(
                            6,
                            tipoMembresia.ESTUDIANTE,
                            new BigDecimal("500.00"),
                            "Efectivo"
                    );

                    throw new IllegalStateException(
                            "ERROR: Se permitio renovar una membresia "
                            + "sin acreditacion vigente."
                    );

                } catch (IllegalArgumentException e) {

                    if (!e.getMessage().contains(
                            "no cuenta con una acreditación estudiantil vigente")) {
                        throw e;
                    }

                    System.out.println(
                            "Correcto: se rechazo la renovacion "
                            + "por acreditacion vencida."
                    );

                } finally {

                    actualizarVigenciaAcreditacion(
                            idAcreditacionSocio6,
                            hoy.plusMonths(3)
                    );

                    System.out.println(
                            "Vigencia original de la acreditacion "
                            + "restaurada."
                    );
                }

            } finally {

                // Garantiza la restauracion incluso si falla
                // alguna parte de la prueba 11.
                actualizarVigenciaAcreditacion(
                        idAcreditacionSocio6,
                        hoy.plusMonths(3)
                );
            }


            // =====================================================
            // RESULTADO FINAL
            // =====================================================

            System.out.println(
                    "\n=========================================="
            );

            System.out.println(
                    "TODAS LAS PRUEBAS FINALIZARON CORRECTAMENTE."
            );

            System.out.println(
                    "=========================================="
            );

        } finally {

            usuarioService.cerrarSesion();
        }
    }


    // =============================================================
    // METODO AUXILIAR PARA MODIFICAR TEMPORALMENTE LA VIGENCIA
    // =============================================================

    private static void actualizarVigenciaAcreditacion(
            int idAcreditacion,
            LocalDate nuevaFecha
    ) throws Exception {

        String sql = """
            UPDATE acreditacionestudiante
            SET fechaVigencia = ?
            WHERE idAcreditacion = ?
        """;

        try (Connection conexion = Conexion.conectar();
             PreparedStatement sentencia =
                     conexion.prepareStatement(sql)) {

            sentencia.setDate(
                    1,
                    Date.valueOf(nuevaFecha)
            );

            sentencia.setInt(
                    2,
                    idAcreditacion
            );

            int filas = sentencia.executeUpdate();

            if (filas != 1) {
                throw new IllegalStateException(
                        "No se pudo actualizar la acreditacion "
                        + idAcreditacion
                );
            }
        }
    }
}
