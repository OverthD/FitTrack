package com.fittrack.service;

import com.fittrack.dao.AccesoDAO;
import com.fittrack.dao.MembresiaDAO;
import com.fittrack.dao.SocioDAO;
import com.fittrack.dao.SocioMembresiaDAO;

import com.fittrack.database.Conexion;

import com.fittrack.model.Acceso;
import com.fittrack.model.Membresia;
import com.fittrack.model.Socio;
import com.fittrack.model.estadoMembresia;
import com.fittrack.model.estadoSocio;
import com.fittrack.model.AccesoReporte;
import com.fittrack.util.SesionUsuario;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Time;

import java.time.LocalDate;
import java.time.LocalTime;

import java.util.List;

public class AccesoService {

    private final AccesoDAO accesoDAO;
    private final SocioDAO socioDAO;
    private final MembresiaDAO membresiaDAO;
    private final SocioMembresiaDAO socioMembresiaDAO;

    public AccesoService() {

        this.accesoDAO = new AccesoDAO();
        this.socioDAO = new SocioDAO();
        this.membresiaDAO = new MembresiaDAO();
        this.socioMembresiaDAO = new SocioMembresiaDAO();
    }

    // =============================================================
    // REGISTRAR ENTRADA
    // =============================================================
    public int registrarEntrada(int idSocio)
            throws SQLException, IOException {

        // Verificar que exista una sesion activa.
        int idEmpleado = SesionUsuario.obtenerIdEmpleado();

        if (idSocio <= 0) {
            throw new IllegalArgumentException(
                    "El identificador del socio no es valido."
            );
        }

        // Comprobar que el socio exista.
        Socio socio = socioDAO.buscarPorId(idSocio);

        if (socio == null) {
            throw new IllegalArgumentException(
                    "El socio no existe."
            );
        }

        // Comprobar que el socio este activo.
        if (socio.getEstado() != estadoSocio.ACTIVO) {
            throw new IllegalArgumentException(
                    "El socio no se encuentra activo."
            );
        }

        // Comprobar que no tenga una entrada sin salida.
        Acceso accesoAbierto
                = accesoDAO.buscarAccesoAbiertoPorSocio(idSocio);

        if (accesoAbierto != null) {
            throw new IllegalArgumentException(
                    "El socio ya tiene una entrada registrada "
                    + "sin una salida correspondiente."
            );
        }

        // Comprobar que tenga una membresia que permita el acceso.
        if (!tieneMembresiaValida(idSocio)) {
            throw new IllegalArgumentException(
                    "El socio no cuenta con una membresia vigente "
                    + "que permita el acceso."
            );
        }

        // Crear el registro de entrada.
        LocalDate fecha = LocalDate.now();
        LocalTime horaEntrada = LocalTime.now().withNano(0);

        Acceso acceso = new Acceso(
                idSocio,
                idEmpleado,
                fecha,
                horaEntrada
        );

        // Guardar el acceso.
        try (Connection conexion = Conexion.conectar()) {

            return accesoDAO.registrar(conexion, acceso);
        }
    }

    // =============================================================
    // REGISTRAR SALIDA
    // =============================================================
    public boolean registrarSalida(int idSocio)
            throws SQLException, IOException {

        // Verificar que exista una sesion activa.
        SesionUsuario.obtenerIdEmpleado();

        if (idSocio <= 0) {
            throw new IllegalArgumentException(
                    "El identificador del socio no es valido."
            );
        }

        // Buscar una entrada sin salida.
        Acceso acceso
                = accesoDAO.buscarAccesoAbiertoPorSocio(idSocio);

        if (acceso == null) {
            throw new IllegalArgumentException(
                    "El socio no tiene una entrada pendiente de salida."
            );
        }

        // Registrar la hora de salida en el objeto.
        acceso.registrarSalida();

        // Actualizar la hora de salida en la base de datos.
        boolean actualizado = accesoDAO.registrarSalida(
                acceso.getIdAcceso(),
                Time.valueOf(acceso.getHoraSalida())
        );

        if (!actualizado) {
            throw new SQLException(
                    "No se pudo registrar la salida del socio."
            );
        }

        return true;
    }

    // =============================================================
    // CONSULTAR HISTORIAL DE ACCESOS
    // =============================================================
    public List<Acceso> consultarHistorial(int idSocio)
            throws SQLException, IOException {

        // Verificar que exista una sesion activa.
        SesionUsuario.obtenerIdEmpleado();

        if (idSocio <= 0) {
            throw new IllegalArgumentException(
                    "El identificador del socio no es valido."
            );
        }

        // Comprobar que el socio exista.
        Socio socio = socioDAO.buscarPorId(idSocio);

        if (socio == null) {
            throw new IllegalArgumentException(
                    "El socio no existe."
            );
        }

        return accesoDAO.listarPorSocio(idSocio);
    }

    // =============================================================
    // GENERAR REPORTE DE ACCESOS (RF-21)
    // =============================================================
    public List<AccesoReporte> generarReporteAccesos(
            LocalDate fechaInicio,
            LocalDate fechaFin)
            throws SQLException, IOException {

        // Verificar permisos administrativos.
        if (!SesionUsuario.haySesionActiva()
                || !SesionUsuario.esAdministrador()) {

            throw new SecurityException(
                    "No tiene permisos para generar el reporte "
                    + "de accesos. Se requiere un administrador."
            );
        }

        validarPeriodo(fechaInicio, fechaFin);

        return accesoDAO.listarDatosReporte(
                fechaInicio,
                fechaFin
        );
    }

    // Validar las fechas del reporte.
    private void validarPeriodo(
            LocalDate fechaInicio,
            LocalDate fechaFin) {

        if (fechaInicio == null || fechaFin == null) {
            throw new IllegalArgumentException(
                    "Debe especificar las fechas del periodo."
            );
        }

        if (fechaInicio.isAfter(fechaFin)) {
            throw new IllegalArgumentException(
                    "La fecha inicial no puede ser posterior "
                    + "a la fecha final."
            );
        }
    }

    // =============================================================
    // VALIDAR MEMBRESIA
    // =============================================================
    private boolean tieneMembresiaValida(int idSocio)
            throws SQLException, IOException {

        LocalDate hoy = LocalDate.now();

        // Obtener las membresias asociadas al socio.
        List<Integer> idsMembresias
                = socioMembresiaDAO.listarIdsMembresiasPorSocio(
                        idSocio
                );

        // Revisar cada membresia.
        for (Integer idMembresia : idsMembresias) {

            Membresia membresia
                    = membresiaDAO.buscarPorId(idMembresia);

            if (membresia == null) {
                continue;
            }

            // Calcular el estado real para la fecha actual.
            estadoMembresia estadoActual
                    = membresia.calcularEstado(hoy);

            // Permitir acceso solamente con estos estados.
            if (estadoActual == estadoMembresia.VIGENTE
                    || estadoActual == estadoMembresia.TOLERANCIA) {

                return true;
            }
        }

        return false;
    }
}
