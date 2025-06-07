package org.example.cliente.modelo.mensaje;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Mensaje implements Serializable {
    private String emisor;
    private String receptor;
    private String contenidoCifrado; // Ahora almacenamos el contenido cifrado
    private LocalDateTime timestamp;

    // Constructor para mensajes YA CIFRADOS o para crear el objeto antes de cifrar
    public Mensaje(String emisor, String receptor, String contenidoCifrado) {
        this.emisor = emisor;
        this.receptor = receptor;
        this.contenidoCifrado = contenidoCifrado;
        this.timestamp = LocalDateTime.now();
    }

    // Constructor to include timestamp if creating from deserialized message
    public Mensaje(String emisor, String receptor, String contenidoCifrado, LocalDateTime timestamp) {
        this.emisor = emisor;
        this.receptor = receptor;
        this.contenidoCifrado = contenidoCifrado;
        this.timestamp = timestamp;
    }


    public String getEmisor() {
        return emisor;
    }

    public String getReceptor() {
        return receptor;
    }

    public String getContenidoCifrado() {
        return contenidoCifrado;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    // Si tu mensaje también tenía un contenido en texto plano para mostrarlo,
    // es mejor que lo descifres en el momento de mostrarlo, no que lo guardes aquí.
    // Esto es para asegurar que el Mensaje transportado siempre esté cifrado.

    @Override
    public String toString() {
        return "Mensaje{" +
                "emisor='" + emisor + '\'' +
                ", receptor='" + receptor + '\'' +
                ", contenidoCifrado='" + contenidoCifrado.substring(0, Math.min(contenidoCifrado.length(), 20)) + "...'" + // Muestra solo una parte
                ", timestamp=" + timestamp +
                '}';
    }
}