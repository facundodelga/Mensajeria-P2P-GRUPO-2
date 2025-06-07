package org.example.cliente.modelo.mensaje;

import java.io.Serializable;
import java.time.LocalDateTime; // Usaremos LocalDateTime para el timestamp
import java.time.format.DateTimeFormatter; // Para formatear la hora en toString, si queremos

public class Mensaje implements Serializable {
    private String emisor;
    private String receptor;
    private String contenido;          // Contenido en texto plano (se llena al crear/descifrar)
    private String contenidoCifrado;   // Contenido cifrado (se llena al cifrar/recibir)
    private LocalDateTime timestamp;   // Marca de tiempo del mensaje

    // Constructor 1: Para crear un mensaje en texto plano (antes de cifrar para enviar)
    // O cuando ya ha sido descifrado y se va a usar para la UI/lógica.
    public Mensaje(String emisor, String receptor, String contenido) {
        this.emisor = emisor;
        this.receptor = receptor;
        this.contenido = contenido;
        this.timestamp = LocalDateTime.now(); // La hora actual al crear el mensaje
        this.contenidoCifrado = null; // Aún no cifrado
    }

    // Constructor 2: Para recrear un mensaje que ha sido recibido (llega cifrado de la red)
    // NOTA: EL TIMESTAMP QUE RECIBES DE LA RED ES EL DEL MENSAJE ORIGINAL.
    public Mensaje(String emisor, String receptor, String contenidoCifrado, LocalDateTime timestamp) {
        this.emisor = emisor;
        this.receptor = receptor;
        this.contenidoCifrado = contenidoCifrado;
        this.timestamp = timestamp; // Usar el timestamp que viene con el mensaje cifrado
        this.contenido = null; // El contenido en texto plano aún no se ha descifrado
    }

    // --- Getters ---
    public String getEmisor() {
        return emisor;
    }

    public String getReceptor() {
        return receptor;
    }

    // Obtener el contenido en texto plano
    public String getContenido() {
        return contenido;
    }

    // Obtener el contenido cifrado
    public String getContenidoCifrado() {
        return contenidoCifrado;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    // --- Setters (muy importantes para el flujo de cifrado/descifrado) ---

    // Para establecer el contenido en texto plano después de descifrarlo
    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    // Para establecer el contenido cifrado después de cifrarlo
    public void setContenidoCifrado(String contenidoCifrado) {
        this.contenidoCifrado = contenidoCifrado;
    }


    @Override
    public String toString() {
        // Para hacer el toString más robusto y mostrar lo que tiene:
        String contentInfo = (contenido != null) ? "contenido='" + contenido + "'" : "contenido=null";
        String encryptedContentInfo = (contenidoCifrado != null) ?
                "contenidoCifrado='" + contenidoCifrado.substring(0, Math.min(contenidoCifrado.length(), 20)) + "...'" :
                "contenidoCifrado=null";

        // Formatear el timestamp para una mejor lectura en toString
        String formattedTimestamp = (timestamp != null) ?
                timestamp.format(DateTimeFormatter.ofPattern("HH:mm:ss dd-MM-yyyy")) : "null";

        return "Mensaje{" +
                "emisor='" + emisor + '\'' +
                ", receptor='" + receptor + '\'' +
                ", " + contentInfo +
                ", " + encryptedContentInfo +
                ", timestamp=" + formattedTimestamp +
                '}';
    }
}