package com.fittrack.service;

import com.fittrack.dao.MembresiaDAO;
import com.fittrack.dao.PagoDAO;
import com.fittrack.dao.SocioDAO;
import com.fittrack.dao.SocioMembresiaDAO;
import com.fittrack.database.Conexion;
import com.fittrack.model.Membresia;
import com.fittrack.model.Pago;
import com.fittrack.model.Socio;
import com.fittrack.model.tipoMembresia;
import com.fittrack.model.estadoMembresia;
import com.fittrack.model.estadoSocio;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.fittrack.util.SesionUsuario;

public class MembresiaService {

    private final MembresiaDAO membresiaDAO;
    private final PagoDAO pagoDAO;
    private final SocioDAO socioDAO;
    private final SocioMembresiaDAO socioMembresiaDAO;

    public MembresiaService() {
        this.membresiaDAO = new MembresiaDAO();
        this.pagoDAO = new PagoDAO();
        this.socioDAO = new SocioDAO();
        this.socioMembresiaDAO = new SocioMembresiaDAO();
    }

    // Registrar una membresía, asociarla con uno o dos socios
    // y registrar su pago dentro de una sola transacción.
    public int registrarMembresia(
            tipoMembresia tipo,
            LocalDate fechaInicio,
            BigDecimal precio,
            int idSocio1,
            Integer idSocio2,
            String metodoPago)
            throws SQLException, IOException {

        validarDatos(tipo, fechaInicio, precio, metodoPago);

        if (idSocio1 <= 0) {
            throw new IllegalArgumentException(
                    "El identificador del primer socio debe ser válido."
            );
        }

        if (tipo == tipoMembresia.DOSPERSONAS) {

            if (idSocio2 == null || idSocio2 <= 0) {
                throw new IllegalArgumentException(
                        "La membresía para dos personas requiere "
                        + "un segundo socio."
                );
            }

            if (idSocio1 == idSocio2) {
                throw new IllegalArgumentException(
                        "Los dos socios deben ser diferentes."
                );
            }

        } else if (idSocio2 != null) {

            throw new IllegalArgumentException(
                    "Este tipo de membresía solamente permite un socio."
            );
        }

        validarSocio(idSocio1);

        if (idSocio2 != null) {
            validarSocio(idSocio2);
        }

        Membresia membresia = new Membresia(
                0,
                tipo,
                fechaInicio,
                precio
        );

        validarDisponibilidadMembresia(idSocio1, membresia);

        if (idSocio2 != null) {
            validarDisponibilidadMembresia(idSocio2, membresia);
        }
        // Obtener el identificador del usuario que realiza la operación.
        int idEmpleado = SesionUsuario.obtenerIdEmpleado();
        try (Connection conexion = Conexion.conectar()) {

            conexion.setAutoCommit(false);

            try {
                // 1. Registrar la membresía.
                int idMembresia = membresiaDAO.registrar(
                        conexion,
                        membresia
                );

                // 2. Asociar el primer socio.
                socioMembresiaDAO.asociar(
                        conexion,
                        idSocio1,
                        idMembresia
                );

                // 3. Asociar el segundo socio, si corresponde.
                if (idSocio2 != null) {
                    socioMembresiaDAO.asociar(
                            conexion,
                            idSocio2,
                            idMembresia
                    );
                }

                // 4. Crear el pago con el mismo precio de la membresía.
                Pago pago = new Pago(
                        idMembresia,
                        idEmpleado,
                        LocalDate.now(),
                        membresia.getPrecio(),
                        metodoPago
                );

                // 5. Registrar el pago usando la misma conexión.
                pagoDAO.registrar(conexion, pago);

                // 6. Confirmar todas las operaciones.
                conexion.commit();

                return idMembresia;

            } catch (SQLException | RuntimeException e) {
                conexion.rollback();
                throw e;
            }
        }
    }

    // Renovar una membresía conservando sus socios asociados.
    public int renovarMembresia(
            int idMembresiaAnterior,
            tipoMembresia nuevoTipo,
            BigDecimal nuevoPrecio,
            String metodoPago)
            throws SQLException, IOException {

        if (idMembresiaAnterior <= 0) {
            throw new IllegalArgumentException(
                    "El identificador de la membresía anterior no es válido."
            );
        }

        // Recuperar la membresía anterior y actualizar su estado.
        Membresia anterior
                = consultarMembresia(idMembresiaAnterior);

        if (anterior == null) {
            throw new IllegalArgumentException(
                    "No existe la membresía que se desea renovar."
            );
        }

        // Solo se permite renovar membresías vigentes o en tolerancia.
        estadoMembresia estadoActual = anterior.calcularEstado();

        if (estadoActual != estadoMembresia.VIGENTE
                && estadoActual != estadoMembresia.TOLERANCIA) {

            throw new IllegalArgumentException(
                    "Solamente se pueden renovar membresías vigentes "
                    + "o en tolerancia."
            );
        }

        // Recuperar los socios asociados a la membresía anterior.
        List<Integer> idsSocios
                = socioMembresiaDAO.listarIdsSociosPorMembresia(
                        idMembresiaAnterior
                );

        if (idsSocios == null || idsSocios.isEmpty()
                || idsSocios.size() > 2) {

            throw new IllegalArgumentException(
                    "La membresía anterior no tiene una asociación "
                    + "válida de socios."
            );
        }

        // Comprobar que la cantidad de socios corresponda
        // con el tipo de la membresía original.
        if (anterior.getTipo() == tipoMembresia.DOSPERSONAS) {

            if (idsSocios.size() != 2) {
                throw new IllegalArgumentException(
                        "La membresía compartida debe tener "
                        + "exactamente dos socios asociados."
                );
            }

        } else if (idsSocios.size() != 1) {

            throw new IllegalArgumentException(
                    "La membresía individual debe tener "
                    + "exactamente un socio asociado."
            );
        }

        // El nuevo tipo debe conservar la modalidad individual
        // o compartida de la membresía anterior.
        if (idsSocios.size() == 2
                && nuevoTipo != tipoMembresia.DOSPERSONAS) {

            throw new IllegalArgumentException(
                    "Una membresía compartida debe renovarse "
                    + "como membresía para dos personas."
            );
        }

        if (idsSocios.size() == 1
                && nuevoTipo == tipoMembresia.DOSPERSONAS) {

            throw new IllegalArgumentException(
                    "Una membresía individual no puede convertirse "
                    + "en compartida durante la renovación."
            );
        }

        // Verificar que los socios continúen activos.
        for (int idSocio : idsSocios) {
            validarSocio(idSocio);
        }

        // La nueva membresía comienza después del periodo
        // efectivo de la anterior, incluyendo tolerancia.
        LocalDate nuevaFechaInicio
                = calcularFinEfectivo(anterior).plusDays(1);

        validarDatos(
                nuevoTipo,
                nuevaFechaInicio,
                nuevoPrecio,
                metodoPago
        );

        // Crear la nueva membresía con el tipo y precio seleccionados.
        Membresia nuevaMembresia = new Membresia(
                0,
                nuevoTipo,
                nuevaFechaInicio,
                nuevoPrecio
        );

        // Comprobar que no existan otras membresías traslapadas.
        for (int idSocio : idsSocios) {
            validarDisponibilidadMembresia(
                    idSocio,
                    nuevaMembresia
            );
        }

        // Obtener el identificador del usuario que realiza la renovación.
        int idEmpleado = SesionUsuario.obtenerIdEmpleado();
        // Registrar membresía, asociaciones y pago en una transacción.
        try (Connection conexion = Conexion.conectar()) {

            conexion.setAutoCommit(false);

            try {
                // 1. Registrar la nueva membresía.
                int nuevoIdMembresia = membresiaDAO.registrar(
                        conexion,
                        nuevaMembresia
                );

                // 2. Copiar las asociaciones de los socios.
                for (int idSocio : idsSocios) {
                    socioMembresiaDAO.asociar(
                            conexion,
                            idSocio,
                            nuevoIdMembresia
                    );
                }

                // 3. Crear el pago correspondiente a la renovación.
                Pago nuevoPago = new Pago(
                        nuevoIdMembresia,
                        idEmpleado,
                        LocalDate.now(),
                        nuevaMembresia.getPrecio(),
                        metodoPago
                );

                // 4. Registrar el pago.
                pagoDAO.registrar(conexion, nuevoPago);

                // 5. Confirmar todas las operaciones.
                conexion.commit();

                return nuevoIdMembresia;

            } catch (SQLException | RuntimeException e) {
                conexion.rollback();
                throw e;
            }
        }
    }

    // Validar que el socio exista y esté activo.
    private void validarSocio(int idSocio)
            throws SQLException, IOException {

        Socio socio = socioDAO.buscarPorId(idSocio);

        if (socio == null) {
            throw new IllegalArgumentException(
                    "No existe un socio con el identificador "
                    + idSocio + "."
            );
        }

        if (socio.getEstado() != estadoSocio.ACTIVO) {
            throw new IllegalArgumentException(
                    "El socio con identificador " + idSocio
                    + " no se encuentra activo."
            );
        }
    }

    // Consultar una membresía por su identificador.
    public Membresia consultarMembresia(int idMembresia)
            throws SQLException, IOException {

        if (idMembresia <= 0) {
            throw new IllegalArgumentException(
                    "El identificador debe ser válido."
            );
        }

        Membresia membresia = membresiaDAO.buscarPorId(idMembresia);

        if (membresia != null) {
            sincronizarEstado(membresia);
        }

        return membresia;
    }

    // Listar todas las membresías y sincronizar sus estados.
    public List<Membresia> listarMembresias()
            throws SQLException, IOException {

        List<Membresia> membresias = membresiaDAO.listarTodas();

        for (Membresia membresia : membresias) {
            sincronizarEstado(membresia);
        }

        return membresias;
    }

    // Actualizar el estado almacenado si cambió el estado calculado.
    private void sincronizarEstado(Membresia membresia)
            throws SQLException, IOException {

        estadoMembresia estadoCalculado = membresia.calcularEstado();

        if (estadoCalculado != membresia.getEstado()) {

            boolean actualizado = membresiaDAO.actualizarEstado(
                    membresia.getIdMembresia(),
                    estadoCalculado
            );

            if (!actualizado) {
                throw new SQLException(
                        "No se pudo actualizar el estado de la membresía."
                );
            }

            membresia.actualizarEstado();
        }
    }

    // Validaciones generales.
    private void validarDatos(
            tipoMembresia tipo,
            LocalDate fechaInicio,
            BigDecimal precio,
            String metodoPago) {

        if (tipo == null) {
            throw new IllegalArgumentException(
                    "Debe seleccionar un tipo de membresía."
            );
        }

        if (fechaInicio == null) {
            throw new IllegalArgumentException(
                    "Debe especificar la fecha de inicio."
            );
        }

        if (precio == null || precio.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "El precio debe ser mayor que cero."
            );
        }

        if (metodoPago == null || metodoPago.isBlank()) {
            throw new IllegalArgumentException(
                    "Debe especificar el método de pago."
            );
        }
    }

    // Verificar que el socio no tenga otra membresía
    // cuyo periodo se traslape con el nuevo.
    private void validarDisponibilidadMembresia(
            int idSocio,
            Membresia nuevaMembresia)
            throws SQLException, IOException {

        List<Integer> idsMembresias
                = socioMembresiaDAO.listarIdsMembresiasPorSocio(idSocio);

        LocalDate nuevoInicio = nuevaMembresia.getFechaInicio();
        LocalDate nuevoFin = calcularFinEfectivo(nuevaMembresia);

        for (int idMembresia : idsMembresias) {

            Membresia existente
                    = membresiaDAO.buscarPorId(idMembresia);

            if (existente == null) {
                continue;
            }

            LocalDate inicioExistente = existente.getFechaInicio();
            LocalDate finExistente = calcularFinEfectivo(existente);

            boolean seTraslapan
                    = !nuevoInicio.isAfter(finExistente)
                    && !inicioExistente.isAfter(nuevoFin);

            if (seTraslapan) {
                throw new IllegalArgumentException(
                        "El socio con identificador " + idSocio
                        + " ya tiene una membresía cuyo periodo "
                        + "se traslapa con las fechas solicitadas."
                );
            }
        }
    }

    // Considerar los tres días de tolerancia de las membresías mensuales.
    private LocalDate calcularFinEfectivo(Membresia membresia) {

        LocalDate fechaFin = membresia.getFechaVencimiento();

        if (membresia.getTipo() == tipoMembresia.MENSUAL) {
            return fechaFin.plusDays(3);
        }

        return fechaFin;
    }

}
