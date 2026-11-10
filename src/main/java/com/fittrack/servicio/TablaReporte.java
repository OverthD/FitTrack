package com.fittrack.servicio;

import java.util.List;

public record TablaReporte(List<String> columnas, List<List<String>> filas) { }
