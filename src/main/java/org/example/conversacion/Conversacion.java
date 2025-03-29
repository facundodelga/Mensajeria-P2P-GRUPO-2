package org.example.conversacion;

import org.example.mensaje.Mensaje;
import org.example.usuario.Usuario;

import java.util.ArrayList;
import java.util.List;

public class Conversacion {
    private List<Mensaje> mensajes = new ArrayList<>();
    private boolean pendiente = true;

    public Conversacion() {

    }

    public List<Mensaje> getMensajes() {
        return mensajes;
    }
    public void addMensaje(Mensaje mensaje) {
        mensajes.add(mensaje);
    }

    public boolean isPendiente() {
        return pendiente;
    }

    public void setPendiente(boolean pendiente) {
        this.pendiente = pendiente;
    }


}
