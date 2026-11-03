
package com.fittrack.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class AccesoReporte {

    private final int idSocio;
    private final String nombreSocio;
    private final LocalDate fecha;
    private final LocalTime horaEntrada;
    private final LocalTime horaSalida;

    public AccesoReporte(
            int idSocio,
            String nombreSocio,
            LocalDate fecha,
            LocalTime horaEntrada,
            LocalTime horaSalida) {

        this.idSocio = idSocio;
        this.nombreSocio = nombreSocio;
        this.fecha = fecha;
        this.horaEntrada = horaEntrada;
        this.horaSalida = horaSalida;
    }

    public int getIdSocio() {
        return idSocio;
    }

    public String getNombreSocio() {
        return nombreSocio;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public LocalTime getHoraEntrada() {
        return horaEntrada;
    }

    public LocalTime getHoraSalida() {
        return horaSalida;
    }
}
