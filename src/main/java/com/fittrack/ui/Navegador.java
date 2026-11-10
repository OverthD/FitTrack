package com.fittrack.ui;

/** Lo que una pantalla puede pedir para cambiar a otra. Lo implementa VentanaPrincipal. */
public interface Navegador {

    /** Claves del menú: validar-acceso, socios, membresias, pagos, accesos, ingresos, reportes, usuarios, respaldo. */
    void ir(String clave);

    void irNuevoSocio();

    void irPerfil(int idSocio);

    /** idSocio puede ser null si todavía no se eligió. */
    void irRegistrarMembresia(Integer idSocio);

    void irRenovar(int idSocio);

    /** idSocio y monto pueden ser null. */
    void irRegistrarPago(Integer idSocio, Double monto);

    void irValidarAcceso(Integer idSocio);

    void irPagosDe(int idSocio);

    void irAccesosDe(int idSocio);
}
