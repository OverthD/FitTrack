package com.fittrack.model;

import java.time.LocalDate;

public class AcreditacionEstudiante {

    private int idAcreditacion;
    private int idSocio;
    private LocalDate fechaVerificacion;
    private LocalDate fechaVigencia;
    private String tipoComprobante;

    public AcreditacionEstudiante(
            int idAcreditacion,
            int idSocio,
            LocalDate fechaVerificacion,
            LocalDate fechaVigencia,
            String tipoComprobante) {

        this.idAcreditacion = idAcreditacion;
        this.idSocio = idSocio;
        this.fechaVerificacion = fechaVerificacion;
        this.fechaVigencia = fechaVigencia;
        this.tipoComprobante = tipoComprobante;
    }

    public boolean estadoVigencia() {

        if (fechaVigencia == null) {
            return false;
        }

        return !LocalDate.now().isAfter(fechaVigencia);
    }

    public void asignarMembresia(Membresia m) {

        if (m == null) {
            throw new IllegalArgumentException(
                    "La membresía no puede ser nula."
            );
        }

        if (m.getTipo() != tipoMembresia.ESTUDIANTE) {
            throw new IllegalArgumentException(
                    "La acreditación solo corresponde "
                    + "a una membresía estudiantil."
            );
        }

        if (!estadoVigencia()) {
            throw new IllegalStateException(
                    "La acreditación estudiantil ha vencido."
            );
        }
    }

    public int getIdAcreditacion() {
        return idAcreditacion;
    }

    public int getIdSocio() {
        return idSocio;
    }

    public LocalDate getFechaVerificacion() {
        return fechaVerificacion;
    }

    public LocalDate getFechaVigencia() {
        return fechaVigencia;
    }

    public String getTipoComprobante() {
        return tipoComprobante;
    }
}