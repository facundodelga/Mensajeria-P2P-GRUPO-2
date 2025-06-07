// src/main/java/org/example/cliente/modelo/conversacion/Conversacion.java
package org.example.cliente.modelo.conversacion;

import org.example.cliente.modelo.mensaje.Mensaje;
import org.example.cliente.modelo.usuario.Contacto;

import javax.crypto.SecretKey;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Conversacion implements Serializable {
    private Contacto contactoRemoto;
    private List<Mensaje> mensajes;
    private SecretKey claveSecretaAes;
    private boolean myPublicKeySent;
    private boolean pendiente; // AÑADIDA: Para el estado de mensajes pendientes

    public Conversacion(Contacto contactoRemoto) {
        this.contactoRemoto = contactoRemoto;
        this.mensajes = new ArrayList<>();
        this.claveSecretaAes = null; // Asegurarse de que se inicializa a null
        this.myPublicKeySent = false; // Inicializar en falso para el intercambio de claves
        this.pendiente = false; // Inicializar en falso para el estado de pendiente
    }

    public Contacto getContactoRemoto() {
        return contactoRemoto;
    }

    public void addMensaje(Mensaje mensaje) {
        this.mensajes.add(mensaje);
    }

    public List<Mensaje> getMensajes() {
        return Collections.unmodifiableList(mensajes);
    }

    public SecretKey getClaveSecretaAes() {
        return claveSecretaAes;
    }

    public void setClaveSecretaAes(SecretKey claveSecretaAes) {
        this.claveSecretaAes = claveSecretaAes;
    }

    public boolean isMyPublicKeySent() {
        return myPublicKeySent;
    }

    public void setMyPublicKeySent(boolean myPublicKeySent) {
        this.myPublicKeySent = myPublicKeySent;
    }

    // NUEVOS MÉTODOS para el estado de pendiente
    public boolean isPendiente() {
        return pendiente;
    }

    public void setPendiente(boolean pendiente) {
        this.pendiente = pendiente;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Conversacion that = (Conversacion) o;
        return contactoRemoto.equals(that.contactoRemoto);
    }

    @Override
    public int hashCode() {
        return contactoRemoto.hashCode();
    }
}