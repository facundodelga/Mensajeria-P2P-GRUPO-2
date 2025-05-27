package org.example.cliente.cifrado; // Ajustado al nuevo paquete

import javax.crypto.SecretKey;

public interface ICifradorMensajes {
    byte[] encriptar(String textoPlano, SecretKey claveSecreta) throws Exception;
    String desencriptar(byte[] textoCifrado, SecretKey claveSecreta) throws Exception;
}