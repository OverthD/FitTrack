
package com.fittrack.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class ReportePagos {

    private final LocalDate fechaInicio;
    private final LocalDate fechaFin;
    private final List<PagoReporte> pagos;
    private final BigDecimal totalAcumulado;

    public ReportePagos(
            LocalDate fechaInicio,
            LocalDate fechaFin,
            List<PagoReporte> pagos,
            BigDecimal totalAcumulado) {

        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.pagos = List.copyOf(pagos);
        this.totalAcumulado = totalAcumulado;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public List<PagoReporte> getPagos() {
        return pagos;
    }

    public BigDecimal getTotalAcumulado() {
        return totalAcumulado;
    }
}
