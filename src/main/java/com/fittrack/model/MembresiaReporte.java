
package com.fittrack.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class MembresiaReporte {

    private int idSocio;
    private String nombre;
    private String apellido;
    private int idMembresia;
    private tipoMembresia tipo;
    private LocalDate fechaInicio;
    private LocalDate fechaVencimiento;
    private BigDecimal precio;
    private estadoMembresia estado;

    public MembresiaReporte(
            int idSocio,
            String nombre,
            String apellido,
            int idMembresia,
            tipoMembresia tipo,
            LocalDate fechaInicio,
            LocalDate fechaVencimiento,
            BigDecimal precio,
            estadoMembresia estado) {

        this.idSocio = idSocio;
        this.nombre = nombre;
        this.apellido = apellido;
        this.idMembresia = idMembresia;
        this.tipo = tipo;
        this.fechaInicio = fechaInicio;
        this.fechaVencimiento = fechaVencimiento;
        this.precio = precio;
        this.estado = estado;
    }

    public int getIdSocio() {
        return idSocio;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
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

    public BigDecimal getPrecio() {
        return precio;
    }

    public estadoMembresia getEstado() {
        return estado;
    }
}
