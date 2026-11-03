package com.fittrack;

import com.fittrack.model.SocioActivoReporte;
import com.fittrack.service.SocioService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PruebaReporteSociosActivos {

    public static void main(String[] args) throws Exception {

        SocioService servicio = new SocioService();

        List<SocioActivoReporte> socios
                = servicio.generarReporteSociosActivos();

        Set<Integer> idsSocios = new HashSet<>();

        System.out.println("\n===== REPORTE DE SOCIOS ACTIVOS =====");

        for (SocioActivoReporte socio : socios) {

            idsSocios.add(socio.getIdSocio());

            System.out.println("------------------------------");
            System.out.println("ID: " + socio.getIdSocio());
            System.out.println("Nombre: " + socio.getNombre());
            System.out.println("Apellido: " + socio.getApellido());
            System.out.println("Teléfono: " + socio.getTelefono());
            System.out.println("Correo: " + socio.getCorreo());
            System.out.println("Tipo de membresía: "
                    + socio.getTipoMembresia());
            System.out.println("Fecha de vencimiento: "
                    + socio.getFechaVencimiento());
        }

        System.out.println("------------------------------");
        System.out.println("Total de registros: " + socios.size());
        System.out.println("Total de socios diferentes: "
                + idsSocios.size());
        System.out.println("====================================");
    }
}