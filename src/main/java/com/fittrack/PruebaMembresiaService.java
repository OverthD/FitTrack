package com.fittrack;

import com.fittrack.service.MembresiaService;
import com.fittrack.service.SocioService;
import com.fittrack.dao.PagoDAO;
import com.fittrack.model.Membresia;
import com.fittrack.model.Pago;
import com.fittrack.model.tipoMembresia;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
public class PruebaMembresiaService {

    public static void main(String[] args) {

        MembresiaService servicio = new MembresiaService();
        SocioService socioService = new SocioService();
        PagoDAO pagoDAO = new PagoDAO();

        System.out.println("=== PRUEBA 6: MEMBRESÍA EN TOLERANCIA Y REGISTRO DE PAGO ===");

        try {
            int socioTolerancia = socioService.registrarSocio(
                    "Prueba",
                    "Tolerancia",
                    "tolerancia.20261101@example.com",
                    "5555555555"
            );

            int idMembresia = servicio.registrarMembresia(
                    tipoMembresia.MENSUAL,
                    LocalDate.of(2026, 9, 30),
                    new BigDecimal("850.00"),
                    socioTolerancia,
                    null,
                    "Efectivo"
            );

            System.out.println(
                    "Membresía registrada. ID: " + idMembresia
            );

            // Consultar la membresía registrada.
            Membresia membresia
                    = servicio.consultarMembresia(idMembresia);

            System.out.println(
                    "Estado de la membresía: " + membresia.getEstado()
            );

            System.out.println(
                    "Precio de la membresía: $" + membresia.getPrecio()
            );

            // Consultar el pago asociado.
            List<Pago> pagos
                    = pagoDAO.listarPorMembresia(idMembresia);

            System.out.println("\n=== PAGO ASOCIADO ===");

            for (Pago pago : pagos) {
                System.out.println("ID del pago: " + pago.getIdPago());
                System.out.println("Fecha: " + pago.getFechaPago());
                System.out.println("Monto: $" + pago.getMonto());
                System.out.println("Método: " + pago.getMetodoPago());
            }

            // Comprobar que exista exactamente un pago y coincida el importe.
            if (pagos.size() == 1
                    && pagos.get(0).getMonto().compareTo(
                            membresia.getPrecio()) == 0) {

                System.out.println(
                        "\nPRUEBA CORRECTA: El pago está asociado "
                        + "y coincide con el precio de la membresía."
                );

            } else {
                System.out.println(
                        "\nERROR: El pago no coincide con lo esperado."
                );
            }

        } catch (Exception e) {
            System.out.println("ERROR DURANTE LA PRUEBA:");
            e.printStackTrace();
        }

        /*System.out.println("=== PRUEBA 5: SOCIO OCUPADO EN MEMBRESÍA COMPARTIDA ===");

        try {
            int socioNuevo = socioService.registrarSocio(
                    "Prueba",
                    "Nuevo",
                    "nuevo.20261101@example.com",
                    "4444444444"
            );

            servicio.registrarMembresia(
                    tipoMembresia.DOSPERSONAS,
                    LocalDate.now(),
                    new BigDecimal("1200.00"),
                    socioNuevo,
                    6
            );

            System.out.println("ERROR: Se permitió registrar la membresía compartida.");

        } catch (Exception e) {
            System.out.println("Validación correcta: " + e.getMessage());
        }*/
 /*try {
            // Crear tres socios nuevos
            int socioA = socioService.registrarSocio(
                    "Prueba", "Individual", "individual.20261101@example.com", "1111111111"
            );

            int socioB = socioService.registrarSocio(
                    "Prueba", "Compartido A", "compartidoA.20261101@example.com", "2222222222"
            );

            int socioC = socioService.registrarSocio(
                    "Prueba", "Compartido B", "compartidoB.20261101@example.com", "3333333333"
            );

            System.out.println("Socios creados: " + socioA + ", " + socioB + ", " + socioC);

            // Prueba 3: Membresía individual
            System.out.println("=== PRUEBA 3: REGISTRO INDIVIDUAL ===");

            int idIndividual = servicio.registrarMembresia(
                    tipoMembresia.MENSUAL,
                    LocalDate.now(),
                    new BigDecimal("850.00"),
                    socioA,
                    null
            );

            System.out.println("Membresía individual registrada. ID: " + idIndividual);

            // Prueba 4: Membresía compartida
            System.out.println("=== PRUEBA 4: REGISTRO COMPARTIDO ===");

            int idCompartida = servicio.registrarMembresia(
                    tipoMembresia.DOSPERSONAS,
                    LocalDate.now(),
                    new BigDecimal("1200.00"),
                    socioB,
                    socioC
            );

            System.out.println("Membresía compartida registrada. ID: " + idCompartida);

        } catch (Exception e) {
            e.printStackTrace();
        }*/
        //try {
        // PRUEBA 1: Membresía individual.
        /*System.out.println("=== PRUEBA 1: MEMBRESÍA INDIVIDUAL ===");

            int idIndividual = servicio.registrarMembresia(
                    tipoMembresia.MENSUAL,
                    LocalDate.now(),
                    new BigDecimal("850.00"),
                    1,
                    null
            );

            System.out.println(
                    "Membresía individual registrada. ID: " + idIndividual
            );

            // PRUEBA 2: Membresía para dos personas.
            System.out.println("\n=== PRUEBA 2: DOS PERSONAS ===");

            int idCompartida = servicio.registrarMembresia(
                    tipoMembresia.DOSPERSONAS,
                    LocalDate.now(),
                    new BigDecimal("1200.00"),
                    1,
                    2
            );

            System.out.println(
                    "Membresía compartida registrada. ID: " + idCompartida
            );

            // PRUEBA 3: Validación de socios repetidos.
            System.out.println("\n=== PRUEBA 3: SOCIOS REPETIDOS ===");

            try {
                servicio.registrarMembresia(
                        tipoMembresia.DOSPERSONAS,
                        LocalDate.now(),
                        new BigDecimal("1200.00"),
                        1,
                        1
                );

                System.out.println("ERROR: Se permitió repetir el socio.");

            } catch (IllegalArgumentException e) {
                System.out.println("Validación correcta: " + e.getMessage());
            }*/

 /*System.out.println("=== PRUEBA 1: SOCIO CON MEMBRESÍA VIGENTE ===");

            try {
                servicio.registrarMembresia(
                        tipoMembresia.MENSUAL,
                        LocalDate.now(),
                        new BigDecimal("850.00"),
                        1,
                        null
                );

                System.out.println("ERROR: Se permitió registrar una segunda membresía.");
            } catch (IllegalArgumentException e) {
                System.out.println("Validación correcta: " + e.getMessage());
            }

            System.out.println("=== PRUEBA 2: MEMBRESÍA COMPARTIDA ===");

            try {
                servicio.registrarMembresia(
                        tipoMembresia.DOSPERSONAS,
                        LocalDate.now(),
                        new BigDecimal("1200.00"),
                        2,
                        1
                );

                System.out.println("ERROR: Se permitió registrar la membresía compartida.");
            } catch (IllegalArgumentException e) {
                System.out.println("Validación correcta: " + e.getMessage());
            }

            System.out.println("\nPRUEBAS FINALIZADAS.");

        } catch (Exception e) {
            System.out.println("ERROR DURANTE LAS PRUEBAS:");
            e.printStackTrace();
        }*/
    }
}
