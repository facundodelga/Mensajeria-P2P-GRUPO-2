package org.example.util; // Nuevo paquete para utilidades

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

// Utilidad para manejar claves SecretKey (generación y conversión a/desde String)
public class ClaveUtil {
    private static final String ALGORITMO = "AES";
    private static final int TAMANO_CLAVE = 256; // 256 bits para mayor seguridad

    public static SecretKey generarClaveAes() throws NoSuchAlgorithmException {
        KeyGenerator generadorClaves = KeyGenerator.getInstance(ALGORITMO);
        generadorClaves.init(TAMANO_CLAVE, new SecureRandom()); // Usar SecureRandom para criptografía
        return generadorClaves.generateKey();
    }

    public static String claveAString(SecretKey secretKey) {
        return Base64.getEncoder().encodeToString(secretKey.getEncoded());
    }

    public static SecretKey stringAClave(String encodedKey) {
        byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
        return new SecretKeySpec(decodedKey, 0, decodedKey.length, ALGORITMO);
    }
}