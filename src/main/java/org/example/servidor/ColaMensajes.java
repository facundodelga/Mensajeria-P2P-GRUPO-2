package org.example.servidor;

import org.example.cliente.modelo.mensaje.Mensaje;
import org.example.cliente.modelo.usuario.Contacto;
import java.util.ArrayList;
import java.util.List;

public class ColaMensajes implements IColaMensajes {
    private final ArrayList<Mensaje> mensajesRecibidos;

    public ColaMensajes() {
        mensajesRecibidos = new ArrayList<>();
    }

    @Override
    public ArrayList<Mensaje> getMensajesRecibidos() {
        return mensajesRecibidos;
    }

    public void agregarMensajePendiente(Mensaje mensaje) {
        mensajesRecibidos.add(mensaje);
    }

    public void removeMensaje(Mensaje mensaje) {
        mensajesRecibidos.remove(mensaje);
    }

    @Override
    public void eliminarMensajesPorReceptor(Contacto receptor) {
        mensajesRecibidos.removeIf(mensaje -> mensaje.getReceptor().equals(receptor));
    }
}