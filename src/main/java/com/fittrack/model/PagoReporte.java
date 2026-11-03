
package com.fittrack.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class PagoReporte {

    private int idPago;
    private List<Integer> idsSocios;
    private List<String> nombresSocios;
    private int idMembresia;
    private tipoMembresia tipoMembresia;
    private LocalDate fechaPago;
    private BigDecimal monto;
    private String metodoPago;

    public PagoReporte(
            int idPago,
            List<Integer> idsSocios,
            List<String> nombresSocios,
            int idMembresia,
            tipoMembresia tipoMembresia,
            LocalDate fechaPago,
            BigDecimal monto,
            String metodoPago) {

        this.idPago = idPago;
        this.idsSocios = List.copyOf(idsSocios);
        this.nombresSocios = List.copyOf(nombresSocios);
        this.idMembresia = idMembresia;
        this.tipoMembresia = tipoMembresia;
        this.fechaPago = fechaPago;
        this.monto = monto;
        this.metodoPago = metodoPago;
    }

    public int getIdPago() {
        return idPago;
    }

    public List<Integer> getIdsSocios() {
        return idsSocios;
    }

    public List<String> getNombresSocios() {
        return nombresSocios;
    }

    public int getIdMembresia() {
        return idMembresia;
    }

    public tipoMembresia getTipoMembresia() {
        return tipoMembresia;
    }

    public LocalDate getFechaPago() {
        return fechaPago;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public String getMetodoPago() {
        return metodoPago;
    }
}
