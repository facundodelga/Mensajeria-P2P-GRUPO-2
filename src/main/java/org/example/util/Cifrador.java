// src/main/java/org/example/util/cifrado/Cifrador.java
package org.example.util;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec; // Necesario para el modo CBC o similar
import java.security.SecureRandom;
import java.util.Base64;

public class Cifrador {
    // Es buena práctica usar un IV (Initialization Vector) para cada cifrado,
    // y debe ser único para cada operación de cifrado con la misma clave.
    // El IV se envía junto con el texto cifrado (generalmente al principio).
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding"; // AES con modo CBC y padding

    /**
     * Cifra un texto plano usando una clave secreta AES.
     * Genera un IV aleatorio para cada cifrado y lo prepone al texto cifrado.
     *
     * @param plainText El texto a cifrar.
     * @param secretKey La clave secreta AES.
     * @return El IV + texto cifrado, codificado en Base64.
     * @throws Exception Si ocurre un error durante el cifrado.
     */
    public static String cifrar(String plainText, SecretKey secretKey) throws Exception {
        byte[] iv = new byte[16]; // 16 bytes para un IV de AES (tamaño de bloque)
        new SecureRandom().nextBytes(iv); // Generar un IV aleatorio

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(iv)); // Inicializar con clave e IV

        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes("UTF-8"));

        // Combinar el IV y el texto cifrado, luego codificar en Base64
        byte[] combined = new byte[iv.length + encryptedBytes.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encryptedBytes, 0, combined, iv.length, encryptedBytes.length);

        return Base64.getEncoder().encodeToString(combined);
    }

    /**
     * Descifra un texto codificado en Base64 usando una clave secreta AES.
     * Extrae el IV del inicio del texto cifrado.
     *
     * @param encryptedBase64Text El texto cifrado (IV + contenido), codificado en Base64.
     * @param secretKey La clave secreta AES.
     * @return El texto descifrado en texto plano.
     * @throws Exception Si ocurre un error durante el descifrado.
     */
    public static String descifrar(String encryptedBase64Text, SecretKey secretKey) throws Exception {
        byte[] decodedCombined = Base64.getDecoder().decode(encryptedBase64Text);

        byte[] iv = new byte[16];
        System.arraycopy(decodedCombined, 0, iv, 0, iv.length); // Extraer el IV

        byte[] encryptedBytes = new byte[decodedCombined.length - iv.length];
        System.arraycopy(decodedCombined, iv.length, encryptedBytes, 0, encryptedBytes.length); // Extraer el texto cifrado

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv)); // Inicializar con clave e IV

        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        return new String(decryptedBytes, "UTF-8");
    }
}