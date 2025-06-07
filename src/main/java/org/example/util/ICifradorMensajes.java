// Contenido de ICifradorMensajes.java (adaptado)
package org.example.util;

import javax.crypto.SecretKey;

public interface ICifradorMensajes {
    // Retorna String para el texto cifrado en Base64, ya que Cifrador lo hace
    String encriptar(String textoPlano, SecretKey claveSecreta) throws Exception;
    // Recibe String para el texto cifrado en Base64
    String desencriptar(String textoCifrado, SecretKey claveSecreta) throws Exception;
}