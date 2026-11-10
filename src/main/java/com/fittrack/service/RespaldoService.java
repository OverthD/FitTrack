package com.fittrack.service;

import com.fittrack.dao.RespaldoDAO;
import com.fittrack.util.SesionUsuario;

import com.fittrack.servicio.RespaldoResumen;
import java.util.List;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclEntryPermission;
import java.nio.file.attribute.AclEntryType;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.UserPrincipal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.List;
import java.util.Properties;

public class RespaldoService {

    private static final String RUTA_MYSQLDUMP
            = "C:\\Program Files\\MySQL\\MySQL Server 8.0\\bin\\mysqldump.exe";

    private static final String ARCHIVO_CONFIGURACION
            = "/db.properties";

    private static final DateTimeFormatter FORMATO_FECHA
            = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss-SSS");

    private final RespaldoDAO respaldoDAO;

    public RespaldoService() {
        this.respaldoDAO = new RespaldoDAO();
    }

    // Generar un respaldo completo de la base de datos.
    public Path generarRespaldo(Path directorioDestino)
            throws SQLException, IOException {

        // Verificar que exista una sesión administrativa.
        if (!SesionUsuario.haySesionActiva()
                || !SesionUsuario.esAdministrador()) {

            throw new SecurityException(
                    "No tiene permisos para generar respaldos. "
                    + "Se requiere un administrador."
            );
        }

        int idAdministrador = SesionUsuario.obtenerIdEmpleado();

        // Validar la ubicación seleccionada.
        if (directorioDestino == null
                || !Files.isDirectory(directorioDestino)
                || !Files.isWritable(directorioDestino)) {

            throw new IllegalArgumentException(
                    "La ubicación seleccionada no es válida "
                    + "o no permite guardar archivos."
            );
        }

        // Verificar la existencia de mysqldump.
        Path ejecutable = Paths.get(RUTA_MYSQLDUMP);

        if (!Files.isRegularFile(ejecutable)) {
            throw new IOException(
                    "No se encontró mysqldump.exe en la ruta configurada."
            );
        }

        // Leer los datos de conexión.
        Properties propiedades = cargarConfiguracion();

        String url = propiedades.getProperty("db.url");
        String usuario = propiedades.getProperty("db.user");
        String contrasena = propiedades.getProperty("db.password", "");

        if (url == null || usuario == null || usuario.isBlank()) {
            throw new IOException(
                    "La configuración de la base de datos está incompleta."
            );
        }

        // Obtener host, puerto y nombre de la base de datos.
        java.net.URI uri;

        try {
            uri = java.net.URI.create(
                    url.replaceFirst("^jdbc:", "")
            );
        } catch (IllegalArgumentException e) {
            throw new IOException(
                    "La URL de conexión a MySQL no es válida.",
                    e
            );
        }

        String host = uri.getHost();
        int puerto = uri.getPort() == -1 ? 3306 : uri.getPort();
        String baseDatos = uri.getPath();

        if (host == null || baseDatos == null
                || baseDatos.length() <= 1) {

            throw new IOException(
                    "No se pudo identificar el servidor "
                    + "o la base de datos."
            );
        }

        baseDatos = baseDatos.substring(1);

        if (!baseDatos.matches("[a-zA-Z0-9_$]+")) {
            throw new IOException(
                    "El nombre de la base de datos no es válido."
            );
        }

        // Capturar una sola fecha y hora para toda la operación.
        LocalDateTime fechaHora = LocalDateTime.now();

        String nombreArchivo = "FitTrack_Respaldo_"
                + fechaHora.format(FORMATO_FECHA)
                + ".sql";

        Path archivoRespaldo
                = directorioDestino.resolve(nombreArchivo);

        Path archivoConfiguracion = null;
        Path archivoErrores = null;

        boolean respaldoGenerado = false;

        try {

            // Crear archivos temporales.
            archivoConfiguracion = Files.createTempFile(
                    "fittrack-mysql-", ".cnf"
            );

            archivoErrores = Files.createTempFile(
                    "fittrack-error-", ".txt"
            );

            // Restringir el acceso al archivo de credenciales.
            restringirPermisos(archivoConfiguracion);

            // Preparar las credenciales.
            String configuracion = """
                [client]
                host=%s
                port=%d
                user=%s
                password=%s
                """.formatted(
                    escaparOpcion(host),
                    puerto,
                    escaparOpcion(usuario),
                    escaparOpcion(contrasena)
            );

            Files.writeString(
                    archivoConfiguracion,
                    configuracion,
                    StandardCharsets.UTF_8
            );

            // Ejecutar mysqldump.
            ProcessBuilder procesoBuilder = new ProcessBuilder(
                    RUTA_MYSQLDUMP,
                    "--defaults-extra-file="
                    + archivoConfiguracion.toAbsolutePath(),
                    "--single-transaction",
                    "--routines",
                    "--events",
                    "--triggers",
                    "--databases",
                    baseDatos
            );

            procesoBuilder.redirectOutput(
                    archivoRespaldo.toFile()
            );

            procesoBuilder.redirectError(
                    archivoErrores.toFile()
            );

            Process proceso = procesoBuilder.start();

            int codigoSalida;

            try {
                codigoSalida = proceso.waitFor();

            } catch (InterruptedException e) {

                proceso.destroyForcibly();
                Thread.currentThread().interrupt();

                throw new IOException(
                        "La generación del respaldo fue interrumpida.",
                        e
                );
            }

            // Comprobar que mysqldump terminó correctamente.
            if (codigoSalida != 0
                    || !Files.exists(archivoRespaldo)
                    || Files.size(archivoRespaldo) == 0) {

                String detalle = Files.readString(
                        archivoErrores,
                        StandardCharsets.UTF_8
                );

                throw new IOException(
                        "No se pudo generar el respaldo. "
                        + "Código de salida: " + codigoSalida
                        + ". Detalle: " + detalle
                );
            }

            respaldoGenerado = true;

            // Registrar el respaldo en la base de datos.
            try {

                respaldoDAO.registrar(
                        fechaHora.toLocalDate(),
                        idAdministrador
                );

            } catch (SQLException e) {

                throw new SQLException(
                        "El archivo de respaldo se generó correctamente en: "
                        + archivoRespaldo.toAbsolutePath()
                        + ", pero no se pudo registrar su generación "
                        + "en la base de datos.",
                        e
                );
            }

            return archivoRespaldo;

        } catch (SQLException | IOException | RuntimeException e) {

            // Eliminar archivos incompletos.
            // Si el SQL ya se generó, conservarlo aunque falle
            // el registro de su generación.
            if (!respaldoGenerado) {
                Files.deleteIfExists(archivoRespaldo);
            }

            throw e;

        } finally {

            // Eliminar archivos temporales si llegaron a crearse.
            if (archivoConfiguracion != null) {
                Files.deleteIfExists(archivoConfiguracion);
            }

            if (archivoErrores != null) {
                Files.deleteIfExists(archivoErrores);
            }
        }
    }

    // Cargar la configuración de la conexión.
    private Properties cargarConfiguracion()
            throws IOException {

        Properties propiedades = new Properties();

        try (InputStream archivo
                = RespaldoService.class.getResourceAsStream(
                        ARCHIVO_CONFIGURACION)) {

            if (archivo == null) {
                throw new IOException(
                        "No se encontró el archivo db.properties."
                );
            }

            propiedades.load(archivo);
        }

        return propiedades;
    }

    // Escapar valores para el archivo de opciones de MySQL.
    private String escaparOpcion(String valor) {

        return "\""
                + valor.replace("\\", "\\\\")
                        .replace("\"", "\\\"")
                + "\"";
    }

    // Restringir el acceso al archivo temporal de credenciales.
    private void restringirPermisos(Path archivo)
            throws IOException {

        PosixFileAttributeView vistaPosix
                = Files.getFileAttributeView(
                        archivo,
                        PosixFileAttributeView.class
                );

        if (vistaPosix != null) {

            Files.setPosixFilePermissions(
                    archivo,
                    EnumSet.of(
                            PosixFilePermission.OWNER_READ,
                            PosixFilePermission.OWNER_WRITE
                    )
            );

            return;
        }

        AclFileAttributeView vistaAcl
                = Files.getFileAttributeView(
                        archivo,
                        AclFileAttributeView.class
                );

        if (vistaAcl != null) {

            UserPrincipal propietario = Files.getOwner(archivo);

            AclEntry entrada = AclEntry.newBuilder()
                    .setType(AclEntryType.ALLOW)
                    .setPrincipal(propietario)
                    .setPermissions(EnumSet.allOf(
                            AclEntryPermission.class
                    ))
                    .build();

            vistaAcl.setAcl(List.of(entrada));
        }
    }

    // Consultar el historial de respaldos.
    public List<RespaldoResumen> listarRespaldos()
            throws SQLException, IOException {

        // Verificar que exista una sesión administrativa.
        if (!SesionUsuario.haySesionActiva()
                || !SesionUsuario.esAdministrador()) {

            throw new SecurityException(
                    "No tiene permisos para consultar los respaldos."
            );
        }

        return respaldoDAO.listarTodos();
    }
}
