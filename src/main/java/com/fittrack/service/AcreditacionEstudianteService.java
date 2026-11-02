
package com.fittrack.service;

import com.fittrack.dao.AcreditacionEstudianteDAO;
import com.fittrack.dao.SocioDAO;
import com.fittrack.model.AcreditacionEstudiante;
import com.fittrack.model.Socio;
import com.fittrack.model.estadoSocio;
import com.fittrack.util.SesionUsuario;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class AcreditacionEstudianteService {

    private final AcreditacionEstudianteDAO acreditacionDAO;
    private final SocioDAO socioDAO;

    public AcreditacionEstudianteService() {
        this.acreditacionDAO = new AcreditacionEstudianteDAO();
        this.socioDAO = new SocioDAO();
    }

    // Registrar una nueva acreditación para un socio.
    public int registrarAcreditacion(
            int idSocio,
            LocalDate fechaVerificacion,
            LocalDate fechaVigencia,
            String tipoComprobante)
            throws SQLException, IOException {

        // Verificar que exista una sesión activa.
        SesionUsuario.obtenerIdEmpleado();

        // Validar el identificador del socio.
        if (idSocio <= 0) {
            throw new IllegalArgumentException(
                    "El identificador del socio debe ser válido."
            );
        }

        // Validar las fechas.
        if (fechaVerificacion == null || fechaVigencia == null) {
            throw new IllegalArgumentException(
                    "Las fechas de acreditación son obligatorias."
            );
        }

        if (fechaVerificacion.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "La fecha de verificación no puede ser futura."
            );
        }

        if (fechaVigencia.isBefore(fechaVerificacion)) {
            throw new IllegalArgumentException(
                    "La fecha de vigencia no puede ser anterior "
                    + "a la fecha de verificación."
            );
        }

        // Validar el tipo de comprobante.
        if (tipoComprobante == null || tipoComprobante.isBlank()) {
            throw new IllegalArgumentException(
                    "Debe especificar el tipo de comprobante."
            );
        }

        // Comprobar que el socio exista y esté activo.
        Socio socio = socioDAO.buscarPorId(idSocio);

        if (socio == null) {
            throw new IllegalArgumentException(
                    "No existe un socio con el identificador "
                    + idSocio + "."
            );
        }

        if (socio.getEstado() != estadoSocio.ACTIVO) {
            throw new IllegalArgumentException(
                    "El socio no se encuentra activo."
            );
        }

        // Crear el objeto de acreditación.
        AcreditacionEstudiante acreditacion =
                new AcreditacionEstudiante(
                        0,
                        idSocio,
                        fechaVerificacion,
                        fechaVigencia,
                        tipoComprobante.trim()
                );

        // Registrar en la base de datos.
        return acreditacionDAO.registrar(acreditacion);
    }

    // Consultar una acreditación por su identificador.
    public AcreditacionEstudiante buscarPorId(int idAcreditacion)
            throws SQLException, IOException {

        SesionUsuario.obtenerIdEmpleado();

        if (idAcreditacion <= 0) {
            throw new IllegalArgumentException(
                    "El identificador de la acreditación no es válido."
            );
        }

        return acreditacionDAO.buscarPorId(idAcreditacion);
    }

    // Consultar el historial de acreditaciones de un socio.
    public List<AcreditacionEstudiante> listarPorSocio(int idSocio)
            throws SQLException, IOException {

        SesionUsuario.obtenerIdEmpleado();

        if (idSocio <= 0) {
            throw new IllegalArgumentException(
                    "El identificador del socio no es válido."
            );
        }

        return acreditacionDAO.listarPorSocio(idSocio);
    }

    // Consultar si el socio tiene una acreditación vigente.
    public AcreditacionEstudiante consultarAcreditacionVigente(
            int idSocio)
            throws SQLException, IOException {

        SesionUsuario.obtenerIdEmpleado();

        if (idSocio <= 0) {
            throw new IllegalArgumentException(
                    "El identificador del socio no es válido."
            );
        }

        return acreditacionDAO.buscarVigentePorSocio(idSocio);
    }

    // Comprobar si el socio puede contratar una membresía estudiantil.
    public boolean tieneAcreditacionVigente(int idSocio)
            throws SQLException, IOException {

        return consultarAcreditacionVigente(idSocio) != null;
    }
}
