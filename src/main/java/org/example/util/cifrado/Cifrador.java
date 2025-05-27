package org.example.util.cifrado;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class Cifrador{

    private static final String ALGORITHM = "AES"; // Algoritmo de cifrado
    private static final int KEY_SIZE = 128; // Tamaño de la clave en bits (128, 192 o 256)

    /**
     * Genera una nueva clave secreta AES aleatoria.
     * En un sistema P2P, esta clave debería ser generada por un lado y compartida de forma segura
     * con el otro lado ANTES de enviar mensajes cifrados.
     * @return Una nueva clave secreta AES.
     * @throws NoSuchAlgorithmException Si el algoritmo AES no está disponible.
     */
    public static SecretKey generarClaveAES() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM);
        keyGen.init(KEY_SIZE);
        return keyGen.generateKey();
    }

    /**
     * Cifra un texto plano usando una clave secreta AES.
     * @param plainText El texto a cifrar.
     * @param secretKey La clave secreta AES.
     * @return El texto cifrado en formato Base64.
     * @throws Exception Si ocurre un error durante el cifrado.
     */
    public static String cifrar(String plainText, SecretKey secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    /**
     * Descifra un texto cifrado (en Base64) usando una clave secreta AES.
     * @param encryptedText El texto cifrado en formato Base64.
     * @param secretKey La clave secreta AES.
     * @return El texto descifrado.
     * @throws Exception Si ocurre un error durante el descifrado.
     */
    public static String descifrar(String encryptedText, SecretKey secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decodedBytes = Base64.getDecoder().decode(encryptedText);
        byte[] decryptedBytes = cipher.doFinal(decodedBytes);
        return new String(decryptedBytes);
    }

    /**
     * Convierte una clave SecretKey a una cadena Base64 para almacenamiento/transmisión.
     * @param secretKey La clave secreta.
     * @return La clave codificada en Base64.
     */
    public static String claveATexto(SecretKey secretKey) {
        return Base64.getEncoder().encodeToString(secretKey.getEncoded());
    }

    /**
     * Convierte una cadena Base64 de vuelta a una clave SecretKey.
     * @param encodedKey La clave codificada en Base64.
     * @return La clave secreta.
     */
    public static SecretKey textoAClave(String encodedKey) {
        byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
        return new SecretKeySpec(decodedKey, 0, decodedKey.length, ALGORITHM);
    }
}