package com.fittrack.model;

import java.time.LocalDate;

public class SocioActivoReporte {

    private int idSocio;
    private String nombre;
    private String apellido;
    private String telefono;
    private String correo;
    private String tipoMembresia;
    private LocalDate fechaVencimiento;
    private estadoMembresia estadoMembresia;

    public SocioActivoReporte(
            int idSocio,
            String nombre,
            String apellido,
            String telefono,
            String correo,
            String tipoMembresia,
            LocalDate fechaVencimiento,
            estadoMembresia estadoMembresia) {

        this.idSocio = idSocio;
        this.nombre = nombre;
        this.apellido = apellido;
        this.telefono = telefono;
        this.correo = correo;
        this.tipoMembresia = tipoMembresia;
        this.fechaVencimiento = fechaVencimiento;
        this.estadoMembresia = estadoMembresia;
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

    public String getTelefono() {
        return telefono;
    }

    public String getCorreo() {
        return correo;
    }

    public String getTipoMembresia() {
        return tipoMembresia;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public estadoMembresia getEstadoMembresia() {
        return estadoMembresia;
    }
}