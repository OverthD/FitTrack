package com.fittrack.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Pago {

    private int idPago;
    private int idMembresia;
    private Integer idEmpleado;
    private LocalDate fechaPago;
    private BigDecimal monto;
    private String metodoPago;

    // Constructor para registrar un pago nuevo.
    public Pago(int idMembresia,
            Integer idEmpleado,
            LocalDate fechaPago,
            BigDecimal monto,
            String metodoPago) {

        if (idMembresia <= 0) {
            throw new IllegalArgumentException(
                    "El identificador de la membresía debe ser válido."
            );
        }

        if (fechaPago == null) {
            throw new IllegalArgumentException(
                    "Debe especificar la fecha del pago."
            );
        }

        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "El monto debe ser mayor que cero."
            );
        }

        if (metodoPago == null || metodoPago.isBlank()) {
            throw new IllegalArgumentException(
                    "Debe especificar el método de pago."
            );
        }

        this.idPago = 0;
        this.idMembresia = idMembresia;
        this.idEmpleado = idEmpleado;
        this.fechaPago = fechaPago;
        this.monto = monto;
        this.metodoPago = metodoPago;
    }

    public void registrarPago() {
        // El registro en MySQL se realiza mediante PagoDAO.
    }

    public int getIdPago() {
        return idPago;
    }

    public int getIdMembresia() {
        return idMembresia;
    }

    public Integer getIdEmpleado() {
        return idEmpleado;
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

    public void setIdPago(int idPago) {
        this.idPago = idPago;
    }
}
