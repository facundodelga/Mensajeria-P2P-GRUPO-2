package org.example.servidor;

import org.example.cliente.modelo.mensaje.Mensaje;
import org.example.cliente.modelo.usuario.Contacto;

import java.util.ArrayList;
import java.util.List;

public interface IColaMensajes {
    void agregarMensajePendiente(Mensaje mensaje);
    ArrayList<Mensaje> getMensajesRecibidos();
    void removeMensaje(Mensaje mensaje);
    void eliminarMensajesPorReceptor(Contacto usuario);
}
