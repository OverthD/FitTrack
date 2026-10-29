
package com.fittrack.model;

import java.time.LocalDate;

public class Socio {

    private int idSocio;
    private String nombre;
    private String apellido;
    private String correo;
    private String telefono;
    private LocalDate fechaRegistro;
    private estadoSocio estado;

    public Socio(int idSocio, String nombre, String apellido,
                 String correo, String telefono) {
        this.idSocio = idSocio;
        this.nombre = nombre;
        this.apellido = apellido;
        this.correo = correo;
        this.telefono = telefono;
        this.fechaRegistro = LocalDate.now();
        this.estado = estadoSocio.ACTIVO;
    }

    public void asignarMembresia(Membresia m) {
        if (m == null) {
            throw new IllegalArgumentException(
                "La membresía no puede ser nula."
            );
        }

        // La relación persistente se gestionará posteriormente
        // mediante el servicio y el DAO correspondientes.
    }

    public void actualizarDatos(String nombre, String apellido,
                                String correo, String telefono) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.correo = correo;
        this.telefono = telefono;
    }

    public void cambiarEstado(estadoSocio nuevoEstado) {
        if (nuevoEstado == null) {
            throw new IllegalArgumentException(
                "El estado no puede ser nulo."
            );
        }

        this.estado = nuevoEstado;
    }

    public int generarIdentificador() {
        // El identificador definitivo se generará al registrar
        // el socio en la base de datos.
        return idSocio;
    }

    public boolean validarEstudiante() {
        // La vigencia se comprobará con AcreditacionEstudiante.
        // Se implementará cuando integremos esa clase.
        return false;
    }

    public boolean membresiaDoble() {
        // La comprobación se realizará con las membresías
        // asociadas al socio.
        return false;
    }

    public int getIdSocio() {
        return idSocio;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public String getCorreo() {
        return correo;
    }

    public String getTelefono() {
        return telefono;
    }

    public LocalDate getFechaRegistro() {
        return fechaRegistro;
    }

    public estadoSocio getEstado() {
        return estado;
    }
}
