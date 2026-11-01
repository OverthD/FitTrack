
package com.fittrack.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Membresia {

    private int idMembresia;
    private tipoMembresia tipo;
    private LocalDate fechaInicio;
    private LocalDate fechaVencimiento;
    private BigDecimal precio;
    private estadoMembresia estado;

    // Constructor para crear una membresía nueva.
    public Membresia(int idMembresia, tipoMembresia tipo,
                     LocalDate fechaInicio, BigDecimal precio) {

        this.idMembresia = idMembresia;
        this.tipo = tipo;
        this.fechaInicio = fechaInicio;
        asignarPrecio(precio);
        this.fechaVencimiento = calcularVencimiento();
        this.estado = calcularEstado();
    }

    // Constructor para recuperar una membresía desde la base de datos.
    public Membresia(int idMembresia, tipoMembresia tipo,
                     LocalDate fechaInicio, LocalDate fechaVencimiento,
                     BigDecimal precio, estadoMembresia estado) {

        this.idMembresia = idMembresia;
        this.tipo = tipo;
        this.fechaInicio = fechaInicio;
        this.fechaVencimiento = fechaVencimiento;
        asignarPrecio(precio);
        this.estado = estado;
    }

    public void asignarPrecio(BigDecimal precio) {
        if (precio == null || precio.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                "El precio no puede ser nulo ni negativo."
            );
        }

        this.precio = precio;
    }

    public LocalDate calcularVencimiento() {

        if (fechaInicio == null || tipo == null) {
            throw new IllegalStateException(
                "Se requiere una fecha de inicio y un tipo de membresía."
            );
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
                return fechaInicio.plusMonths(1);

            default:
                throw new IllegalStateException(
                    "Tipo de membresía no reconocido."
                );
        }
    }

    public estadoMembresia calcularEstado() {

        return calcularEstado(LocalDate.now());
    }

    // Sobrecarga para facilitar las pruebas con fechas específicas.
    public estadoMembresia calcularEstado(LocalDate fechaConsulta) {

        if (fechaConsulta == null || fechaInicio == null
                || fechaVencimiento == null || tipo == null) {
            throw new IllegalStateException(
                "No se puede calcular el estado con datos incompletos."
            );
        }

        if (fechaConsulta.isBefore(fechaInicio)) {
            return estadoMembresia.PROGRAMADA;
        }

        if (!fechaConsulta.isAfter(fechaVencimiento)) {
            return estadoMembresia.VIGENTE;
        }

        if (tipo == tipoMembresia.MENSUAL) {

            LocalDate finTolerancia = fechaVencimiento.plusDays(3);

            if (!fechaConsulta.isAfter(finTolerancia)) {
                return estadoMembresia.TOLERANCIA;
            }
        }

        return estadoMembresia.VENCIDA;
    }

    // Actualiza el estado almacenado en el objeto.
    public void actualizarEstado() {
        this.estado = calcularEstado();
    }

    public boolean permitirAcceso() {

        estadoMembresia estadoActual = calcularEstado();

        return estadoActual == estadoMembresia.VIGENTE
            || estadoActual == estadoMembresia.TOLERANCIA;
    }

    // Getters

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

    public BigDecimal getPrecio() {
        return precio;
    }

    public estadoMembresia getEstado() {
        return estado;
    }
}
