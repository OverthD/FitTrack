package com.fittrack.servicio;

import com.fittrack.model.Membresia;
import com.fittrack.model.Pago;
import com.fittrack.model.Socio;

/** Una fila del historial de pagos. La membresía puede ser null. */
public record FilaPago(Pago pago, Socio socio, Membresia membresia) { }
