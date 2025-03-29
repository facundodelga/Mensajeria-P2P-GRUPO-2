package org.example.modelo.conversacion;

import org.example.modelo.mensaje.Mensaje;

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


    public boolean isPendiente() {
        return pendiente;
    }

    public void setPendiente(boolean pendiente) {
        this.pendiente = pendiente;
    }


}
