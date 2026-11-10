package com.fittrack.servicio;

import com.fittrack.model.AcreditacionEstudiante;
import com.fittrack.model.Membresia;
import com.fittrack.model.Socio;

/** Socio junto con su membresía actual (la membresía puede ser null). */
public class SocioResumen {

    private final Socio socio;
    private final Membresia membresia;
    private final Integer idSegundoSocio;
    private final AcreditacionEstudiante acreditacion;

    public SocioResumen(Socio socio, Membresia membresia,
                        Integer idSegundoSocio, AcreditacionEstudiante acreditacion) {
        this.socio = socio;
        this.membresia = membresia;
        this.idSegundoSocio = idSegundoSocio;
        this.acreditacion = acreditacion;
    }

    public Socio getSocio() { return socio; }
    public Membresia getMembresia() { return membresia; }
    public Integer getIdSegundoSocio() { return idSegundoSocio; }
    public AcreditacionEstudiante getAcreditacion() { return acreditacion; }

    public String nombreCompleto() {
        return socio.getNombre() + " " + socio.getApellido();
    }
}
