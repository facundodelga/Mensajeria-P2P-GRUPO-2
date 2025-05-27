package org.example.cliente.cifrado; // Ajustado al nuevo paquete

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.security.SecureRandom;
import java.util.Arrays;

public class Cifrador implements ICifradorMensajes {
    private static final String ESQUEMA_CIFRADO = "AES/GCM/NoPadding";
    private static final int LONGITUD_IV_GCM = 12; // bytes (recomendado para GCM)
    private static final int LONGITUD_TAG_GCM = 16; // bytes (128 bits)

    @Override
    public byte[] encriptar(String textoPlano, SecretKey claveSecreta) throws Exception {
        byte[] iv = new byte[LONGITUD_IV_GCM];
        new SecureRandom().nextBytes(iv); // Generar un IV único para cada encriptación

        Cipher cifrador = Cipher.getInstance(ESQUEMA_CIFRADO);
        GCMParameterSpec parametrosGCM = new GCMParameterSpec(LONGITUD_TAG_GCM * 8, iv);
        cifrador.init(Cipher.ENCRYPT_MODE, claveSecreta, parametrosGCM);

        byte[] textoCifrado = cifrador.doFinal(textoPlano.getBytes("UTF-8"));

        // Concatenar IV y texto cifrado. El IV es parte del mensaje cifrado.
        byte[] mensajeEncriptadoConIV = new byte[iv.length + textoCifrado.length];
        System.arraycopy(iv, 0, mensajeEncriptadoConIV, 0, iv.length);
        System.arraycopy(textoCifrado, 0, mensajeEncriptadoConIV, iv.length, textoCifrado.length);

        return mensajeEncriptadoConIV;
    }

    @Override
    public String desencriptar(byte[] textoCifradoConIV, SecretKey claveSecreta) throws Exception {
        // Extraer el IV
        byte[] iv = Arrays.copyOfRange(textoCifradoConIV, 0, LONGITUD_IV_GCM);

        // Extraer el texto cifrado real
        byte[] textoCifradoPuro = Arrays.copyOfRange(textoCifradoConIV, LONGITUD_IV_GCM, textoCifradoConIV.length);

        Cipher cifrador = Cipher.getInstance(ESQUEMA_CIFRADO);
        GCMParameterSpec parametrosGCM = new GCMParameterSpec(LONGITUD_TAG_GCM * 8, iv);
        cifrador.init(Cipher.DECRYPT_MODE, claveSecreta, parametrosGCM);

        byte[] textoDesencriptadoBytes = cifrador.doFinal(textoCifradoPuro);
        return new String(textoDesencriptadoBytes, "UTF-8");
    }
}