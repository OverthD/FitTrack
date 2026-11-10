package com.fittrack.servicio;

import com.fittrack.model.Acceso;
import java.math.BigDecimal;
import com.fittrack.model.Membresia;
import com.fittrack.model.Pago;
import com.fittrack.model.Socio;
import com.fittrack.model.UsuarioSistema;
import com.fittrack.model.estadoSocio;
import com.fittrack.model.rolUsuario;
import com.fittrack.model.tipoMembresia;
import java.time.LocalDate;
import java.util.List;

/**
 * PUNTO DE CONEXIÓN ENTRE LAS PANTALLAS Y LA LÓGICA.
 *
 * Las pantallas solo hablan con esta interfaz. Hoy la implementa ServicioDemo
 * (datos en memoria). Para conectar MySQL, crear ServicioMySQL implements
 * FitTrackServicio (usando sus DAO) y cambiar UNA línea en FitTrack.java.
 *
 * Los métodos lanzan IllegalArgumentException / IllegalStateException con un
 * mensaje listo para mostrarse al usuario.
 */
public interface FitTrackServicio {

    // ---------- Autenticación ----------
    /**
     * Devuelve el usuario si las credenciales son válidas, o null.
     */
    UsuarioSistema autenticar(String usuario, String contrasena);

    // ---------- Socios ----------
    /**
     * texto: ID o nombre (puede ser vacío). filtro: null = todos.
     */
    List<SocioResumen> listarSocios(String texto, estadoSocio filtro);

    /**
     * Todas las membresías registradas, mostrando una fila por socio asociado.
     */
    List<SocioResumen> listarMembresias();

    /**
     * null si no existe.
     */
    SocioResumen buscarSocio(int idSocio);

    Socio registrarSocio(String nombre, String apellido, String correo, String telefono);

    void actualizarSocio(int idSocio, String nombre, String apellido, String correo, String telefono);

    void cambiarEstadoSocio(int idSocio, estadoSocio nuevoEstado, String motivo);

    // ---------- Membresías ----------
    double precioDe(tipoMembresia tipo);

    /**
     * idSegundoSocio solo para DOSPERSONAS; acreditacion solo para ESTUDIANTE.
     */
    Membresia registrarMembresia(
            int idSocio,
            tipoMembresia tipo,
            LocalDate inicio,
            Integer idSegundoSocio,
            LocalDate fechaVerificacion,
            LocalDate fechaVigencia,
            String tipoComprobante,
            String metodoPago,
            BigDecimal precio
    );

    /**
     * Renueva la membresía actual y registra el pago.
     */
    Membresia renovarMembresia(int idSocio, String metodoPago, double monto);

    /**
     * Membresías (vigentes o en tolerancia) que vencen dentro de los próximos
     * "dias".
     */
    List<SocioResumen> membresiasProximasAVencer(int dias);

    // ---------- Pagos ----------
    Pago registrarPago(int idSocio, double monto, String metodoPago, LocalDate fecha);

    List<FilaPago> listarPagos(String texto);

    // ---------- Accesos ----------
    Acceso registrarEntrada(int idSocio);

    void registrarSalida(int idAcceso);

    /**
     * fecha null = todas.
     */
    List<FilaAcceso> listarAccesos(LocalDate fecha, String texto);

    List<FilaAcceso> ultimosAccesos(int cantidad);

    // ---------- Administración ----------
    ResumenVentas resumenVentas(LocalDate desde, LocalDate hasta);

    /**
     * filtro: "Todos" o el texto de la opción que se eligió en pantalla.
     */
    TablaReporte reporte(TipoReporte tipo, LocalDate desde, LocalDate hasta, String filtro);

    List<UsuarioSistema> listarUsuarios();

    UsuarioSistema crearUsuario(rolUsuario rol, String usuario, String contrasena);

    void cambiarActivoUsuario(int idEmpleado, boolean activo);

    int siguienteIdUsuario();

    void actualizarDatosUsuario(
            int idEmpleado,
            rolUsuario rol,
            String usuario);

    void cambiarContrasenaUsuario(
            int idEmpleado,
            String nuevaContrasena);

    /**
     * Devuelve el nombre del archivo de respaldo generado.
     */
    String generarRespaldo(String carpeta) throws Exception;
}
