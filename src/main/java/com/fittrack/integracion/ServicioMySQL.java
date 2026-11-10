package com.fittrack.integracion;

import com.fittrack.model.Membresia;
import com.fittrack.model.Socio;
import com.fittrack.model.UsuarioSistema;
import com.fittrack.model.estadoSocio;
import com.fittrack.model.rolUsuario;

import com.fittrack.service.MembresiaService;
import com.fittrack.service.SocioService;
import com.fittrack.service.UsuarioSistemaService;
import com.fittrack.servicio.FitTrackServicio;
import com.fittrack.servicio.SocioResumen;
import com.fittrack.dao.SocioMembresiaDAO;
import com.fittrack.dao.MembresiaDAO;
import com.fittrack.servicio.FilaPago;
import com.fittrack.service.PagoService;

import com.fittrack.model.Acceso;
import com.fittrack.servicio.FilaAcceso;
import com.fittrack.dao.AccesoDAO;
import com.fittrack.service.AccesoService;

import com.fittrack.model.Pago;
import com.fittrack.model.tipoMembresia;
import com.fittrack.servicio.LineaVenta;
import com.fittrack.servicio.ResumenVentas;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.fittrack.service.AcreditacionEstudianteService;


import java.io.IOException;
import java.sql.SQLException;

import com.fittrack.model.SocioActivoReporte;
import com.fittrack.model.MembresiaReporte;
import com.fittrack.model.PagoReporte;
import com.fittrack.model.ReportePagos;
import com.fittrack.model.AccesoReporte;

import com.fittrack.model.estadoMembresia;
import com.fittrack.servicio.TablaReporte;
import com.fittrack.servicio.TipoReporte;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ServicioMySQL implements FitTrackServicio {

    private final UsuarioSistemaService usuarioService;
    private final SocioService socioService;
    private final MembresiaService membresiaService;
    private final AcreditacionEstudianteService acreditacionService;
    private final MembresiaDAO membresiaDAO;
    private final PagoService pagoService;
    private final AccesoDAO accesoDAO;
    private final AccesoService accesoService;

    public ServicioMySQL() {
        this.usuarioService = new UsuarioSistemaService();
        this.socioService = new SocioService();
        this.membresiaService = new MembresiaService();
        this.acreditacionService = new AcreditacionEstudianteService();
        this.membresiaDAO = new MembresiaDAO();
        this.pagoService = new PagoService();
        this.accesoDAO = new AccesoDAO();
        this.accesoService = new AccesoService();
    }

    // --------------------------------------------------
    // AUTENTICACIÓN
    // --------------------------------------------------
    @Override
    public UsuarioSistema autenticar(String usuario, String contrasena) {
        try {
            return usuarioService.iniciarSesion(usuario, contrasena);
        } catch (SQLException | IOException ex) {
            throw new IllegalStateException(
                    "No fue posible iniciar sesión. Verifique la conexión con la base de datos.",
                    ex
            );
        }
    }

    // --------------------------------------------------
    // SOCIOS
    // --------------------------------------------------
    @Override
    public List<SocioResumen> listarSocios(String texto, estadoSocio filtro) {
        try {
            List<Socio> socios = socioService.listarSocios();
            List<SocioResumen> resultado = new ArrayList<>();

            String busqueda = texto == null
                    ? ""
                    : texto.trim().toLowerCase(Locale.ROOT);

            for (Socio socio : socios) {

                if (filtro != null && socio.getEstado() != filtro) {
                    continue;
                }

                boolean coincide = busqueda.isEmpty()
                        || String.valueOf(socio.getIdSocio()).contains(busqueda)
                        || socio.getNombre().toLowerCase(Locale.ROOT).contains(busqueda)
                        || socio.getApellido().toLowerCase(Locale.ROOT).contains(busqueda)
                        || (socio.getNombre() + " " + socio.getApellido())
                                .toLowerCase(Locale.ROOT).contains(busqueda);

                if (coincide) {
                    resultado.add(construirResumen(socio));
                }
            }

            return resultado;

        } catch (SQLException | IOException ex) {
            throw new IllegalStateException(
                    "No fue posible consultar los socios.",
                    ex
            );
        }
    }

    @Override
    public SocioResumen buscarSocio(int idSocio) {
        try {
            Socio socio = socioService.consultarSocio(idSocio);

            if (socio == null) {
                return null;
            }

            return construirResumen(socio);

        } catch (SQLException | IOException ex) {
            throw new IllegalStateException(
                    "No fue posible consultar el socio.",
                    ex
            );
        }
    }

    @Override
    public Socio registrarSocio(
            String nombre,
            String apellido,
            String correo,
            String telefono) {

        try {
            int idSocio = socioService.registrarSocio(
                    nombre, apellido, correo, telefono
            );

            return socioService.consultarSocio(idSocio);

        } catch (SQLException | IOException ex) {
            throw new IllegalStateException(
                    "No fue posible registrar el socio.",
                    ex
            );
        }
    }

    @Override
    public void actualizarSocio(
            int idSocio,
            String nombre,
            String apellido,
            String correo,
            String telefono) {

        try {
            boolean actualizado = socioService.actualizarSocio(
                    idSocio, nombre, apellido, correo, telefono
            );

            if (!actualizado) {
                throw new IllegalStateException(
                        "No se encontró el socio o no se pudieron actualizar sus datos."
                );
            }

        } catch (SQLException | IOException ex) {
            throw new IllegalStateException(
                    "No fue posible actualizar los datos del socio.",
                    ex
            );
        }
    }

    @Override
    public void cambiarEstadoSocio(
            int idSocio,
            estadoSocio nuevoEstado,
            String motivo) {

        try {
            boolean actualizado = socioService.cambiarEstadoSocio(
                    idSocio, nuevoEstado
            );

            if (!actualizado) {
                throw new IllegalStateException(
                        "No fue posible cambiar el estado del socio."
                );
            }

        } catch (SQLException | IOException ex) {
            throw new IllegalStateException(
                    "No fue posible cambiar el estado del socio.",
                    ex
            );
        }
    }

    /**
     * Construye el objeto que necesita la tabla de socios. La membresía se
     * obtiene mediante la consulta real del backend.
     */
    private SocioResumen construirResumen(Socio socio)
            throws SQLException, IOException {

        Membresia membresia = null;

        List<MembresiaReporte> reportes
                = membresiaDAO.consultarMasRecientePorSocio(
                        socio.getIdSocio()
                );

        if (reportes != null && !reportes.isEmpty()) {
            int idMembresia = reportes.get(0).getIdMembresia();

            membresia = membresiaDAO.buscarPorId(idMembresia);
        }

        return new SocioResumen(
                socio,
                membresia,
                null,
                null
        );
    }

    // --------------------------------------------------
    // MÉTODOS PENDIENTES DE INTEGRACIÓN
    // --------------------------------------------------
    private UnsupportedOperationException pendiente(String metodo) {
        return new UnsupportedOperationException(
                "La operación todavía no está conectada al backend: " + metodo
        );
    }

    @Override
    public double precioDe(tipoMembresia tipo) {
        return 0.0;
    }

    @Override
    public Membresia registrarMembresia(
            int idSocio,
            tipoMembresia tipo,
            LocalDate inicio,
            Integer idSegundoSocio,
            LocalDate fechaVerificacion,
            LocalDate fechaVigencia,
            String tipoComprobante,
            String metodoPago,
            BigDecimal precio) {

        try {
            // Registrar acreditación cuando se trata de una membresía estudiantil.
            if (tipo == tipoMembresia.ESTUDIANTE) {

                acreditacionService.registrarAcreditacion(
                        idSocio,
                        fechaVerificacion,
                        fechaVigencia,
                        tipoComprobante
                );
            }

            // Registrar membresía, asociaciones y pago.
            int idMembresia = membresiaService.registrarMembresia(
                    tipo,
                    inicio,
                    precio,
                    idSocio,
                    idSegundoSocio,
                    metodoPago
            );

            return membresiaService.consultarMembresia(idMembresia);

        } catch (SQLException | IOException ex) {
            throw new IllegalStateException(
                    "No fue posible registrar la membresía.",
                    ex
            );
        }
    }

    @Override
    public Membresia renovarMembresia(
            int idSocio,
            String metodoPago,
            double monto) {

        try {
            List<MembresiaReporte> reportes
                    = membresiaDAO.consultarMasRecientePorSocio(idSocio);

            if (reportes == null || reportes.isEmpty()) {
                throw new IllegalStateException(
                        "El socio no tiene una membresía registrada para renovar."
                );
            }

            int idMembresiaAnterior = reportes.get(0).getIdMembresia();

            Membresia anterior
                    = membresiaDAO.buscarPorId(idMembresiaAnterior);

            if (anterior == null) {
                throw new IllegalStateException(
                        "No se encontró la membresía que se desea renovar."
                );
            }

            int nuevoId = membresiaService.renovarMembresia(
                    idMembresiaAnterior,
                    anterior.getTipo(),
                    BigDecimal.valueOf(monto),
                    metodoPago
            );

            return membresiaDAO.buscarPorId(nuevoId);

        } catch (SQLException | IOException ex) {
            throw new IllegalStateException(
                    "No fue posible renovar la membresía.",
                    ex
            );
        }
    }

    @Override
    public List<SocioResumen> membresiasProximasAVencer(int dias) {

        try {
            List<Membresia> membresias
                    = membresiaService.consultarProximasAVencer(dias);

            List<SocioResumen> resultado = new ArrayList<>();

            SocioMembresiaDAO socioMembresiaDAO
                    = new SocioMembresiaDAO();

            for (Membresia membresia : membresias) {

                List<Integer> idsSocios
                        = socioMembresiaDAO.listarIdsSociosPorMembresia(
                                membresia.getIdMembresia()
                        );

                for (int idSocio : idsSocios) {

                    Socio socio = socioService.consultarSocio(idSocio);

                    if (socio != null) {
                        resultado.add(new SocioResumen(
                                socio,
                                membresia,
                                null,
                                null
                        ));
                    }
                }
            }

            return resultado;

        } catch (SQLException | IOException ex) {
            throw new IllegalStateException(
                    "No fue posible consultar las membresías próximas a vencer.",
                    ex
            );
        }
    }

    @Override
    public List<SocioResumen> listarMembresias() {

        try {
            List<Membresia> membresias = membresiaService.listarMembresias();

            List<SocioResumen> resultado = new ArrayList<>();

            SocioMembresiaDAO socioMembresiaDAO = new SocioMembresiaDAO();

            for (Membresia membresia : membresias) {

                List<Integer> idsSocios
                        = socioMembresiaDAO.listarIdsSociosPorMembresia(
                                membresia.getIdMembresia()
                        );

                for (int idSocio : idsSocios) {

                    Socio socio = socioService.consultarSocio(idSocio);

                    if (socio != null) {
                        resultado.add(new SocioResumen(
                                socio,
                                membresia,
                                null,
                                null
                        ));
                    }
                }
            }

            return resultado;

        } catch (SQLException | IOException ex) {
            throw new IllegalStateException(
                    "No fue posible consultar todas las membresías.",
                    ex
            );
        }
    }

    @Override
    public com.fittrack.model.Pago registrarPago(
            int idSocio,
            double monto,
            String metodoPago,
            LocalDate fecha) {
        throw pendiente("registrarPago");
    }

    @Override
    public List<FilaPago> listarPagos(String texto) {

        try {
            List<Pago> pagos = pagoService.consultarTodosPagos();

            List<FilaPago> resultado = new ArrayList<>();

            SocioMembresiaDAO socioMembresiaDAO
                    = new SocioMembresiaDAO();

            String busqueda = texto == null
                    ? ""
                    : texto.trim().toLowerCase(Locale.ROOT);

            for (Pago pago : pagos) {

                Membresia membresia
                        = membresiaDAO.buscarPorId(pago.getIdMembresia());

                List<Integer> idsSocios
                        = socioMembresiaDAO.listarIdsSociosPorMembresia(
                                pago.getIdMembresia()
                        );

                for (int idSocio : idsSocios) {

                    Socio socio = socioService.consultarSocio(idSocio);

                    if (socio == null) {
                        continue;
                    }

                    boolean coincide = busqueda.isEmpty()
                            || String.valueOf(socio.getIdSocio())
                                    .contains(busqueda)
                            || socio.getNombre()
                                    .toLowerCase(Locale.ROOT)
                                    .contains(busqueda)
                            || socio.getApellido()
                                    .toLowerCase(Locale.ROOT)
                                    .contains(busqueda)
                            || (socio.getNombre() + " " + socio.getApellido())
                                    .toLowerCase(Locale.ROOT)
                                    .contains(busqueda);

                    if (coincide) {
                        resultado.add(new FilaPago(
                                pago,
                                socio,
                                membresia
                        ));
                    }
                }
            }

            return resultado;

        } catch (SQLException | IOException ex) {
            throw new IllegalStateException(
                    "No fue posible consultar el historial de pagos.",
                    ex
            );
        }
    }

    @Override
    public Acceso registrarEntrada(int idSocio) {

        try {
            int idAcceso = accesoService.registrarEntrada(idSocio);

            Acceso acceso = accesoDAO.buscarPorId(idAcceso);

            if (acceso == null) {
                throw new IllegalStateException(
                        "La entrada se registró, pero no fue posible recuperar el acceso."
                );
            }

            return acceso;

        } catch (SQLException | IOException ex) {
            throw new IllegalStateException(
                    "No fue posible registrar la entrada.",
                    ex
            );
        }
    }

    @Override
    public void registrarSalida(int idAcceso) {

        try {
            Acceso acceso = accesoDAO.buscarPorId(idAcceso);

            if (acceso == null) {
                throw new IllegalArgumentException(
                        "El registro de acceso no existe."
                );
            }

            accesoService.registrarSalida(acceso.getIdSocio());

        } catch (SQLException | IOException ex) {
            throw new IllegalStateException(
                    "No fue posible registrar la salida.",
                    ex
            );
        }
    }

    private String FormatoIdSocio(int idSocio) {
        return String.format("FT-%06d", idSocio).toLowerCase(Locale.ROOT);
    }

    @Override
    public List<FilaAcceso> listarAccesos(LocalDate fecha, String texto) {

        try {
            List<Acceso> accesos = accesoDAO.listarTodos();
            List<FilaAcceso> resultado = new ArrayList<>();

            String busqueda = texto == null
                    ? ""
                    : texto.trim().toLowerCase(Locale.ROOT);

            for (Acceso acceso : accesos) {

                if (fecha != null && !acceso.getFecha().equals(fecha)) {
                    continue;
                }

                Socio socio = socioService.consultarSocio(acceso.getIdSocio());

                if (socio == null) {
                    continue;
                }

                String nombreCompleto
                        = (socio.getNombre() + " " + socio.getApellido())
                                .toLowerCase(Locale.ROOT);

                boolean coincide = busqueda.isEmpty()
                        || String.valueOf(socio.getIdSocio()).contains(busqueda)
                        || FormatoIdSocio(socio.getIdSocio()).contains(busqueda)
                        || nombreCompleto.contains(busqueda)
                        || socio.getNombre().toLowerCase(Locale.ROOT).contains(busqueda)
                        || socio.getApellido().toLowerCase(Locale.ROOT).contains(busqueda);

                if (coincide) {
                    resultado.add(new FilaAcceso(acceso, socio));
                }
            }

            return resultado;

        } catch (SQLException | IOException ex) {
            throw new IllegalStateException(
                    "No fue posible consultar el historial de accesos.",
                    ex
            );
        }
    }

    @Override
    public List<FilaAcceso> ultimosAccesos(int cantidad) {

        if (cantidad <= 0) {
            throw new IllegalArgumentException(
                    "La cantidad debe ser mayor que cero."
            );
        }

        try {
            List<Acceso> accesos = accesoDAO.listarTodos();
            List<FilaAcceso> resultado = new ArrayList<>();

            for (Acceso acceso : accesos) {

                if (resultado.size() >= cantidad) {
                    break;
                }

                Socio socio = socioService.consultarSocio(
                        acceso.getIdSocio()
                );

                if (socio != null) {
                    resultado.add(new FilaAcceso(acceso, socio));
                }
            }

            return resultado;

        } catch (SQLException | IOException ex) {
            throw new IllegalStateException(
                    "No fue posible consultar los últimos accesos.",
                    ex
            );
        }
    }

    @Override
    public ResumenVentas resumenVentas(
            LocalDate desde,
            LocalDate hasta) {

        try {
            // Consultar ingresos y cantidad global de ventas.
            BigDecimal ingresos = pagoService.consultarIngresos(
                    desde,
                    hasta
            );

            int vendidas = pagoService.consultarTotalVentas(
                    desde,
                    hasta
            );

            // Consultar pagos del periodo.
            List<Pago> pagos = pagoService.consultarPagosPorPeriodo(
                    desde,
                    hasta
            );

            // Identificar renovaciones sin contar dos veces una membresía.
            Set<Integer> membresiasRevisadas = new LinkedHashSet<>();
            int renovaciones = 0;

            for (Pago pago : pagos) {
                int idMembresia = pago.getIdMembresia();

                if (idMembresia > 0
                        && membresiasRevisadas.add(idMembresia)
                        && membresiaDAO.tieneMembresiaAnterior(idMembresia)) {

                    renovaciones++;
                }
            }

            // Consultar ventas e importes agrupados por tipo.
            Map<String, Integer> cantidades
                    = pagoService.consultarVentasPorTipo(desde, hasta);

            Map<String, BigDecimal> importes
                    = pagoService.consultarImportesPorTipo(desde, hasta);

            List<LineaVenta> lineas = new ArrayList<>();

            for (Map.Entry<String, Integer> entrada : cantidades.entrySet()) {

                tipoMembresia tipo = tipoMembresia.valueOf(
                        entrada.getKey()
                );

                BigDecimal monto = importes.getOrDefault(
                        entrada.getKey(),
                        BigDecimal.ZERO
                );

                lineas.add(new LineaVenta(
                        tipo,
                        entrada.getValue(),
                        monto.doubleValue()
                ));
            }

            return new ResumenVentas(
                    ingresos.doubleValue(),
                    vendidas,
                    renovaciones,
                    lineas
            );

        } catch (SQLException | IOException ex) {
            throw new IllegalStateException(
                    "No fue posible consultar el resumen de ventas.",
                    ex
            );
        }
    }

    @Override
    public TablaReporte reporte(
            TipoReporte tipo,
            LocalDate desde,
            LocalDate hasta,
            String filtro) {

        try {

            List<String> columnas = new ArrayList<>();
            List<List<String>> filas = new ArrayList<>();

            String filtroActual = filtro == null
                    ? "Todos"
                    : filtro.trim();

            switch (tipo) {

                // =========================================
                // REPORTE DE SOCIOS ACTIVOS
                // =========================================
                case SOCIOS_ACTIVOS -> {

                    columnas.addAll(List.of(
                            "ID socio",
                            "Nombre",
                            "Apellido",
                            "Teléfono",
                            "Correo",
                            "Membresía",
                            "Vencimiento",
                            "Estado"
                    ));

                    List<SocioActivoReporte> socios
                            = socioService.generarReporteSociosActivos();

                    for (SocioActivoReporte socio : socios) {

                        estadoMembresia estado
                                = socio.getEstadoMembresia();

                        if (!filtroActual.equalsIgnoreCase("Todos")
                                && !estado.name().equalsIgnoreCase(filtroActual)) {
                            continue;
                        }

                        filas.add(List.of(
                                String.valueOf(socio.getIdSocio()),
                                socio.getNombre(),
                                socio.getApellido(),
                                socio.getTelefono() == null
                                ? ""
                                : socio.getTelefono(),
                                socio.getCorreo() == null
                                ? ""
                                : socio.getCorreo(),
                                socio.getTipoMembresia(),
                                formatearFecha(socio.getFechaVencimiento()),
                                estado.name()
                        ));
                    }
                }

                // =========================================
                // REPORTE DE MEMBRESÍAS
                // =========================================
                case MEMBRESIAS -> {

                    validarPeriodo(desde, hasta);

                    columnas.addAll(List.of(
                            "ID socio",
                            "Nombre",
                            "Apellido",
                            "ID membresía",
                            "Tipo",
                            "Fecha inicio",
                            "Vencimiento",
                            "Precio",
                            "Estado"
                    ));

                    estadoMembresia estadoFiltro
                            = convertirEstadoFiltro(filtroActual);

                    List<MembresiaReporte> membresias
                            = membresiaService.generarReporteMembresias(
                                    desde,
                                    hasta,
                                    estadoFiltro
                            );

                    for (MembresiaReporte membresia : membresias) {

                        filas.add(List.of(
                                String.valueOf(membresia.getIdSocio()),
                                membresia.getNombre(),
                                membresia.getApellido(),
                                String.valueOf(membresia.getIdMembresia()),
                                membresia.getTipo().name(),
                                formatearFecha(membresia.getFechaInicio()),
                                formatearFecha(membresia.getFechaVencimiento()),
                                formatearMonto(membresia.getPrecio()),
                                membresia.getEstado().name()
                        ));
                    }
                }

                // =========================================
                // REPORTE DE PAGOS
                // =========================================
                case PAGOS -> {

                    validarPeriodo(desde, hasta);

                    columnas.addAll(List.of(
                            "ID pago",
                            "ID socios",
                            "Socios",
                            "ID membresía",
                            "Tipo membresía",
                            "Fecha de pago",
                            "Monto",
                            "Método de pago"
                    ));

                    ReportePagos reportePagos
                            = pagoService.generarReportePagos(desde, hasta);

                    BigDecimal total = BigDecimal.ZERO;

                    for (PagoReporte pago : reportePagos.getPagos()) {

                        // Aplicar el filtro de método de pago.
                        if (!filtroActual.equalsIgnoreCase("Todos")
                                && !pago.getMetodoPago().equalsIgnoreCase(filtroActual)) {
                            continue;
                        }

                        filas.add(List.of(
                                String.valueOf(pago.getIdPago()),
                                unirIds(pago.getIdsSocios()),
                                String.join(", ", pago.getNombresSocios()),
                                String.valueOf(pago.getIdMembresia()),
                                pago.getTipoMembresia().name(),
                                formatearFecha(pago.getFechaPago()),
                                formatearMonto(pago.getMonto()),
                                pago.getMetodoPago()
                        ));

                        total = total.add(pago.getMonto());
                    }

                    // Agregar el total de los pagos mostrados.
                    filas.add(List.of(
                            "",
                            "",
                            "",
                            "",
                            "",
                            "TOTAL",
                            formatearMonto(total),
                            ""
                    ));
                }

                // =========================================
                // REPORTE DE ACCESOS
                // =========================================
                case ACCESOS -> {

                    validarPeriodo(desde, hasta);

                    columnas.addAll(List.of(
                            "ID socio",
                            "Socio",
                            "Fecha",
                            "Hora de entrada",
                            "Hora de salida"
                    ));

                    List<AccesoReporte> accesos
                            = accesoService.generarReporteAccesos(
                                    desde,
                                    hasta
                            );

                    for (AccesoReporte acceso : accesos) {

                        filas.add(List.of(
                                String.valueOf(acceso.getIdSocio()),
                                acceso.getNombreSocio(),
                                formatearFecha(acceso.getFecha()),
                                formatearHora(acceso.getHoraEntrada()),
                                formatearHora(acceso.getHoraSalida())
                        ));
                    }
                }
            }

            return new TablaReporte(columnas, filas);

        } catch (SQLException | IOException e) {

            throw new IllegalStateException(
                    "No se pudo generar el reporte: " + e.getMessage(),
                    e
            );
        }
    }

    // Validar que el periodo del reporte sea correcto.
    private void validarPeriodo(LocalDate desde, LocalDate hasta) {

        if (desde == null || hasta == null) {
            throw new IllegalArgumentException(
                    "Debe seleccionar las fechas del reporte."
            );
        }

        if (desde.isAfter(hasta)) {
            throw new IllegalArgumentException(
                    "La fecha inicial no puede ser posterior a la final."
            );
        }
    }

// Convertir el filtro de texto a estado de membresía.
    private estadoMembresia convertirEstadoFiltro(String filtro) {

        if (filtro == null || filtro.equalsIgnoreCase("Todos")) {
            return null;
        }

        return estadoMembresia.valueOf(
                filtro.toUpperCase(Locale.ROOT)
        );
    }

// Formatear fechas.
    private String formatearFecha(LocalDate fecha) {

        return fecha == null ? "" : fecha.toString();
    }

// Formatear horas.
    private String formatearHora(LocalTime hora) {

        return hora == null ? "Sin salida" : hora.toString();
    }

// Formatear importes monetarios.
    private String formatearMonto(BigDecimal monto) {

        return monto == null
                ? "$0.00"
                : "$" + monto.setScale(2, RoundingMode.HALF_UP)
                        .toPlainString();
    }

// Unir identificadores de socios asociados a un pago.
    private String unirIds(List<Integer> ids) {

        return ids.stream()
                .map(String::valueOf)
                .collect(java.util.stream.Collectors.joining(", "));
    }

    @Override
    public List<UsuarioSistema> listarUsuarios() {
        try {
            return usuarioService.listarUsuarios();
        } catch (SQLException | IOException ex) {
            throw new IllegalStateException(
                    "No fue posible consultar los usuarios del sistema.",
                    ex
            );
        }
    }

    @Override
    public UsuarioSistema crearUsuario(
            rolUsuario rol,
            String usuario,
            String contrasena) {

        try {
            int idGenerado = usuarioService.registrarUsuario(
                    rol,
                    usuario,
                    contrasena
            );

            return usuarioService.buscarPorId(idGenerado);

        } catch (SQLException | IOException ex) {
            throw new IllegalStateException(
                    "No fue posible registrar el usuario.",
                    ex
            );
        }
    }

    @Override
    public void cambiarActivoUsuario(
            int idEmpleado,
            boolean activo) {

        try {
            boolean actualizado = usuarioService.cambiarEstado(
                    idEmpleado,
                    activo
            );

            if (!actualizado) {
                throw new IllegalStateException(
                        "No fue posible actualizar el estado del usuario."
                );
            }

        } catch (SQLException | IOException ex) {
            throw new IllegalStateException(
                    "Ocurrió un error al cambiar el estado del usuario.",
                    ex
            );
        }
    }

    @Override
    public int siguienteIdUsuario() {
        try {
            List<UsuarioSistema> usuarios
                    = usuarioService.listarUsuarios();

            int mayorId = 0;

            for (UsuarioSistema usuario : usuarios) {
                if (usuario.getIdEmpleado() > mayorId) {
                    mayorId = usuario.getIdEmpleado();
                }
            }

            return mayorId + 1;

        } catch (SQLException | IOException ex) {
            throw new IllegalStateException(
                    "No fue posible obtener el siguiente identificador de usuario.",
                    ex
            );
        }
    }

    @Override
    public void actualizarDatosUsuario(
            int idEmpleado,
            rolUsuario rol,
            String usuario) {

        try {
            boolean actualizado = usuarioService.actualizarDatos(
                    idEmpleado,
                    rol,
                    usuario
            );

            if (!actualizado) {
                throw new IllegalStateException(
                        "No fue posible actualizar los datos del usuario."
                );
            }

        } catch (SQLException | IOException ex) {
            throw new IllegalStateException(
                    "Ocurrió un error al modificar el usuario.",
                    ex
            );
        }
    }

    @Override
    public void cambiarContrasenaUsuario(
            int idEmpleado,
            String nuevaContrasena) {

        try {
            boolean actualizado = usuarioService.cambiarContrasena(
                    idEmpleado,
                    nuevaContrasena
            );

            if (!actualizado) {
                throw new IllegalStateException(
                        "No fue posible cambiar la contraseña."
                );
            }

        } catch (SQLException | IOException ex) {
            throw new IllegalStateException(
                    "Ocurrió un error al cambiar la contraseña.",
                    ex
            );
        }
    }

    @Override
    public String generarRespaldo(String carpeta) throws Exception {
        throw pendiente("generarRespaldo");
    }
}
