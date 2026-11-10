
package com.fittrack.model;

import java.time.LocalDate;

public class Respaldo {

    private int idRespaldo;
    private LocalDate fecha;

    public Respaldo(int idRespaldo, LocalDate fecha) {
        this.idRespaldo = idRespaldo;
        this.fecha = fecha;
    }

    public int getIdRespaldo() {
        return idRespaldo;
    }

    public void setIdRespaldo(int idRespaldo) {
        this.idRespaldo = idRespaldo;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }
}
