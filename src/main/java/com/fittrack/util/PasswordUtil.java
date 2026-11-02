
package com.fittrack.util;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class PasswordUtil {

    private static final String ALGORITMO =
            "PBKDF2WithHmacSHA256";

    private static final String IDENTIFICADOR =
            "pbkdf2-sha256";

    private static final int ITERACIONES = 600_000;

    private static final int LONGITUD_SALT = 16;

    private static final int LONGITUD_HASH = 256;

    private static final SecureRandom RANDOM =
            new SecureRandom();

    // Generar un hash seguro para una contraseña.
    public static String generarHash(String contrasena) {

        if (contrasena == null || contrasena.isBlank()) {
            throw new IllegalArgumentException(
                "La contraseña no puede estar vacía."
            );
        }

        byte[] salt = new byte[LONGITUD_SALT];
        RANDOM.nextBytes(salt);

        byte[] hash = derivarHash(
            contrasena.toCharArray(),
            salt,
            ITERACIONES
        );

        return IDENTIFICADOR + "$"
                + ITERACIONES + "$"
                + Base64.getEncoder().encodeToString(salt) + "$"
                + Base64.getEncoder().encodeToString(hash);
    }

    // Verificar una contraseña contra un hash almacenado.
    public static boolean verificarContrasena(
            String contrasena,
            String hashAlmacenado) {

        if (contrasena == null
                || hashAlmacenado == null
                || hashAlmacenado.isBlank()) {
            return false;
        }

        try {
            String[] partes = hashAlmacenado.split("\\$");

            if (partes.length != 4
                    || !IDENTIFICADOR.equals(partes[0])) {
                return false;
            }

            int iteraciones = Integer.parseInt(partes[1]);

            if (iteraciones < 1 || iteraciones > 2_000_000) {
                return false;
            }

            byte[] salt = Base64.getDecoder()
                    .decode(partes[2]);

            byte[] hashOriginal = Base64.getDecoder()
                    .decode(partes[3]);

            if (salt.length != LONGITUD_SALT
                    || hashOriginal.length != LONGITUD_HASH / 8) {
                return false;
            }

            byte[] hashCalculado = derivarHash(
                contrasena.toCharArray(),
                salt,
                iteraciones
            );

            return MessageDigest.isEqual(
                hashOriginal,
                hashCalculado
            );

        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    // Derivar el hash mediante PBKDF2.
    private static byte[] derivarHash(
            char[] contrasena,
            byte[] salt,
            int iteraciones) {

        PBEKeySpec especificacion = new PBEKeySpec(
            contrasena,
            salt,
            iteraciones,
            LONGITUD_HASH
        );

        try {
            SecretKeyFactory fabrica =
                    SecretKeyFactory.getInstance(ALGORITMO);

            return fabrica.generateSecret(especificacion)
                    .getEncoded();

        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException(
                "No se pudo generar el hash de la contraseña.",
                ex
            );

        } finally {
            especificacion.clearPassword();
            java.util.Arrays.fill(contrasena, '\0');
        }
    }
}
