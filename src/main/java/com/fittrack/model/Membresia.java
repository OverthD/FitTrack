
package com.fittrack.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Membresia {

    private int idMembresia;
    private tipoMembresia tipo;
    private LocalDate fechaInicio;
    private LocalDate fechaVencimiento;
    private double precio;
    private estadoMembresia estado;

    public Membresia(int idMembresia, tipoMembresia tipo,
                     LocalDate fechaInicio, double precio) {
        this.idMembresia = idMembresia;
        this.tipo = tipo;
        this.fechaInicio = fechaInicio;
        this.precio = precio;
        this.fechaVencimiento = calcularVencimiento();
        this.estado = calcularEstado();
    }

    public void asignarPrecio(double precio) {
        if (precio < 0) {
            throw new IllegalArgumentException(
                "El precio no puede ser negativo."
            );
        }
        this.precio = precio;
    }

    public LocalDate calcularVencimiento() {
        if (fechaInicio == null || tipo == null) {
            return null;
        }

        switch (tipo) {
            case SEMANAL:
                return fechaInicio.plusDays(7);
            case QUINCENAL:
                return fechaInicio.plusDays(15);
            case MENSUAL:
            case ESTUDIANTE:
                return fechaInicio.plusMonths(1);
            case BIMESTRAL:
                return fechaInicio.plusMonths(2);
            case ANUAL:
                return fechaInicio.plusMonths(12);
            case DOSPERSONAS:
                // La duración debe configurarse según las reglas del gimnasio.
                return null;
            default:
                throw new IllegalStateException(
                    "Tipo de membresía no reconocido."
                );
        }
    }

    public estadoMembresia calcularEstado() {
        if (fechaInicio == null || fechaVencimiento == null) {
            return estadoMembresia.VENCIDA;
        }

        LocalDate hoy = LocalDate.now();

        if (hoy.isBefore(fechaInicio)) {
            return estadoMembresia.PROGRAMADA;
        }

        if (!hoy.isAfter(fechaVencimiento)) {
            return estadoMembresia.VIGENTE;
        }

        if (tipo == tipoMembresia.MENSUAL) {
            LocalDate finTolerancia = fechaVencimiento.plusDays(3);

            if (!hoy.isAfter(finTolerancia)) {
                return estadoMembresia.TOLERANCIA;
            }
        }

        return estadoMembresia.VENCIDA;
    }

    public boolean permitirAcceso() {
        estadoMembresia estadoActual = calcularEstado();

        return estadoActual == estadoMembresia.VIGENTE
            || estadoActual == estadoMembresia.TOLERANCIA;
    }

    public void renovar() {
        fechaInicio = LocalDate.now();
        fechaVencimiento = calcularVencimiento();
        estado = calcularEstado();
    }

    public int getIdMembresia() {
        return idMembresia;
    }

    public tipoMembresia getTipo() {
        return tipo;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public double getPrecio() {
        return precio;
    }

    public estadoMembresia getEstado() {
        estado = calcularEstado();
        return estado;
    }
}
