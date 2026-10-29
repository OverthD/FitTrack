
package com.fittrack.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class Acceso {

    private int idAcceso;
    private LocalDate fecha;
    private LocalTime horaEntrada;
    private LocalTime horaSalida;

    public Acceso(int idAcceso, LocalDate fecha,
                  LocalTime horaEntrada) {

        this.idAcceso = idAcceso;
        this.fecha = fecha;
        this.horaEntrada = horaEntrada;
        this.horaSalida = null;
    }

    public void registrarEntrada() {
        if (horaEntrada != null) {
            throw new IllegalStateException(
                "La entrada ya está registrada."
            );
        }

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
