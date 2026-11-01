package com.fittrack.service;

import com.fittrack.dao.SocioDAO;
import com.fittrack.model.Socio;
import com.fittrack.model.estadoSocio;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class SocioService {

    private final SocioDAO socioDAO;

    public SocioService() {
        this.socioDAO = new SocioDAO();
    }

    // Registrar socio con validaciones.
    public int registrarSocio(String nombre, String apellido,
            String correo, String telefono)
            throws SQLException, IOException {

        validarDatos(nombre, apellido, correo, telefono);

        return socioDAO.registrar(
                nombre.trim(),
                apellido.trim(),
                correo.trim(),
                telefono.trim()
        );
    }

    // Buscar socio.
    public Socio consultarSocio(int idSocio)
            throws SQLException, IOException {

        if (idSocio <= 0) {
            throw new IllegalArgumentException(
                    "El identificador debe ser válido."
            );
        }

        return socioDAO.buscarPorId(idSocio);
    }

    // Consultar todos los socios.
    public List<Socio> listarSocios()
            throws SQLException, IOException {

        return socioDAO.listarTodos();
    }

    // Actualizar datos personales.
    public boolean actualizarSocio(int idSocio,
            String nombre,
            String apellido,
            String correo,
            String telefono)
            throws SQLException, IOException {

        if (idSocio <= 0) {
            throw new IllegalArgumentException(
                    "El identificador debe ser válido."
            );
        }

        validarDatos(nombre, apellido, correo, telefono);

        return socioDAO.actualizarDatos(
                idSocio,
                nombre.trim(),
                apellido.trim(),
                correo.trim(),
                telefono.trim()
        );
    }

    // Validar los datos recibidos.
    private void validarDatos(String nombre, String apellido,
            String correo, String telefono) {

        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre es obligatorio."
            );
        }

        if (apellido == null || apellido.isBlank()) {
            throw new IllegalArgumentException(
                    "El apellido es obligatorio."
            );
        }

        if (correo == null || correo.isBlank()
                || !correo.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException(
                    "El correo electrónico no es válido."
            );
        }

        if (telefono == null || !telefono.matches("\\d{10}")) {
            throw new IllegalArgumentException(
                    "El teléfono debe contener exactamente 10 dígitos."
            );
        }

        if (nombre.trim().length() > 45
                || apellido.trim().length() > 45
                || correo.trim().length() > 45) {
            throw new IllegalArgumentException(
                    "Uno de los campos supera la longitud permitida."
            );
        }
    }

    public boolean cambiarEstadoSocio(int idSocio, estadoSocio nuevoEstado)
            throws SQLException, IOException {

        if (idSocio <= 0) {
            throw new IllegalArgumentException("El identificador debe ser válido.");
        }

        if (nuevoEstado == null) {
            throw new IllegalArgumentException("Debe seleccionar un estado.");
        }

        return socioDAO.cambiarEstado(idSocio, nuevoEstado);
    }
}
