
package com.fittrack.servicio;

import java.time.LocalDate;

public class RespaldoResumen {

    private final int idRespaldo;
    private final LocalDate fecha;
    private final String administrador;

    public RespaldoResumen(
            int idRespaldo,
            LocalDate fecha,
            String administrador) {

        this.idRespaldo = idRespaldo;
        this.fecha = fecha;
        this.administrador = administrador;
    }

    public int getIdRespaldo() {
        return idRespaldo;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public String getAdministrador() {
        return administrador;
    }
}
