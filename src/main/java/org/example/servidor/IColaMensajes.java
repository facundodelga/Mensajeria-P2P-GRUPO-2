package org.example.servidor;

import org.example.cliente.modelo.mensaje.Mensaje;
import org.example.cliente.modelo.usuario.Contacto;

import java.io.Serializable;
import java.util.ArrayList;

public interface IColaMensajes extends Serializable {
    void agregarMensajePendiente(Mensaje mensaje);
    ArrayList<Mensaje> getMensajesRecibidos();
    void removeMensaje(Mensaje mensaje);
    void eliminarMensajesPorReceptor(Contacto usuario);

    Object clonar();

}
