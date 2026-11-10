package com.fittrack.servicio;

import java.util.List;

public record ResumenVentas(double ingresos, int vendidas, int renovaciones, List<LineaVenta> lineas) { }
