
package Pruebas;

import com.fittrack.service.MembresiaService;
import com.fittrack.database.Conexion;
import com.fittrack.model.Membresia;
import com.fittrack.model.tipoMembresia;
import com.fittrack.model.estadoMembresia;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;

public class PruebaRenovacionMembresia {

    @FunctionalInterface
    interface Operacion {
        void ejecutar() throws Exception;
    }

    public static void main(String[] args) {

        MembresiaService servicio = new MembresiaService();

        try {
            System.out.println(
                    "=== PREPARACIÓN: MEMBRESÍA VENCIDA ==="
            );

            // El socio 9 no tiene membresías asociadas.
            int idVencida = servicio.registrarMembresia(
                    tipoMembresia.MENSUAL,
                    LocalDate.of(2026, 8, 1),
                    new BigDecimal("850.00"),
                    9,
                    null,
                    "Efectivo"
            );

            Membresia vencida =
                    servicio.consultarMembresia(idVencida);

            System.out.println(
                    "Membresía creada: " + idVencida
            );

            System.out.println(
                    "Vencimiento: " + vencida.getFechaVencimiento()
            );

            System.out.println(
                    "Estado: " + vencida.getEstado()
            );

            if (vencida.getEstado()
                    != estadoMembresia.VENCIDA) {

                throw new AssertionError(
                        "La membresía de prueba no está vencida."
                );
            }

            System.out.println(
                    "Preparación correcta.\n"
            );

            // PRUEBA 1: Intentar renovar una membresía vencida.
            verificarRechazo(
                    "PRUEBA 1: RENOVACIÓN DE MEMBRESÍA VENCIDA",
                    () -> servicio.renovarMembresia(
                            idVencida,
                            tipoMembresia.MENSUAL,
                            new BigDecimal("850.00"),
                            "Efectivo"
                    )
            );

            // PRUEBA 2: Convertir una membresía compartida
            // en una individual.
            verificarRechazo(
                    "PRUEBA 2: CAMBIO DE COMPARTIDA A INDIVIDUAL",
                    () -> servicio.renovarMembresia(
                            5,
                            tipoMembresia.MENSUAL,
                            new BigDecimal("850.00"),
                            "Efectivo"
                    )
            );

            // PRUEBA 3: Renovar una membresía inexistente.
            verificarRechazo(
                    "PRUEBA 3: MEMBRESÍA INEXISTENTE",
                    () -> servicio.renovarMembresia(
                            9999,
                            tipoMembresia.MENSUAL,
                            new BigDecimal("850.00"),
                            "Efectivo"
                    )
            );

            System.out.println(
                    "\n=== VALIDACIONES NEGATIVAS FINALIZADAS ==="
            );

        } catch (Exception e) {
            System.out.println(
                    "ERROR DURANTE LAS PRUEBAS:"
            );
            e.printStackTrace();
        }
    }

    private static void verificarRechazo(
            String nombre,
            Operacion operacion) throws Exception {

        System.out.println("\n=== " + nombre + " ===");

        long[] registrosAntes = contarRegistros();

        boolean rechazada = false;

        try {
            operacion.ejecutar();

        } catch (IllegalArgumentException e) {

            rechazada = true;

            System.out.println(
                    "Operación rechazada correctamente: "
                    + e.getMessage()
            );
        }

        if (!rechazada) {
            throw new AssertionError(
                    "ERROR: El sistema permitió una operación inválida."
            );
        }

        long[] registrosDespues = contarRegistros();

        if (!Arrays.equals(registrosAntes, registrosDespues)) {
            throw new AssertionError(
                    "ERROR: Se modificaron registros "
                    + "durante una operación rechazada."
            );
        }

        System.out.println(
                "Correcto: No se generaron registros adicionales."
        );

        System.out.println(
                "PRUEBA CORRECTA."
        );
    }

    private static long[] contarRegistros() throws Exception {

        String[] tablas = {
            "membresia",
            "sociomembresia",
            "pago"
        };

        long[] cantidades = new long[tablas.length];

        try (Connection conexion = Conexion.conectar();
             Statement st = conexion.createStatement()) {

            for (int i = 0; i < tablas.length; i++) {

                String sql =
                        "SELECT COUNT(*) FROM " + tablas[i];

                try (ResultSet rs = st.executeQuery(sql)) {

                    if (rs.next()) {
                        cantidades[i] = rs.getLong(1);
                    }
                }
            }
        }

        return cantidades;
    }
}
