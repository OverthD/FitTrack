package com.fittrack.service;

import com.fittrack.dao.PagoDAO;
import com.fittrack.dao.SocioDAO;
import com.fittrack.model.Pago;
import com.fittrack.model.Socio;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import com.fittrack.model.PagoReporte;
import com.fittrack.model.ReportePagos;
import com.fittrack.util.SesionUsuario;

public class PagoService {

    private final PagoDAO pagoDAO;
    private final SocioDAO socioDAO;

    public PagoService() {
        this.pagoDAO = new PagoDAO();
        this.socioDAO = new SocioDAO();
    }

    // Consultar el historial de pagos de un socio.
    public List<Pago> consultarHistorialPagos(int idSocio)
            throws SQLException, IOException {

        if (idSocio <= 0) {
            throw new IllegalArgumentException(
                    "El identificador del socio debe ser válido."
            );
        }

        Socio socio = socioDAO.buscarPorId(idSocio);

        if (socio == null) {
            throw new IllegalArgumentException(
                    "No existe un socio con el identificador "
                    + idSocio + "."
            );
        }

        return pagoDAO.listarPorSocio(idSocio);
    }

    // Consultar el reporte de ingresos de un periodo.
    public BigDecimal consultarIngresos(
            LocalDate fechaInicio,
            LocalDate fechaFin)
            throws SQLException, IOException {

        validarPeriodo(fechaInicio, fechaFin);

        return pagoDAO.calcularIngresos(fechaInicio, fechaFin);
    }

    // Consultar el desglose de ingresos por método de pago.
    public Map<String, BigDecimal> consultarIngresosPorMetodo(
            LocalDate fechaInicio,
            LocalDate fechaFin)
            throws SQLException, IOException {

        validarPeriodo(fechaInicio, fechaFin);

        return pagoDAO.calcularIngresosPorMetodo(
                fechaInicio,
                fechaFin
        );
    }

    // Consultar el detalle de pagos de un periodo.
    public List<Pago> consultarPagosPorPeriodo(
            LocalDate fechaInicio,
            LocalDate fechaFin)
            throws SQLException, IOException {

        validarPeriodo(fechaInicio, fechaFin);

        return pagoDAO.listarPorPeriodo(fechaInicio, fechaFin);
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

    // Consulta la cantidad total de membresías vendidas.
    public int consultarTotalVentas(
            LocalDate fechaInicio,
            LocalDate fechaFin)
            throws SQLException, IOException {

        validarPeriodo(fechaInicio, fechaFin);

        return pagoDAO.contarVentas(fechaInicio, fechaFin);
    }

// Consulta la cantidad de ventas agrupadas por tipo de membresía.
    public Map<String, Integer> consultarVentasPorTipo(
            LocalDate fechaInicio,
            LocalDate fechaFin)
            throws SQLException, IOException {

        validarPeriodo(fechaInicio, fechaFin);

        return pagoDAO.contarVentasPorTipo(fechaInicio, fechaFin);
    }

// Consulta el importe vendido agrupado por tipo de membresía.
    public Map<String, BigDecimal> consultarImportesPorTipo(
            LocalDate fechaInicio,
            LocalDate fechaFin)
            throws SQLException, IOException {

        validarPeriodo(fechaInicio, fechaFin);

        return pagoDAO.calcularImportesPorTipo(fechaInicio, fechaFin);
    }

    // Generar el reporte de pagos correspondiente al RF-20.
    public ReportePagos generarReportePagos(
            LocalDate fechaInicio,
            LocalDate fechaFin)
            throws SQLException, IOException {

        if (!SesionUsuario.haySesionActiva()
                || !SesionUsuario.esAdministrador()) {

            throw new SecurityException(
                    "No tiene permisos para generar el reporte de pagos. "
                    + "Se requiere un administrador."
            );
        }

        validarPeriodo(fechaInicio, fechaFin);

        List<PagoReporte> pagos = pagoDAO.listarDatosReporte(
                fechaInicio,
                fechaFin
        );

        BigDecimal totalAcumulado = pagoDAO.calcularIngresos(
                fechaInicio,
                fechaFin
        );

        return new ReportePagos(
                fechaInicio,
                fechaFin,
                pagos,
                totalAcumulado
        );
    }

    // Consultar todos los pagos registrados en el sistema.
    public List<Pago> consultarTodosPagos()
            throws SQLException, IOException {

        return pagoDAO.listarTodos();
    }

}
