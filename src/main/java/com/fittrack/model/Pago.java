
package com.fittrack.model;

import java.time.LocalDate;

public class Pago {

    private int idPago;
    private LocalDate fechaPago;
    private double monto;
    private String metodoPago;

    public Pago(int idPago, LocalDate fechaPago,
                double monto, String metodoPago) {

        if (monto <= 0) {
            throw new IllegalArgumentException(
                "El monto debe ser mayor que cero."
            );
        }

        this.idPago = idPago;
        this.fechaPago = fechaPago;
        this.monto = monto;
        this.metodoPago = metodoPago;
    }

    public void registrarPago() {
        // El registro definitivo se realizará mediante el DAO
        // cuando conectemos la aplicación con MySQL.
    }

    public int getIdPago() {
        return idPago;
    }

    public LocalDate getFechaPago() {
        return fechaPago;
    }

    public double getMonto() {
        return monto;
    }

    public String getMetodoPago() {
        return metodoPago;
    }
}
