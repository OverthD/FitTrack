package com.fittrack.servicio;

import com.fittrack.model.Acceso;
import com.fittrack.model.Socio;

/** Una fila del historial de accesos. */
public record FilaAcceso(Acceso acceso, Socio socio) { }
