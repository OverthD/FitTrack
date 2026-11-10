package com.fittrack.servicio;

import com.fittrack.model.tipoMembresia;

public record LineaVenta(tipoMembresia tipo, int cantidad, double monto) { }
