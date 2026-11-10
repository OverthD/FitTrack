package Pruebas;

import com.fittrack.model.UsuarioSistema;
import com.fittrack.service.UsuarioSistemaService;
import com.fittrack.dao.UsuarioSistemaDAO;

import java.util.List;

public class PruebaUsuarioSistemaService {

    public static void main(String[] args) throws Exception {

        UsuarioSistemaService servicio =
                new UsuarioSistemaService();

        UsuarioSistemaDAO dao =
                new UsuarioSistemaDAO();

        // Sustituye estos valores por tus credenciales administrativas.
        String usuario = "admin_prueba";
        String contrasena = "FitTrack2026!";

        System.out.println("\n===== DIAGNÓSTICO RF-17 =====");

        UsuarioSistema admin =
                servicio.iniciarSesion(usuario, contrasena);

        if (admin == null) {
            System.out.println("ERROR: No se pudo iniciar sesión.");
            return;
        }

        if (!admin.getRol().name().equals("ADMINISTRADOR")) {
            System.out.println("ERROR: La cuenta no es administradora.");
            servicio.cerrarSesion();
            return;
        }

        System.out.println("Sesión administrativa iniciada.");
        System.out.println("ID administrador actual: "
                + admin.getIdEmpleado());

        List<UsuarioSistema> usuarios =
                servicio.listarUsuarios();

        System.out.println("\nUsuarios registrados:");

        for (UsuarioSistema u : usuarios) {
            System.out.println("------------------------------");
            System.out.println("ID: " + u.getIdEmpleado());
            System.out.println("Usuario: " + u.getUsuario());
            System.out.println("Rol: " + u.getRol());
            System.out.println("Activo: " + u.isActivo());
        }

        System.out.println("------------------------------");
        System.out.println("Total de usuarios: " + usuarios.size());
        System.out.println("Administradores activos: "
                + dao.contarAdministradoresActivos());

        System.out.println("====================================");

        servicio.cerrarSesion();
    }
}