package com.fittrack.servicio;

import java.util.List;

/** Métodos de pago permitidos (valores fijos para que MySQL siempre guarde lo mismo). */
public final class MetodoPago {

    public static final String EFECTIVO = "Efectivo";
    public static final String TARJETA = "Tarjeta";
    public static final String TRANSFERENCIA = "Transferencia";
    public static final List<String> TODOS = List.of(EFECTIVO, TARJETA, TRANSFERENCIA);

    private MetodoPago() { }
}
