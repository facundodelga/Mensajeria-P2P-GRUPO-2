// src/main/java/org/example/cliente/modelo/conversacion/Conversacion.java (Modificado)
package org.example.cliente.modelo.conversacion;

import org.example.cliente.modelo.mensaje.Mensaje;

import javax.crypto.SecretKey; // ¡Importar SecretKey!
import java.util.ArrayList;
import java.util.List;

public class Conversacion {
    private List<Mensaje> mensajes = new ArrayList<>();
    private boolean pendiente = true;
    private SecretKey claveSecretaAes; // <-- ¡Nuevo atributo para la clave simétrica!

    public Conversacion() {
        // La clave se establecerá DESPUÉS del intercambio de claves
    }

    public List<Mensaje> getMensajes() {
        return mensajes;
    }

    public boolean isPendiente() {
        return pendiente;
    }

    public void setPendiente(boolean pendiente) {
        this.pendiente = pendiente;
    }

    // Nuevos getters y setters para la clave secreta
    public SecretKey getClaveSecretaAes() {
        return claveSecretaAes;
    }

    public void setClaveSecretaAes(SecretKey claveSecretaAes) {
        this.claveSecretaAes = claveSecretaAes;
    }
}