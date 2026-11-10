package Pruebas;

import com.fittrack.model.Pago;
import com.fittrack.service.PagoService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.fittrack.model.Membresia;
import com.fittrack.service.MembresiaService;

public class PruebaReportesService {

    public static void main(String[] args) throws Exception {

        PagoService servicio = new PagoService();

        LocalDate inicio = LocalDate.of(2026, 11, 1);
        LocalDate fin = LocalDate.of(2026, 11, 2);

        System.out.println("===== RF-11: HISTORIAL DE PAGOS =====");

        for (int idSocio : new int[]{4, 5, 9}) {

            List<Pago> pagos
                    = servicio.consultarHistorialPagos(idSocio);

            System.out.println("\nSocio: " + idSocio);

            for (Pago pago : pagos) {
                System.out.println(
                        "Pago: " + pago.getIdPago()
                        + " | Membresía: " + pago.getIdMembresia()
                        + " | Fecha: " + pago.getFechaPago()
                        + " | Monto: " + pago.getMonto()
                        + " | Método: " + pago.getMetodoPago()
                );
            }

            System.out.println("Cantidad de pagos: " + pagos.size());
        }

        System.out.println("\n===== RF-14: REPORTE DE INGRESOS =====");

        BigDecimal total
                = servicio.consultarIngresos(inicio, fin);

        System.out.println("Ingresos totales: $" + total);

        Map<String, BigDecimal> porMetodo
                = servicio.consultarIngresosPorMetodo(inicio, fin);

        System.out.println("\nIngresos por método:");

        porMetodo.forEach((metodo, importe)
                -> System.out.println(metodo + ": $" + importe)
        );

        List<Pago> detalle
                = servicio.consultarPagosPorPeriodo(inicio, fin);

        System.out.println("\nDetalle de pagos:");

        for (Pago pago : detalle) {
            System.out.println(
                    "Pago: " + pago.getIdPago()
                    + " | Membresía: " + pago.getIdMembresia()
                    + " | Monto: $" + pago.getMonto()
            );
        }

        System.out.println("Total de registros: " + detalle.size());

        System.out.println("\n===== RF-15: REPORTE DE VENTAS =====");

        int totalVentas
                = servicio.consultarTotalVentas(inicio, fin);

        System.out.println("Total de ventas: " + totalVentas);

        Map<String, Integer> ventasPorTipo
                = servicio.consultarVentasPorTipo(inicio, fin);

        System.out.println("\nCantidad por tipo:");

        ventasPorTipo.forEach((tipo, cantidad)
                -> System.out.println(tipo + ": " + cantidad)
        );

        Map<String, BigDecimal> importesPorTipo
                = servicio.consultarImportesPorTipo(inicio, fin);

        System.out.println("\nImportes por tipo:");

        importesPorTipo.forEach((tipo, importe)
                -> System.out.println(tipo + ": $" + importe)
        );

        System.out.println("\n===== RF-13: MEMBRESÍAS PRÓXIMAS A VENCER =====");

        MembresiaService membresiaService = new MembresiaService();

        List<Membresia> proximas
                = membresiaService.consultarProximasAVencer(30);

        for (Membresia membresia : proximas) {
            System.out.println(
                    "ID: " + membresia.getIdMembresia()
                    + " | Tipo: " + membresia.getTipo()
                    + " | Vencimiento: " + membresia.getFechaVencimiento()
                    + " | Estado: " + membresia.getEstado()
            );
        }

        System.out.println("Total próximas a vencer: " + proximas.size());

        System.out.println("\n===== VALIDACIONES RF-11 =====");

        try {
            servicio.consultarHistorialPagos(0);
        } catch (IllegalArgumentException e) {
            System.out.println("ID 0 rechazado: " + e.getMessage());
        }

        try {
            servicio.consultarHistorialPagos(999999);
        } catch (IllegalArgumentException e) {
            System.out.println("Socio inexistente rechazado: " + e.getMessage());
        }

        System.out.println("\n===== VALIDACIONES RF-13 =====");

        try {
            membresiaService.consultarProximasAVencer(0);
        } catch (IllegalArgumentException e) {
            System.out.println("Periodo 0 rechazado: " + e.getMessage());
        }

        try {
            membresiaService.consultarProximasAVencer(-5);
        } catch (IllegalArgumentException e) {
            System.out.println("Periodo negativo rechazado: " + e.getMessage());
        }

        List<Membresia> proximasUnDia
                = membresiaService.consultarProximasAVencer(1);

        System.out.println(
                "Membresías que vencen en el próximo día: "
                + proximasUnDia.size()
        );

        System.out.println("\n===== VALIDACIONES RF-14 Y RF-15 =====");

        try {
            servicio.consultarIngresos(
                    LocalDate.of(2026, 11, 2),
                    LocalDate.of(2026, 11, 1)
            );
        } catch (IllegalArgumentException e) {
            System.out.println("Periodo invertido rechazado: " + e.getMessage());
        }

        try {
            servicio.consultarTotalVentas(null, fin);
        } catch (IllegalArgumentException e) {
            System.out.println("Fecha nula rechazada: " + e.getMessage());
        }
    }
}
