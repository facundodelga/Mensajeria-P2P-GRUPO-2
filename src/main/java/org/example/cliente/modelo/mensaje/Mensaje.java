package org.example.cliente.modelo.mensaje;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Mensaje implements Serializable {
    private String emisor;
    private String receptor;
    private String contenidoCifrado;
    private String contenidoPlano; // NUEVO CAMPO
    private LocalDateTime timestamp;

    // Constructor para mensajes CIFRADOS (para la red)
    public Mensaje(String emisor, String receptor, String contenidoCifrado) {
        this.emisor = emisor;
        this.receptor = receptor;
        this.contenidoCifrado = contenidoCifrado;
        this.timestamp = LocalDateTime.now();
        this.contenidoPlano = null; // No disponible en este constructor
    }

    // NUEVO CONSTRUCTOR: Para crear un Mensaje para la VISTA/almacenamiento interno con contenido PLANO y timestamp original
    public Mensaje(String emisor, String receptor, String contenidoPlano, LocalDateTime timestamp) {
        this.emisor = emisor;
        this.receptor = receptor;
        this.contenidoPlano = contenidoPlano;
        this.timestamp = timestamp;
        this.contenidoCifrado = null; // No disponible en este constructor (o podrías cifrarlo aquí si quieres)
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

    public String getContenidoPlano() { // NUEVO GETTER
        return contenidoPlano;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    // Puedes añadir un setter para contenidoPlano si necesitas modificarlo después de la creación
    public void setContenidoPlano(String contenidoPlano) {
        this.contenidoPlano = contenidoPlano;
    }

    @Override
    public String toString() {
        return "Mensaje{" +
                "emisor='" + emisor + '\'' +
                ", receptor='" + receptor + '\'' +
                ", contenidoCifrado='" + (contenidoCifrado != null ? contenidoCifrado.substring(0, Math.min(contenidoCifrado.length(), 20)) + "..." : "N/A") + "'" +
                ", contenidoPlano='" + (contenidoPlano != null ? contenidoPlano.substring(0, Math.min(contenidoPlano.length(), 20)) + "..." : "N/A") + "'" +
                ", timestamp=" + timestamp +
                '}';
    }
}