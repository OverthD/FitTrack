package com.fittrack;

import com.fittrack.model.Socio;
import com.fittrack.service.SocioService;
import java.util.List;

public class PruebaSociosActivos {

    public static void main(String[] args) {

        try {
            SocioService socioService = new SocioService();

            List<Socio> socios = socioService.listarSociosActivos();

            System.out.println("===== REPORTE DE SOCIOS ACTIVOS =====");
            System.out.println("Total de socios activos: " + socios.size());

            for (Socio socio : socios) {
                System.out.println("------------------------------");
                System.out.println("ID: " + socio.getIdSocio());
                System.out.println("Nombre: " + socio.getNombre());
                System.out.println("Apellido: " + socio.getApellido());
                System.out.println("Estado: " + socio.getEstado());
            }

            System.out.println("====================================");

        } catch (Exception e) {
            System.out.println("Error al consultar socios activos:");
            e.printStackTrace();
        }
    }
}