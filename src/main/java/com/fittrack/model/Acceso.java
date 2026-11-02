
package com.fittrack.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class Acceso {

    private int idAcceso;
    private int idSocio;
    private Integer idEmpleado;
    private LocalDate fecha;
    private LocalTime horaEntrada;
    private LocalTime horaSalida;

    // Constructor para registrar una nueva entrada.
    public Acceso(int idSocio,
                  Integer idEmpleado,
                  LocalDate fecha,
                  LocalTime horaEntrada) {

        this.idSocio = idSocio;
        this.idEmpleado = idEmpleado;
        this.fecha = fecha;
        this.horaEntrada = horaEntrada;
        this.horaSalida = null;
    }

    // Constructor para recuperar un acceso de la base de datos.
    public Acceso(int idAcceso,
                  int idSocio,
                  Integer idEmpleado,
                  LocalDate fecha,
                  LocalTime horaEntrada,
                  LocalTime horaSalida) {

        this.idAcceso = idAcceso;
        this.idSocio = idSocio;
        this.idEmpleado = idEmpleado;
        this.fecha = fecha;
        this.horaEntrada = horaEntrada;
        this.horaSalida = horaSalida;
    }

    public void registrarEntrada() {

        if (horaEntrada != null) {
            throw new IllegalStateException(
                "La entrada ya está registrada."
            );
        }

        fecha = LocalDate.now();
        horaEntrada = LocalTime.now();
    }

    public void registrarSalida() {

        if (horaEntrada == null) {
            throw new IllegalStateException(
                "No se puede registrar una salida sin entrada."
            );
        }

        if (horaSalida != null) {
            throw new IllegalStateException(
                "La salida ya está registrada."
            );
        }

        horaSalida = LocalTime.now();
    }

    public int getIdAcceso() {
        return idAcceso;
    }

    public int getIdSocio() {
        return idSocio;
    }

    public Integer getIdEmpleado() {
        return idEmpleado;
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

    public void setIdAcceso(int idAcceso) {
        this.idAcceso = idAcceso;
    }
}
